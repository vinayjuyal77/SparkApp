package com.app.spark.activity.camera.fragment

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import coil.fetch.VideoFrameUriFetcher
import coil.load
import coil.request.ImageRequest
import com.app.spark.R
import com.app.spark.activity.camera.utill_camera.fitSystemWindows
import com.app.spark.activity.camera.utill_camera.getThumnilFromUri
import com.app.spark.constants.AppConstants.BundleConstants.ACTION_INT
import com.app.spark.constants.AppConstants.BundleConstants.BUNDLE
import com.app.spark.databinding.FragmentPreviewBinding
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory


class PreviewFragment : BaseFragment<FragmentPreviewBinding>(R.layout.fragment_preview), Player.EventListener {
    lateinit var data: String
    override val binding: FragmentPreviewBinding by lazy { FragmentPreviewBinding.inflate(layoutInflater) }
    private lateinit var simpleExoplayer: SimpleExoPlayer
    private var playbackPosition: Long = 0

    private val dataSourceFactory: DataSource.Factory by lazy {
        DefaultDataSourceFactory(requireContext(), "CoNeCtEd")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.fitSystemWindows()
        simpleExoplayer = SimpleExoPlayer.Builder(requireContext()).build()
        data= arguments?.getString(BUNDLE)!!
        var actionFor= arguments?.getInt(ACTION_INT)!!
        if(actionFor==1) setGalleryThumbnail(data)
        else if (actionFor==2) initializePlayer(data)

        binding.ivPriviewVideo.setOnClickListener {
            //binding.ivPriviewVideo.visibility=View.GONE
            preparePlayer(data!!)
            simpleExoplayer.playWhenReady = true
        }
    }

    private fun setGalleryThumbnail(uri: String?) {
        binding.imageView.visibility=View.VISIBLE
        binding.exoplayerView.visibility=View.GONE
        binding.ivPriviewVideo.visibility=View.GONE
        binding.imageView.load(Uri.parse(uri)) {
            listener(object : ImageRequest.Listener {
                override fun onError(request: ImageRequest, throwable: Throwable) {
                    super.onError(request, throwable)
                    binding.imageView.load(Uri.parse(uri)) {
                        fetcher(VideoFrameUriFetcher(requireContext()))
                    }
                }
            })
        }
    }
    override fun onStop() {
        super.onStop()
        if(simpleExoplayer!=null) releasePlayer()
    }
    private fun initializePlayer(uri: String?) {
        binding.imageView.visibility=View.GONE
        binding.exoplayerView.visibility=View.VISIBLE
        binding.ivPriviewVideo.visibility=View.GONE
        preparePlayer(uri!!)
        binding.exoplayerView.player = simpleExoplayer
        simpleExoplayer.seekTo(playbackPosition)
        simpleExoplayer.playWhenReady = true
        simpleExoplayer.addListener(this)
    }
    private fun preparePlayer(videoUrl: String) {
        val uri = Uri.parse(videoUrl)
        binding.ivPriviewVideo.setImageBitmap(getThumnilFromUri(uri))
        val mediaSource = buildMediaSource(uri)
        simpleExoplayer.prepare(mediaSource)
    }
    private fun buildMediaSource(uri: Uri): MediaSource {
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }
    private fun releasePlayer() {
        try {
            playbackPosition = simpleExoplayer.currentPosition
            simpleExoplayer.release()
        }catch (e:ExoPlaybackException){e.printStackTrace()}
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        // handle error
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING)
            binding.progressBar.visibility = View.VISIBLE
        /*else if(playbackState == Player.STATE_ENDED)
             binding.ivPriviewVideo.visibility=View.VISIBLE*/
        else if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED)
            binding.progressBar.visibility = View.INVISIBLE
    }



    override fun onBackPressed() {
        view?.let { Navigation.findNavController(it).popBackStack() }
    }

}