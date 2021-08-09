package com.app.spark.activity.flick_gallery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.spark.R
import com.app.spark.activity.main.MainActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.utils.ImagePickerUtil
import com.lb.video_trimmer_library.interfaces.VideoTrimmingListener
import kotlinx.android.synthetic.main.activity_trimmer.*
import java.io.File

class TrimmerActivity : AppCompatActivity(), VideoTrimmingListener {
//    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimmer)
        val inputVideoUri: Uri? = intent?.getParcelableExtra("EXTRA_INPUT_URI")
        if (inputVideoUri == null) {
            finish()
            return
        }




        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                // No explanation needed; request the permission
                val MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 321
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            }
        } else {

        }

        //setting progressbar
//        progressDialog = ProgressDialog(this)
//        progressDialog!!.setCancelable(false)
//        progressDialog!!.setMessage(getString(R.string.trimming_progress))
        videoTrimmerView.setMaxDurationInMs(36 * 1000)
        videoTrimmerView.setOnK4LVideoListener(this)
      //  val parentFolder = getExternalFilesDir(null)!!
      //  parentFolder.mkdirs()
        var parentFolder: File? = null
        parentFolder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + File.separator + ""
                      /*  + System.currentTimeMillis() + ".mp4"*/
            )
        } else {
            File(
                Environment.getExternalStorageDirectory().toString() + File.separator + ""
                        /*+ System.currentTimeMillis() + ".mp4"*/
            )
        }
        val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"
        val trimmedVideoFile = File(parentFolder, fileName)
        videoTrimmerView.setDestinationFile(trimmedVideoFile)
        videoTrimmerView.setVideoURI(Uri.parse("file://"+inputVideoUri.toString()))
        videoTrimmerView.setVideoInformationVisibility(true)
    }

    override fun onTrimStarted() {
        trimmingProgressView.visibility = View.VISIBLE
    }

    override fun onFinishedTrimming(uri: Uri?) {
        trimmingProgressView.visibility = View.GONE
        if (uri == null) {
            Toast.makeText(this@TrimmerActivity, "failed trimming", Toast.LENGTH_SHORT).show()
        } else {
            val msg = getString(R.string.video_saved_at, uri.path)
       //     Toast.makeText(this@TrimmerActivity, msg, Toast.LENGTH_SHORT).show()
//            val intent = Intent(Intent.ACTION_VIEW, uri)
//            intent.setDataAndType(uri, "video/mp4")
//            startActivity(intent)

            val intent = Intent(this, AddFlickActivity::class.java)
            intent.putExtra(IntentConstant.MEDIA_TYPE, "video")
            intent.putExtra(IntentConstant.MEDIA_URI, uri.path)
            startActivityForResult(intent, IntentConstant.REQUEST_CODE)
        }
       // finish()
    }

    override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
        trimmingProgressView.visibility = View.GONE
        Toast.makeText(this@TrimmerActivity, "error while previewing video", Toast.LENGTH_SHORT).show()
    }

    override fun onVideoPrepared() {
        //        Toast.makeText(TrimmerActivity.this, "onVideoPrepared", Toast.LENGTH_SHORT).show();
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentConstant.REQUEST_CODE && resultCode == RESULT_OK) {
            // onBackPressed()
            var ins=Intent(this@TrimmerActivity,MainActivity::class.java)
            ins.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            ins.putExtra(IntentConstant.PAGE_FLAG,2)
            startActivity(ins)
            finish()
/*        } else if (requestCode == TrimVideo.VIDEO_TRIMMER_REQ_CODE && data != null) {
            onVideoTrim(TrimVideo.getTrimmedVideoPath(data))*/
        }else {
            ImagePickerUtil.onActivityResult(requestCode, resultCode, data)
        }
    }

}
