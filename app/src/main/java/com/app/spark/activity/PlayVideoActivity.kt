package com.app.spark.activity

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ActivityPlayVideoBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer


class PlayVideoActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlayVideoBinding
    private var player: SimpleExoPlayer? = null
    private var videoUrl: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play_video)
        /*
        Get Video Url from Intent
         */
        videoUrl = intent.getStringExtra(IntentConstant.VIDEO_URL)
        //  TrackSelection.Factory factory = new AdaptiveTrackSelection.Factory();
        player = SimpleExoPlayer.Builder(this).build()
        if (videoUrl != null && videoUrl?.length != 0) playVideo(videoUrl!!)
        player!!.playWhenReady = true
        setPlayerListener()
        binding.mVideoView.player = player
        binding.loader.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (player != null) {
            player!!.playWhenReady = true
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        binding.mVideoView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    override fun onPause() {
        super.onPause()
        if (player != null) {
            player?.playWhenReady = false
        }
    }

    override fun onBackPressed() {
        if (player != null) {
            player?.stop()
            player?.release()
        }
        super.onBackPressed()
    }

    /*
         Play Video Url using VideoView
                 */
    private fun playVideo(videoUrl: String) {
        val videoUri = Uri.parse(videoUrl)
       /* val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this@PlayVideoActivity, getString(R.string.app_name))
        )*/
        val mediaItem = MediaItem.fromUri(videoUri)
        player?.setMediaItem(mediaItem, true)
        player?.prepare()
    }

    private fun setPlayerListener() {
        player?.addListener(object : Player.EventListener {
            override fun onLoadingChanged(isLoading: Boolean) {
                if (isLoading) binding.loader.visibility =
                    View.VISIBLE else binding.loader.visibility = View.GONE
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    // media actually playing
                    binding.loader.visibility = View.GONE
                }
                if (playWhenReady && playbackState == Player.STATE_ENDED) {
                    finish()
                }
            }
        })
    }
}