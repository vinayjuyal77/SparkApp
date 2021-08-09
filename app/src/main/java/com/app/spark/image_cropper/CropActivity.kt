package com.app.spark.image_cropper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.spark.R
import com.app.spark.image_cropper.CropImageView
import com.app.spark.image_cropper.CropImageView.*
import java.io.File
import java.io.FileOutputStream

class CropActivity : AppCompatActivity() {
    private var mCropImageView: CropImageView? = null
    private var mProgressView: ProgressBar? = null
    private var tvDone: TextView? = null
    private var tag: String? = null
    private var picWidth = 0
    private var picHeight = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.crop_activity)
        mCropImageView = findViewById(R.id.CropImageView)
        mProgressView = findViewById(R.id.crop_progress)
        tvDone = findViewById(R.id.tvDone)
        val tvCancel = findViewById<TextView?>(R.id.tvCancel)
        val mCropImageUri =
            intent.getParcelableExtra<Uri?>("uri")
        tag = intent.getStringExtra("tag")
        picWidth = intent.getIntExtra("picWidth", 0)
        picHeight = intent.getIntExtra("picHeight", 0)
        mCropImageView?.setImageUriAsync(mCropImageUri)
        mCropImageView?.setOnSetImageUriCompleteListener(object : OnSetImageUriCompleteListener {
            override fun onSetImageUriComplete(view: CropImageView?, uri: Uri?, error: Exception?) {
                tvDone?.visibility = View.VISIBLE
                mProgressView?.visibility = View.GONE
                mCropImageView?.visibility = View.VISIBLE
            }
        })
        mCropImageView?.setOnCropImageCompleteListener(object : OnCropImageCompleteListener {
            override fun onCropImageComplete(view: CropImageView?, result: CropResult?) {
                val file = writeToFile(this@CropActivity, result?.getBitmap())
                val intent = Intent()
                intent.putExtra("uri", file?.absolutePath)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        })
        tvDone?.setOnClickListener {
            mCropImageView?.getCroppedImageAsync(
                picWidth,
                picHeight,
                RequestSizeOptions.RESIZE_FIT
            )
        }
        tvCancel?.setOnClickListener { finish() }
    }

    private fun writeToFile(context: Context?, bitmap: Bitmap?): File? {

        val selectedFile: File?
        try {
            selectedFile = File(context?.cacheDir, "$tag.jpg")
            val outputStream = FileOutputStream(selectedFile,false)
            bitmap?.compress(CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {

            return null

        }

        return selectedFile


    }
}