package com.app.spark.activity.flick_gallery

import android.app.Activity
import android.app.Dialog
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityAddPostBinding
import com.app.spark.dialogs.ConnectionTypeDialog
import com.app.spark.interfaces.OnConnectionTypeSelected
import com.app.spark.models.ImportantDataResult
import com.app.spark.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_confirm_post.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream

class AddFlickActivity : BaseActivity(), View.OnClickListener, OnConnectionTypeSelected {
    lateinit var binding: ActivityAddPostBinding
    lateinit var viewModel: AddFlickViewModel
    private var postType: String = "public"
    private var mediaType: String? = null
    private var mediaUri: String? = null
    private var loginDetails: ImportantDataResult? = null
    lateinit var pref: SharedPrefrencesManager
    private var isFlickUploading = false
    lateinit var imageFile : File
    lateinit var imageFile_path : String

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
        binding.tvPost.text = getString(R.string.flick)
        mediaType = intent.getStringExtra(IntentConstant.MEDIA_TYPE)
        mediaUri = intent.getStringExtra(IntentConstant.MEDIA_URI)
        binding.tvFullName.text = loginDetails?.name
        binding.tvUserName.text = "@${loginDetails?.username}"
     //   binding.tvPostType.visibility = View.GONE




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
         //   binding.shapeableImageView.setColorFilter(getColor(R.color.white))
        }
        else
        {
            getFileDuration(this, binding.shapeableImageView, mediaUri!!)
            //  setThumbnailFromUrl(this, binding.shapeableImageView, mediaUri!!)
        }

        // check user image
        if (loginDetails?.profile_pic.toString().trim().isNotEmpty()) {
            Glide.with(this)
                .load(loginDetails?.profile_pic)
                .apply(RequestOptions().centerCrop())
                .placeholder(R.drawable.ic_profile)
                .into(binding.imgProfilePic)
        } else {
            Glide.with(this)
                .load(R.drawable.ic_profile)
                .apply(RequestOptions().centerCrop())
                .into(binding.imgProfilePic)
        }
    }

    private fun setClickListener(onClickListener: View.OnClickListener?) {
        binding.btnPost.setOnClickListener(onClickListener)
        binding.tvBack.setOnClickListener(onClickListener)
        binding.tvPostType.setOnClickListener(onClickListener)
        binding.edtPost.addTextChangedListener {
            if (it?.length ?: 0 > 0) {
                binding.tvChar.text = "${it!!.length}/1000"
            }
        }
    }


     fun  getFileDuration(context : Activity, shapeableImageView: ShapeableImageView, file :String) {
         var result = null;
         var retriever: MediaMetadataRetriever? = null;
         var inputStream: FileInputStream? = null;

         try {
             retriever = MediaMetadataRetriever()
             inputStream = FileInputStream(file)
             retriever.setDataSource(inputStream.getFD());
             val bmFrame = retriever.getFrameAtTime(1000) //unit in microsecond
             context?.runOnUiThread {
                 shapeableImageView.setImageBitmap(bmFrame)
             }

             persistImage(bmFrame!!,"flick1" )
         } catch (e: java.lang.Exception) {
         }
     }


    private fun persistImage(bitmap: Bitmap, name: String) {
        val filesDir: File = getFilesDir()!!
        imageFile = File(filesDir, "$name.png")
        val os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 60, os)
            os.flush()
            os.close()
           imageFile_path =   imageFile.path.toString()
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error writing bitmap", e)
        }
    }




    private fun initlizeViewModel() {

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                AddFlickViewModel::class.java
            )

        viewModel.response.observe(this, { res: String? ->
            hideView(binding.progressBar)
            setClickListener(this)
            isFlickUploading = false
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
            binding.tvBack -> {
                onBackPressed()
            }
            binding.btnPost -> {
                binding.progressBar.visibility = View.VISIBLE
                setClickListener(null)
                isFlickUploading = true
                binding.edtPost.isEnabled = false
                viewModel.addFlickAPI(
                    pref.getString(PrefConstant.ACCESS_TOKEN, "")!!,
                    pref.getString(PrefConstant.USER_ID, "")!!,
                    binding.edtPost.text.toString(),
                    postType,
                    mediaUri!!,
                    imageFile_path
                )
            }
            binding.tvPostType -> {
                ConnectionTypeDialog(true, this, this).show()
            }
        }
    }

    override fun onSelectedConnection(type: String) {
        this.postType = type
        binding.tvPostType.text = type
    }

    private fun confirmPostDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_post)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        dialog.tvCancel.setOnClickListener {
            dialog.dismiss()

        }
        dialog.tvPost.setOnClickListener {

        }
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
    }

    override fun onBackPressed() {
        if (!isFlickUploading)
            super.onBackPressed()
    }
}