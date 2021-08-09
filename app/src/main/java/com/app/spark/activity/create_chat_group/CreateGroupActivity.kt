package com.app.spark.activity.create_chat_group

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.add_post.AddPostViewModel
import com.app.spark.activity.main.MainActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityAddPostBinding
import com.app.spark.databinding.ActivityChatAddMembersBinding
import com.app.spark.databinding.ActivityCreateChatGroupBinding
import com.app.spark.models.ImportantDataResult
import com.app.spark.utils.*
import com.bumptech.glide.Glide
import com.cancan.Utility.PermissionsUtil
import com.google.gson.Gson
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_upload_profile.*
import java.io.File

class CreateGroupActivity : BaseActivity(), View.OnClickListener {
    lateinit var binding: ActivityCreateChatGroupBinding
    lateinit var viewModel: AddChatMembersViewModel
    lateinit var pref: SharedPrefrencesManager
    private var token: String? = null
    private var userId: String? = null
    private var selectedUserIds: String? = null
    private var profileImageFile: File? = null
    private var permissionsUtil: PermissionsUtil? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_chat_group)
        initlizeViewModel()
        pref = SharedPrefrencesManager.getInstance(this)
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")
        userId = pref.getString(PrefConstant.USER_ID, "")
        selectedUserIds = intent.getStringExtra(IntentConstant.SELECTED_USER_ID)
        setClickListener()
    }

    private fun setClickListener() {
        binding.imgBack.setOnClickListener(this)
        binding.tvFinish.setOnClickListener(this)
        binding.tvAddCover.setOnClickListener(this)
    }

    private fun initlizeViewModel() {

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                AddChatMembersViewModel::class.java
            )


        viewModel.commentsResponse.observe(this, { err: String? ->
            hideView(binding.progressBar)
            if (err != null) {
                showToastShort(this, err)
                setResult(RESULT_OK)
                finish()
            }
        })

        viewModel.errString.observe(this, { err: String? ->
            hideView(binding.progressBar)
            if (err != null)
                showToastShort(this, err)
        })

        viewModel.errRes.observe(this, { err: Int ->
            binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.imgBack -> {
                onBackPressed()
            }
            binding.tvFinish -> {
                hideKeyboard(this)
                if (isValidate()) {
                    viewModel.addPostAPI(
                        token!!,
                        userId!!,
                        selectedUserIds!!,
                        binding.etGroupName.text.toString().trim(),
                        binding.etAbout.text.toString().trim(),
                        profileImageFile
                    )
                }
            }
            binding.tvAddCover -> {
                askPermission()
            }
        }
    }

    private fun isValidate(): Boolean {
        when {
            binding.etGroupName.text.isNullOrEmpty() -> {
                showSnackBar(binding.root, getString(R.string.error_empty_group_name))
                return false
            }
            binding.etAbout.text.isNullOrEmpty() -> {
                showSnackBar(binding.root, getString(R.string.error_empty_group_about))
                return false
            }
            selectedUserIds.isNullOrEmpty() -> {
                showSnackBar(binding.root, getString(R.string.error_group_members))
                return false
            }
        }
        return true
    }

    private fun askPermission() {
        permissionsUtil = PermissionsUtil(this)
        permissionsUtil?.askPermissions(this, PermissionsUtil.CAMERA, PermissionsUtil.STORAGE,
            object : PermissionsUtil.PermissionListener {
                override fun onPermissionResult(isGranted: Boolean) {
                    if (isGranted) {
                        ImagePickerUtil.selectImage(
                            this@CreateGroupActivity,
                            { imageFile, _ ->
                                profileImageFile = imageFile
                                Glide.with(binding.imgProfilePic).load(imageFile).centerCrop()
                                    .into(binding.imgProfilePic)
                            },
                            "user",
                            true, false, false,false
                        )
                    }
                }

            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsUtil?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImagePickerUtil.onActivityResult(requestCode, resultCode, data)
    }
}