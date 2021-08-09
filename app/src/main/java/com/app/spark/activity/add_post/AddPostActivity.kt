package com.app.spark.activity.add_post

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityAddPostBinding
import com.app.spark.dialogs.CreateConnectionTypeDialog
import com.app.spark.interfaces.OnConnectionTypeSelected
import com.app.spark.models.ImportantDataResult
import com.app.spark.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_confirm_post.*

class AddPostActivity : BaseActivity(), View.OnClickListener, OnConnectionTypeSelected {
    lateinit var binding: ActivityAddPostBinding
    lateinit var viewModel: AddPostViewModel
    private var postType: String = "public"
    private var mediaType: String? = null
    private var mimeType: String? = null
    private var mediaUri: String? = null
    private var loginDetails: ImportantDataResult? = null
    lateinit var pref: SharedPrefrencesManager
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_post)
        initlizeViewModel()
        pref = SharedPrefrencesManager.getInstance(this)
        loginDetails = Gson().fromJson(
            pref.getString(PrefConstant.LOGIN_RESPONSE, ""),
            ImportantDataResult::class.java
        )
        setClickListener(this)
        mediaType = intent.getStringExtra(IntentConstant.MEDIA_TYPE)
        mimeType = intent.getStringExtra(IntentConstant.MIME_TYPE)
        mediaUri = intent.getStringExtra(IntentConstant.MEDIA_URI)
        binding.tvFullName.text = loginDetails?.name
        binding.tvUserName.text = "@${loginDetails?.username}"
        // check user image

        if(mediaType!=null && mediaType=="photo")
        {
            /*Glide.with(this)
                .load(mediaUri.toString())
                .apply(RequestOptions().centerCrop())
                .placeholder(R.color.gray_aeaeae)
                .into(binding.shapeableImageView)*/
            binding.shapeableImageView.setImageURI(Uri.parse(mediaUri))
        }
        else if(mediaType=="audio") {
            binding.shapeableImageView.setImageDrawable(getDrawable(R.drawable.ic_baseline_audiotrack_24))
            binding.shapeableImageView.setColorFilter(getColor(R.color.white))
        }
        else
        {
            getFileDuration(this, binding.shapeableImageView, mediaUri!!)
          //  setThumbnailFromUrl(this, binding.shapeableImageView, mediaUri!!)
        }


        if (pref.getString(PrefConstant.PROFILE_PIC,"").toString().trim().isNotEmpty()) {
            Glide.with(this)
                .load(pref.getString(PrefConstant.PROFILE_PIC,""))
                .apply(RequestOptions().centerCrop())
                .placeholder(R.drawable.ic_profile)
                .into(binding.imgProfilePic)
        } else {
            Glide.with(this)
                .load(R.drawable.ic_bg_edit_profile)
                .apply(RequestOptions().centerCrop())
                .into(binding.imgProfilePic)
        }

    }



    private fun setClickListener(onClickListener: View.OnClickListener?) {
        binding.btnPost.setOnClickListener(onClickListener)
        binding.tvBack.setOnClickListener(this)
        binding.tvPostType.setOnClickListener(onClickListener)
        binding.edtPost.addTextChangedListener {
            if (it?.length ?: 0 > 0) {
                binding.tvChar.text = "${it!!.length}/1000"
            }
        }
    }

    private fun initlizeViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                AddPostViewModel::class.java
            )
        viewModel.response.observe(this, { res: String? ->
            hideView(binding.progressBar)
            setClickListener(this)
            if (!res.isNullOrEmpty()) {
                setResult(RESULT_OK)
                finish()
            }
        })

        viewModel.errString.observe(this, { err: String? ->
            hideView(binding.progressBar)
            setClickListener(this)
            binding.edtPost.isEnabled = true
            if (err != null)
                showToastShort(this, err)
        })

        viewModel.errRes.observe(this, { err: Int ->
            binding.progressBar.visibility = View.GONE
            binding.edtPost.isEnabled = true
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.tvBack -> finish()
            binding.btnPost -> {
              //  if (binding.edtPost.text.toString().trim().isNotEmpty()) {
                    confirmPostDialog()
//                } else {
//                    showSnackBar(binding.root, getString(R.string.empty_post_info))
//                }
            }
            binding.tvPostType -> {
                var tempString = postType
                when {
                    postType
                        .equals(resources.getString(R.string.public_name), true) -> {
                        CreateConnectionTypeDialog(1, this, this, "").show()
                    }
                    postType
                        .equals(resources.getString(R.string.professional), true) -> {
                        CreateConnectionTypeDialog(2, this, this, "").show()
                    }
                    postType
                        .equals(resources.getString(R.string.personal), true) -> {
                        CreateConnectionTypeDialog(3, this, this, "").show()
                    }
                    postType.trim().isEmpty()-> {
                        CreateConnectionTypeDialog(1, this, this, "").show()
                    }
                    else -> {
                        CreateConnectionTypeDialog(4, this, this, tempString).show()
                    }
                }

            }
        }
    }

    override fun onSelectedConnection(type: String) {
        if(type.trim().isEmpty()){
            this.postType = "public"
            binding.tvPostType.text = "public"
        }else{
            this.postType = type
            binding.tvPostType.text = type
        }

    }

    private fun confirmPostDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_post)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        dialog.tvCancel.setOnClickListener {
            dialog.dismiss()

        }
        dialog.tvPost.setOnClickListener {
            dialog.dismiss()
//            if (binding.edtPost.text.toString().trim().isEmpty()) {
//                showToastLong(this@AddPostActivity, getString(R.string.empty_post_info))
//            } else {
                binding.progressBar.visibility = View.VISIBLE
                setClickListener(null)
                binding.edtPost.isEnabled = false
                viewModel.addPostAPI(
                    pref.getString(PrefConstant.ACCESS_TOKEN, "")!!,
                    pref.getString(PrefConstant.USER_ID, "")!!,
                    binding.edtPost.text.toString(),
                    postType,
                    mediaType!!,
                    mediaUri!!
                )
      //      }
        }
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
    }

}