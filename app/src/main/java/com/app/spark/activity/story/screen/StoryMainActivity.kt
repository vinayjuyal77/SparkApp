package com.app.spark.activity.story.screen

import android.animation.Animator
import android.animation.ValueAnimator
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.story.customview.StoryPagerAdapter
import com.app.spark.activity.story.dataClass.StoryResult
import com.app.spark.activity.story.utils.CubeOutTransformer
import com.app.spark.activity.story.utils.StoryViewModel
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheWriter
import com.google.android.exoplayer2.upstream.cache.CacheWriter.DEFAULT_BUFFER_SIZE_BYTES
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.story_main_activity.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class StoryMainActivity : AppCompatActivity(),
    PageViewOperator {

    private lateinit var pagerAdapter: StoryPagerAdapter
    private var currentPage: Int = 0


    lateinit var viewModel: StoryViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.story_main_activity)
       // setUpPager()
        initlizeViewModel()

     //   viewModel.storyListing(0)
        if(intent.extras!=null) {

            val list   = intent.extras!!.getParcelableArrayList<StoryResult>("Stories")!!
            setUpPager(list, intent.extras!!.getInt("count",0))
        }

    }



    private fun initlizeViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                StoryViewModel::class.java
            )
        viewModel.storyResponse.observe(this, {


            if(it.result.size>0)
            {

            }


        })
    }

    override fun backPageView() {
        if (viewPager.currentItem > 0) {
            try {
                fakeDrag(false)
            } catch (e: Exception) {
                //NO OP
            }
        }
    }

    override fun nextPageView() {
        if (viewPager.currentItem + 1 < viewPager.adapter?.count ?: 0) {
            try {
                fakeDrag(true)
            } catch (e: Exception) {
                //NO OP
            }
        } else {
            //there is no next story
            Toast.makeText(this, "All stories displayed.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setUpPager(storyUserList :  ArrayList<StoryResult>, count : Int) {
       // val storyUserList = StoryGenerator.generateStories()
        preLoadStories(storyUserList)

        pagerAdapter = StoryPagerAdapter(
            supportFragmentManager,
            storyUserList
        )
        viewPager.adapter = pagerAdapter
        viewPager.currentItem = count
        viewPager.setPageTransformer(
            true,
            CubeOutTransformer()
        )
        viewPager.addOnPageChangeListener(object : PageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
            }

            override fun onPageScrollCanceled() {
                currentFragment()?.resumeCurrentStory()
            }
        })
    }

    //private fun preLoadStories(storyUserList: ArrayList<StoryUser>) {
    private fun preLoadStories(storyUserList: ArrayList<StoryResult>) {
        val imageList = mutableListOf<String>()
        val videoList = mutableListOf<String>()

//        storyUserList.forEach { storyUser ->
//            storyUser.stories.forEach { story ->
//                if (story.isVideo()) {
//                    videoList.add(story.url)
//                } else {
//                    imageList.add(story.url)
//                }
//            }
//        }


        storyUserList.forEach { storyUser ->
            storyUser.stories.forEach { story ->
                if (story.isVideo()) {
                    videoList.add(story.story_img)
                } else {
                    imageList.add(story.story_img)
                }
            }
        }
        preLoadVideos(videoList)
        preLoadImages(imageList)
    }

    private fun preLoadVideos(videoList: MutableList<String>) {
        videoList.map { data ->
            GlobalScope.async {
                val dataUri = Uri.parse(data)
                val dataSpec = DataSpec(dataUri, 0, 500 * 1024, null)
                val dataSource: DataSource =
                    DefaultDataSourceFactory(
                        applicationContext,
                        Util.getUserAgent(applicationContext, getString(R.string.app_name))
                    ).createDataSource()

                val listener = CacheWriter.ProgressListener { requestLength: Long, bytesCached: Long, _: Long ->
                        val downloadPercentage = (bytesCached * 100.0
                                / requestLength)
                        Log.d("preLoadVideos", "downloadPercentage: $downloadPercentage")
                    }


                try {
//                    CacheUtil.cache(
//                        dataSpec,
//                        StoryApp.simpleCache,
//                        CacheUtil.DEFAULT_CACHE_KEY_FACTORY,
//                        dataSource,
//                        listener,
//                        null
//                    )
                    CacheWriter(
                        dataSource as CacheDataSource,
                        dataSpec,
                        ByteArray(DEFAULT_BUFFER_SIZE_BYTES),
                        listener

                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun preLoadImages(imageList: MutableList<String>) {
        imageList.forEach { imageStory ->
            Glide.with(this).load(imageStory).preload()
        }
    }

    private fun currentFragment(): StoryDisplayFragment? {
        return pagerAdapter.findFragmentByPosition(viewPager, currentPage) as StoryDisplayFragment
    }

    /**
     * Change ViewPage sliding programmatically(not using reflection).
     * https://tech.dely.jp/entry/2018/12/13/110000
     * What for?
     * setCurrentItem(int, boolean) changes too fast. And it cannot set animation duration.
     */
    private var prevDragPosition = 0

    private fun fakeDrag(forward: Boolean) {
        if (prevDragPosition == 0 && viewPager.beginFakeDrag()) {
            ValueAnimator.ofInt(0, viewPager.width).apply {
                duration = 400L
                interpolator = FastOutSlowInInterpolator()
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {}

                    override fun onAnimationEnd(animation: Animator?) {
                        removeAllUpdateListeners()
                        if (viewPager.isFakeDragging) {
                            viewPager.endFakeDrag()
                        }
                        prevDragPosition = 0
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        removeAllUpdateListeners()
                        if (viewPager.isFakeDragging) {
                            viewPager.endFakeDrag()
                        }
                        prevDragPosition = 0
                    }

                    override fun onAnimationStart(p0: Animator?) {}
                })
                addUpdateListener {
                    if (!viewPager.isFakeDragging) return@addUpdateListener
                    val dragPosition: Int = it.animatedValue as Int
                    val dragOffset: Float =
                        ((dragPosition - prevDragPosition) * if (forward) -1 else 1).toFloat()
                    prevDragPosition = dragPosition
                    viewPager.fakeDragBy(dragOffset)
                }
            }.start()
        }
    }


    override fun onBackPressed() {
        finish();

    }

    companion object {
        val progressState = SparseIntArray()
    }
}
