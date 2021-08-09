package com.app.spark.activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.models.ImportantDataModel
import com.app.spark.models.ImportantDataResponse
import com.app.spark.utils.*
import com.app.spark.viewmodel.UploadDataViewModel
import com.cancan.Utility.PermissionsUtil
import com.app.spark.utils.SharedPrefrencesManager
import com.google.gson.Gson
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import kotlinx.android.synthetic.main.activity_upload_profile.*
import kotlinx.android.synthetic.main.dialog_location_permission.*
import java.io.File


class UploadProfileActivity : BaseActivity(), View.OnClickListener {

    private lateinit var permissionsUtil: PermissionsUtil
    private lateinit var viewModel: UploadDataViewModel
    lateinit var pref: SharedPrefrencesManager
    var token=""
    var userId=""
    lateinit var model:ImportantDataModel
    var filePath=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_profile)
        permissionsUtil = PermissionsUtil(this)
        pref= SharedPrefrencesManager.getInstance(this)
        token=pref.getString(PrefConstant.ACCESS_TOKEN,"").toString()
        userId=pref.getString(PrefConstant.USER_ID,"").toString()
        if(intent.extras!=null){
            model=intent.getSerializableExtra(IntentConstant.IMPORTANT_DATA) as ImportantDataModel
        }
        applyClickListener()
        initlizeViewModel()
    }

    private fun initlizeViewModel() {

        viewModel= ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(application)).get(UploadDataViewModel::class.java)

        viewModel.response.observe(this, { res: ImportantDataResponse ->
            pref.setBoolean(PrefConstant.IS_LOGIN, true)
            pref.setString(PrefConstant.LOGIN_RESPONSE, Gson().toJson(res.result))
            val intent= Intent(this,SparkRuleActivity::class.java)
            startActivity(intent)
            finish()

        })

        viewModel.errString.observe(this, { err: String ->
            hideView(progressBar)
            showToastShort(this,err)
        })

        viewModel.errRes.observe(this, { err: Int ->
            hideView(progressBar)
            if (err != null)
                showToastShort(this, getString(err))
        })
    }

    private fun applyClickListener() {
        btnCamera.setOnClickListener(this)
        tvBack.setOnClickListener(this)
        btnUpload.setOnClickListener(this)
        tvSkip.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            btnCamera -> {
                if (doesUserHavePermission() == 0) {
                    askPermission()
                } else {
                    cameraPermissionDialog()
                }
            }
            tvBack->
                finish()
            btnUpload->{
                if(!filePath.isEmpty()) {
                    if (isNetworkAvailable(this)) {
                        showView(progressBar)
                        viewModel.importantDataAPI(token, userId, model, filePath)
                    }
                }else{
                    showToastLong(this,getString(R.string.please_upload_image))
                }
            }
            tvSkip->{
                if(isNetworkAvailable(this)){
                    showView(progressBar)
                    viewModel.importantDataAPI(token,userId,model,filePath)
                }
            }
        }
    }

    fun cameraPermissionDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_camera_permission)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        dialog.tvAllow.setOnClickListener {
            askPermission()
            dialog.dismiss()

        }
        dialog.tvDecline.setOnClickListener {
            dialog.dismiss()
            //finish()
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImagePickerUtil.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun doesUserHavePermission(): Int {
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        return permissionCheck
    }

    private fun askPermission() {
        permissionsUtil.askPermissions(this, PermissionsUtil.CAMERA, PermissionsUtil.STORAGE,
            object : PermissionsUtil.PermissionListener {
                override fun onPermissionResult(isGranted: Boolean) {
                    if (isGranted) {
                        ImagePickerUtil.selectImage(
                            this@UploadProfileActivity,
                            { imageFile, tag ->
                                filePath= imageFile!!.absolutePath
                                if (imageFile!!.exists()) {
                                    val myBitmap =
                                        BitmapFactory.decodeFile(imageFile.getAbsolutePath())
                                    imgProfile.setImageBitmap(myBitmap)
                                }
                            },
                            "user",
                            true, false, false,true
                        )
                    }
                }

            })
    }
}