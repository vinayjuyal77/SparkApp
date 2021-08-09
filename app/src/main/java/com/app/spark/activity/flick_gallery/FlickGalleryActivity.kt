package com.app.spark.activity.flick_gallery

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.CodeBoy.MediaFacer.MediaFacer
import com.CodeBoy.MediaFacer.VideoGet
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.app.spark.R
import com.app.spark.activity.main.MainActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ActivityAddPostGalleryBinding
import com.app.spark.interfaces.OnItemSelectedInterface
import com.app.spark.utils.GridSpacingItemDecoration
import com.app.spark.utils.ImagePickerUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cancan.Utility.PermissionsUtil
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.tabs.TabLayout
/*import com.gowtham.library.utils.TrimType
import com.gowtham.library.utils.TrimVideo*/
import java.io.File
import java.net.URLConnection


class FlickGalleryActivity : AppCompatActivity(), OnItemSelectedInterface {

    lateinit var binding: ActivityAddPostGalleryBinding
    var allVideos: ArrayList<videoContent>? = null
    private var player: SimpleExoPlayer? = null
    private var selectedPosition: Int = 0
    private var mediaType: String = "video"
    private var mediaUri: String? = null
    private lateinit var permissionsUtil: PermissionsUtil
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_post_gallery)
        permissionsUtil = PermissionsUtil(this)
        askPermission()
        setClickListener()
        setPlayer()
        setTabLayout()
    }

    private fun askPermission() {
        permissionsUtil.askPermissions(this,PermissionsUtil.CAMERA,
            PermissionsUtil.STORAGE, object : PermissionsUtil.PermissionListener {
                override fun onPermissionResult(isGranted: Boolean) {
                    if (isGranted) {
                        getAllAudio()
                    }else{
                        askPermission()
                    }
                }

            })
    }

    private fun setClickListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.imgNext.setOnClickListener {
            if (mediaUri != null) {
               /* TrimVideo.activity(
                    Uri.fromFile(File(mediaUri)).toString()
                )
                    .setTrimType(TrimType.MIN_MAX_DURATION)
                    .setMinToMax(1, 30) //seconds
                    .start(this);*/

                val intent = Intent(this, TrimmerActivity::class.java)
                intent.putExtra("EXTRA_INPUT_URI", Uri.parse(mediaUri))
               // intent.putExtra("EXTRA_INPUT_URI", Uri.fromFile(File(mediaUri)).toString())
                startActivity(intent)
            }
        }
    }

    private fun setPlayer() {
        player = SimpleExoPlayer.Builder(this).build()
        player!!.playWhenReady = true
        player?.repeatMode = Player.REPEAT_MODE_ONE
        binding.pvVideo.player = player
    }

    private fun setTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 1) {
                    ImagePickerUtil.videoFromCamera(
                        this@FlickGalleryActivity,
                        { imageFile, tag ->
                            mediaUri = imageFile?.path
                            binding.pvVideo.visibility = View.VISIBLE
                            binding.imgImage.visibility = View.INVISIBLE
                            playAudio(mediaUri!!)
                        },
                        "flick_${System.currentTimeMillis()}"
                    )
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
    }

    private fun getAllAudio() {
        allVideos = MediaFacer
            .withVideoContex(this)
            .getAllVideoContent(VideoGet.externalContentUri)
        allVideos?.addAll(
            MediaFacer
                .withVideoContex(this)
                .getAllVideoContent(VideoGet.internalContentUri)
        )
        if (allVideos != null) {
            binding.rvGallery.addItemDecoration(GridSpacingItemDecoration(3, 20, true, 0))
            binding.rvGallery.adapter = FlickGalleryAdapter(this, allVideos!!, this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onItemSelected(position: Int, totalSize: Int?) {
        player?.stop()
        selectedPosition = position
        binding.pvVideo.visibility = View.VISIBLE
        binding.imgImage.visibility = View.INVISIBLE
        mediaUri=allVideos!![position].path
        playAudio(allVideos!![position].path)
    }

    /*
     Play audio Url
               */
    private fun playAudio(videoUrl: String) {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, getString(R.string.app_name))
        )
        binding.pvVideo.player = player
        // This is the MediaSource representing the media to be played.
        val videoSource: MediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())
                .createMediaSource(Uri.parse(videoUrl))
        // Prepare the player with the source.
        player!!.prepare(videoSource, true, false)

    }

    override fun onPause() {
        player?.playWhenReady = false
        super.onPause()
    }

    override fun onResume() {
        player?.playWhenReady = true
        super.onResume()
    }

    override fun onBackPressed() {
        player?.stop()
        player?.release()
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentConstant.REQUEST_CODE && resultCode == RESULT_OK) {
           // onBackPressed()
            var ins=Intent(this@FlickGalleryActivity,MainActivity::class.java)
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

    private fun onVideoTrim(uri: String) {
        Log.d(this::class.java.simpleName, "Trimmed path::$uri")
        val intent = Intent(this, AddFlickActivity::class.java)
        intent.putExtra(IntentConstant.MEDIA_TYPE, mediaType)
        intent.putExtra(IntentConstant.MEDIA_URI, mediaUri)
        startActivityForResult(intent, IntentConstant.REQUEST_CODE)
    }
}
