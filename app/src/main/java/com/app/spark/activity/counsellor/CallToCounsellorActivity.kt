package com.app.spark.activity.counsellor

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.activity.main.MainActivity
import com.app.spark.databinding.ActivityCallToCounsellorBinding
import java.io.IOException
import java.util.*


class CallToCounsellorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallToCounsellorBinding
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val permissions = arrayOf<String>(Manifest.permission.RECORD_AUDIO)
    private var audioRecordingPermissionGranted = false
    private var recorder: MediaRecorder? = null
    private var fileName: String? = null
    private var player: MediaPlayer? = null

    var falgInt=true
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call_to_counsellor)
        binding.activity = this

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        binding.llRecord.setOnClickListener {
            if(falgInt) {
                startRecording()
                binding.ivRecord.setColorFilter(getColor(R.color.yellow_FFA602))
                falgInt=false
            }else {
                stopRecording()
                binding.ivRecord.setColorFilter(getColor(R.color.white))
                falgInt=true
            }
        }

        binding.llHold.setOnClickListener {
            if(falgPlay) playRecording()
            else stopPlaying()
        }
    }
    var falgPlay=true

    private fun startRecording() {
        val uuid: String = UUID.randomUUID().toString()
        fileName = externalCacheDir!!.absolutePath + "/" + uuid + ".3gp"
        Log.i(CallToCounsellorActivity::class.java.simpleName, fileName!!)

        recorder = MediaRecorder()
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder!!.setOutputFile(fileName)
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        try {
            recorder!!.prepare()
        } catch (e: IOException) {
            Log.e(CallToCounsellorActivity::class.java.simpleName + ":startRecording()", "prepare() failed")
        }
        recorder!!.start()
    }
    private fun stopRecording() {
        if (recorder != null) {
            recorder!!.release()
            recorder = null
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> audioRecordingPermissionGranted =
                grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (!audioRecordingPermissionGranted) {
            finish()
        }
    }
    private fun playRecording() {
        player = MediaPlayer()
        try {
            player!!.setDataSource(fileName)
            player!!.setOnCompletionListener(OnCompletionListener { stopPlaying() })
            player!!.prepare()
            player!!.start()
        } catch (e: IOException) {
            Log.e(MainActivity::class.java.simpleName + ":playRecording()", "prepare() failed")
        }
    }
    private fun stopPlaying() {
        if (player != null) {
            player!!.release()
            player = null
        }
    }

}