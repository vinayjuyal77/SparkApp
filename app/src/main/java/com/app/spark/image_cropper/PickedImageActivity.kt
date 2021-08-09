package com.app.spark.image_cropper

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import com.app.spark.R
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Created by Techugo on 9/24/2018.
 */
class PickedImageActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var mProgressView: ProgressBar
    private lateinit var imageUri: Uri
    private lateinit var tvDone: TextView
    private lateinit var tvCancel: TextView
    private  var mFileCaptured: File?=null
    private lateinit var context: Context
    private lateinit var tag: String
    private var picWidth = 0
    private var picHeight = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picked_image_activity)
        context = this
        imageView = findViewById(R.id.imageView)
        mProgressView = findViewById(R.id.crop_progress)
        tvDone = findViewById(R.id.tvDone)
        tvCancel = findViewById(R.id.tvCancel)
        imageUri = intent.getParcelableExtra("uri")!!
        val compressImage = intent.getBooleanExtra("compressImage", false)
        tag = intent.getStringExtra("tag")!!
        picWidth = intent.getIntExtra("picWidth", 500)
        picHeight = intent.getIntExtra("picHeight", 500)
        if (compressImage) {
            Thread(Runnable {
                mFileCaptured = writeToFile(imageUri)
                if (mFileCaptured != null) {
                    mFileCaptured =
                        compressToExistingFile(mFileCaptured?.path, mFileCaptured)!!
                    runOnUiThread {
                        tvDone.visibility = View.VISIBLE
                        tvCancel.visibility = View.VISIBLE
                        mProgressView.visibility = View.GONE
                        if (mFileCaptured != null) imageView.setImageBitmap(
                            BitmapFactory.decodeFile(
                                mFileCaptured?.absolutePath
                            )
                        )
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            context,
                            "Only support jpg and png. Please select another image",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }).start()
        } else {
            Thread(Runnable {
                mFileCaptured = writeToFile(imageUri)
                runOnUiThread {
                    if (mFileCaptured != null) {
                        tvDone.visibility = View.VISIBLE
                        tvCancel.visibility = View.VISIBLE
                        mProgressView.visibility = View.GONE
                        imageView.setImageBitmap(BitmapFactory.decodeFile(mFileCaptured?.absolutePath))
                    } else {
                        Toast.makeText(
                            context,
                            "Only support jpg and png. Please select another image",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }).start()
        }
        tvDone.setOnClickListener {
            val intent = Intent()
            intent.putExtra("filePath", mFileCaptured?.absolutePath)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        tvCancel.setOnClickListener { finish() }
    }

    private fun writeToFile(fileUri: Uri?): File? {
        var fileName = queryName(context.contentResolver, fileUri)
        val filenameArray: List<String> = fileName!!.split("\\.")
        val extension = filenameArray[filenameArray.size - 1].split(".")
        fileName = "$tag.${if (extension.size > 1) extension[1] else extension[0]}"
        try {
            val input = context.contentResolver.openInputStream(fileUri!!)
            val selectedFile = File(context.cacheDir, fileName)
            val output: OutputStream = FileOutputStream(selectedFile)
            val buffer = ByteArray(4 * 1024) // or other buffer size
            var read: Int
            while (input!!.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
            }
            output.flush()
            output.close()
            input.close()
            if (fileName.contains(".jpg") || fileName.contains(".JPG")
                || fileName.contains(".jpeg") || fileName.contains(".JPEG")
                || fileName.contains(".png") || fileName.contains(".PNG")
            ) {
                var bm = decodeFile(
                    selectedFile.absolutePath,
                    picWidth,
                    picHeight,
                    ScalingLogic.CROP
                )
                bm = fixOrientation(bm, selectedFile.absolutePath)
                val outputStream = FileOutputStream(selectedFile)
                bm!!.compress(CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                return selectedFile
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    private fun compressToExistingFile(
        filePath: String?,
        file: File?
    ): File? {
        try {
            var bm = decodeFile(filePath, picWidth, picHeight, ScalingLogic.CROP)
            bm = fixOrientation(bm, filePath)
            val outputStream = FileOutputStream(file)
            bm!!.compress(CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            return null
        }
        return file
    }

    private fun decodeFile(
        path: String?, dstWidth: Int, dstHeight: Int,
        scalingLogic: ScalingLogic?
    ): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        options.inJustDecodeBounds = false
        options.inSampleSize = calculateSampleSize(
            options.outWidth, options.outHeight, dstWidth,
            dstHeight, scalingLogic
        )
        return BitmapFactory.decodeFile(path, options)
    }

    enum class ScalingLogic {
        CROP, FIT
    }

    private fun calculateSampleSize(
        srcWidth: Int, srcHeight: Int, dstWidth: Int, dstHeight: Int,
        scalingLogic: ScalingLogic?
    ): Int {
        return if (scalingLogic == ScalingLogic.FIT) {
            val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
            val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()
            if (srcAspect > dstAspect) {
                srcWidth / dstWidth
            } else {
                srcHeight / dstHeight
            }
        } else {
            val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
            val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()
            if (srcAspect > dstAspect) {
                srcHeight / dstHeight
            } else {
                srcWidth / dstWidth
            }
        }
    }

    private fun queryName(resolver: ContentResolver?, uri: Uri?): String? {
        return if (uri!!.scheme.equals("content", ignoreCase = true)) {
            val returnCursor = resolver!!.query(uri, null, null, null, null)
            val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor?.moveToFirst()
            val name = returnCursor?.getString(nameIndex!!)
            returnCursor?.close()
                name
        } else {
            val filenameArray: List<String> =
                uri.toString().split("\\/")
            filenameArray[filenameArray.size - 1]
        }
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun fixOrientation(bm: Bitmap?, filePath: String?): Bitmap? {
        return try {
            val ei = ExifInterface(filePath!!)
            if (ei != null) {
                val orientation = ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
                )
                val bitmap = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bm, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bm, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bm, 270f)
                    ExifInterface.ORIENTATION_NORMAL -> bm
                    else -> bm
                }
                bitmap ?: bm
            } else {
                bm
            }
        } catch (e: Exception) {
            e.printStackTrace()
            bm
        }
    }

    private fun rotateImage(source: Bitmap?, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source!!, 0, 0, source.width, source.height, matrix,
            true
        )
    }
}