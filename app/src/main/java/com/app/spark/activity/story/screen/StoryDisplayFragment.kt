package com.app.spark.activity.story.screen

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.spark.R
import com.app.spark.activity.story.customview.StoriesProgressView
import com.app.spark.activity.story.dataClass.StoryResult
import com.app.spark.activity.story.dataClass.StoryStories
import com.app.spark.activity.story.utils.OnSwipeTouchListener
import com.app.spark.activity.story.utils.hide
import com.app.spark.activity.story.utils.show
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.fragment_story_display.*
import java.text.SimpleDateFormat
import java.util.*

class StoryDisplayFragment : Fragment(),
    StoriesProgressView.StoriesListener {

    private val position: Int by
    lazy { arguments?.getInt(EXTRA_POSITION) ?: 0 }

//    private val storyUser: StoryUser by
//    lazy {
//        (arguments?.getParcelable<StoryUser>(
//            EXTRA_STORY_USER
//        ) as StoryUser)
//    }

    private val storyUser: StoryResult by
    lazy {
        (arguments?.getParcelable<StoryResult>(
            EXTRA_STORY_USER
        ) as StoryResult)
    }

//    private val stories: ArrayList<Story> by
//    lazy { storyUser.stories }

    private val stories: ArrayList<StoryStories> by
    lazy { storyUser.stories }

    private var simpleExoPlayer: SimpleExoPlayer? = null
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private var pageViewOperator: PageViewOperator? = null
    private var counter = 0
    private var pressTime = 0L
    private var limit = 500L
    private var onResumeCalled = false
    private var onVideoPrepared = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_story_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storyDisplayVideo.useController = false
        updateStory()
        setUpUi()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.pageViewOperator = context as PageViewOperator
    }

    override fun onStart() {
        super.onStart()
      //  counter = restorePosition()
    }

    override fun onResume() {
        super.onResume()
        onResumeCalled = true

            if (stories[counter].isVideo() && !onVideoPrepared) {
                simpleExoPlayer?.playWhenReady = false
                return
            }


            simpleExoPlayer?.seekTo(1)
            simpleExoPlayer?.playWhenReady = true
            if (counter == 0) {
                storiesProgressView?.startStories()
            } else {
                // restart animation
                counter =
                    StoryMainActivity.progressState.get(arguments?.getInt(EXTRA_POSITION) ?: 0)
                storiesProgressView?.startStories(counter)
            }

    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayer?.playWhenReady = false
        storiesProgressView?.abandon()
    }

    override fun onComplete() {
        simpleExoPlayer?.release()
        pageViewOperator?.nextPageView()
    }

    override fun onPrev() {
        if (counter - 1 < 0) return
        --counter
        savePosition(counter)
        updateStory()
    }

    override fun onNext() {
        if (stories.size <= counter + 1) {
            return
        }
        ++counter
        savePosition(counter)
        updateStory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        simpleExoPlayer?.release()
    }

    private fun updateStory() {
        simpleExoPlayer?.stop()
        if (stories[counter].isVideo()) {
            storyDisplayVideo.show()
            storyDisplayImage.hide()
            storyDisplayVideoProgress.show()
            initializePlayer()
        } else {
            storyDisplayVideo.hide()
            storyDisplayVideoProgress.hide()
            storyDisplayImage.show()
           // Glide.with(this).load(stories[counter].url).into(storyDisplayImage)
            Glide.with(this).load(stories[counter].story_img).into(storyDisplayImage)
        }

//        val cal: Calendar = Calendar.getInstance(Locale.ENGLISH).apply {
//         //   timeInMillis = stories[counter].storyDate
//            timeInMillis = stories[counter].created_date
//        }
//      //  storyDisplayTime.text = DateFormat.format("MM-dd-yyyy HH:mm:ss", cal).toString()
//        storyDisplayTime.text = DateFormat.format("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", cal).toString()


        var date = stories[counter].created_date
        var spf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val newDate: Date = spf.parse(date)
        spf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        date = spf.format(newDate)
        println(date)

        storyDisplayTime.text = date


    }

    private fun initializePlayer() {
        if (simpleExoPlayer == null) {
         //  simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(requireContext())
            simpleExoPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        } else {
            simpleExoPlayer?.release()
            simpleExoPlayer = null
           // simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(requireContext())
            simpleExoPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        }

 //       mediaDataSourceFactory = CacheDataSourceFactory(
//            StoryApp.simpleCache!!,
//            DefaultHttpDataSourceFactory(
//                Util.getUserAgent(
//                    requireContext(),
//                    Util.getUserAgent(requireContext(), getString(R.string.app_name))
//                )
//            )
//        )
//        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(
//            Uri.parse(stories[counter].url)
//        )


        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            requireActivity(),
            Util.getUserAgent(requireContext(), getString(R.string.app_name))
        )
        // This is the MediaSource representing the media to be played.

                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
         //   Uri.parse(stories[counter].url)
            Uri.parse(stories[counter].story_img)
        )

            simpleExoPlayer?.prepare(mediaSource, false, false)
        if (onResumeCalled) {
            simpleExoPlayer?.playWhenReady = true
        }

        storyDisplayVideo.setShutterBackgroundColor(Color.BLACK)
        storyDisplayVideo.player = simpleExoPlayer

        simpleExoPlayer?.addListener(object : Player.EventListener {
            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error!!)
                storyDisplayVideoProgress.hide()
                if (counter == stories.size.minus(1)) {
                    pageViewOperator?.nextPageView()
                } else {
                    storiesProgressView?.skip()
                }
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                super.onLoadingChanged(isLoading)
                if (isLoading) {
                    storyDisplayVideoProgress.show()
                    pressTime = System.currentTimeMillis()
                    pauseCurrentStory()
                } else {
                    storyDisplayVideoProgress.hide()
                    storiesProgressView?.getProgressWithIndex(counter)
                        ?.setDuration(simpleExoPlayer?.duration ?: 8000L)
                    onVideoPrepared = true
                    resumeCurrentStory()
                }
            }
        })
    }

    private fun setUpUi() {
        val touchListener = object : OnSwipeTouchListener(requireActivity()!!) {
            override fun onSwipeTop() {
                Toast.makeText(activity, "onSwipeTop", Toast.LENGTH_LONG).show()
            }

            override fun onSwipeBottom() {
                Toast.makeText(activity, "onSwipeBottom", Toast.LENGTH_LONG).show()
            }

            override fun onClick(view: View) {
                when (view) {
                    next -> {
                        if (counter == stories.size - 1) {
                            pageViewOperator?.nextPageView()
                        } else {
                            storiesProgressView?.skip()
                        }
                    }
                    previous -> {
                        if (counter == 0) {
                            pageViewOperator?.backPageView()
                        } else {
                            storiesProgressView?.reverse()
                        }
                    }
                }
            }

            override fun onLongClick() {
                hideStoryOverlay()
            }

            override fun onTouchView(view: View, event: MotionEvent): Boolean {
                super.onTouchView(view, event)
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        pressTime = System.currentTimeMillis()
                        pauseCurrentStory()
                        return false
                    }
                    MotionEvent.ACTION_UP -> {
                        showStoryOverlay()
                        resumeCurrentStory()
                        return limit < System.currentTimeMillis() - pressTime
                    }
                }
                return false
            }
        }
        previous.setOnTouchListener(touchListener)
        next.setOnTouchListener(touchListener)

        storiesProgressView?.setStoriesCountDebug(
            stories.size, position = arguments?.getInt(EXTRA_POSITION) ?: -1
        )
        storiesProgressView?.setAllStoryDuration(4000L)
        storiesProgressView?.setStoriesListener(this)

      //  Glide.with(this).load(storyUser.profilePicUrl).circleCrop().into(storyDisplayProfilePicture)
      //  storyDisplayNick.text = storyUser.username

        Glide.with(this).load(storyUser.user.profile_pic).circleCrop().into(storyDisplayProfilePicture)
        storyDisplayNick.text = storyUser.user.username
    }

    private fun showStoryOverlay() {
        if (storyOverlay == null || storyOverlay.alpha != 0F) return

        storyOverlay.animate()
            .setDuration(100)
            .alpha(1F)
            .start()
    }

    private fun hideStoryOverlay() {
        if (storyOverlay == null || storyOverlay.alpha != 1F) return

        storyOverlay.animate()
            .setDuration(200)
            .alpha(0F)
            .start()
    }

    private fun savePosition(pos: Int) {
        StoryMainActivity.progressState.put(position, pos)
    }

    private fun restorePosition(): Int {
        return StoryMainActivity.progressState.get(position)
    }

    fun pauseCurrentStory() {
        simpleExoPlayer?.playWhenReady = false
        storiesProgressView?.pause()
    }

    fun resumeCurrentStory() {
        if (onResumeCalled) {
            simpleExoPlayer?.playWhenReady = true
            showStoryOverlay()
            storiesProgressView?.resume()
        }
    }

    companion object {
        private const val EXTRA_POSITION = "EXTRA_POSITION"
        private const val EXTRA_STORY_USER = "EXTRA_STORY_USER"
        fun newInstance(position: Int, story: StoryResult): StoryDisplayFragment {
            return StoryDisplayFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_POSITION, position)
                    putParcelable(EXTRA_STORY_USER, story)
                }
            }
        }
    }
}