package com.app.spark.activity.music_gallery

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.CodeBoy.MediaFacer.AudioGet
import com.CodeBoy.MediaFacer.MediaFacer
import com.CodeBoy.MediaFacer.PictureGet
import com.CodeBoy.MediaFacer.mediaHolders.audioContent
import com.app.spark.R
import com.app.spark.activity.add_post.AddPostActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ActivityAddPostGalleryBinding
import com.app.spark.databinding.ActivityAddPostMusicGalleryBinding
import com.app.spark.interfaces.OnItemSelectedInterface
import com.app.spark.utils.GridSpacingItemDecoration
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


class MusicGalleryActivity : AppCompatActivity(), OnItemSelectedInterface {

    lateinit var binding: ActivityAddPostMusicGalleryBinding
    var allAudios: ArrayList<audioContent>? = null
    private var player: SimpleExoPlayer? = null
    private var selectedPosition: Int = 0
    private var mediaType: String = "audio"
    private lateinit var permissionsUtil: PermissionsUtil
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_post_music_gallery)
        permissionsUtil = PermissionsUtil(this)
        askPermission()
        setClickListener()
        setPlayer()
        binding.tvGallery.text = getString(R.string.phone)
        binding.tabLayout.visibility = View.GONE
        //   setTabLayout()
    }

    private fun askPermission() {
        permissionsUtil.askPermission(this,
            PermissionsUtil.STORAGE, object : PermissionsUtil.PermissionListener {
                override fun onPermissionResult(isGranted: Boolean) {
                    if (isGranted) {
                        getAllAudio()
                    }
                }

            })
    }

    private fun setClickListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.imgNext.setOnClickListener {
            if (allAudios != null && allAudios!!.isNotEmpty()) {
                val intent = Intent(this, AddPostActivity::class.java)
                intent.putExtra(IntentConstant.MEDIA_TYPE, mediaType)
                intent.putExtra(
                    IntentConstant.MEDIA_URI,
                    allAudios?.get(selectedPosition)?.filePath
                )
                startActivityForResult(intent, IntentConstant.REQUEST_CODE)
                onBackPressed()
            }
        }
    }

    private fun setPlayer() {
        player = SimpleExoPlayer.Builder(this).build()
        player!!.playWhenReady = true
        player?.repeatMode = Player.REPEAT_MODE_ONE
        binding.pvVideo.player = player
    }

    /*  private fun setTabLayout() {
          binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
              override fun onTabSelected(tab: TabLayout.Tab?) {
                  if (tab?.position == 1) {
                      binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
                  }
              }

              override fun onTabUnselected(tab: TabLayout.Tab?) {
              }

              override fun onTabReselected(tab: TabLayout.Tab?) {
              }

          })
      }
  */
    private fun getAllAudio() {
        allAudios = MediaFacer
            .withAudioContex(this)
            .getAllAudioContent(AudioGet.externalContentUri)
        if (allAudios != null) {
            binding.rvGallery.addItemDecoration(GridSpacingItemDecoration(3, 20, true, 0))
            binding.rvGallery.adapter = MusicGalleryAdapter(this, allAudios!!, this)
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
        binding.pvVideo.visibility = View.GONE
        binding.imgImage.visibility = View.VISIBLE
        playAudio(allAudios!![position].filePath)
    }

    /*
     Play audio Url
               */
    private fun playAudio(videoUrl: String) {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, getString(R.string.app_name))
        )
        // This is the MediaSource representing the media to be played.
        val videoSource: MediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())
                .createMediaSource(Uri.parse(videoUrl))
        // Prepare the player with the source.
        player!!.prepare(videoSource, true, false)
    }

    override fun onBackPressed() {
        player?.stop()
        player?.release()
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentConstant.REQUEST_CODE && resultCode == RESULT_OK) {
            finish()
        }
    }
}
