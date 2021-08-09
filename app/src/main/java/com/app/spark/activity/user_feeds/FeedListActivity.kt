package com.app.spark.activity.user_feeds

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityFeedListBinding
import com.app.spark.dialogs.SimpleCustomDialog
import com.app.spark.interfaces.OnFeedSelectedInterface
import com.app.spark.interfaces.SimpleDialogListner
import com.app.spark.models.GetFlickResponse
import com.app.spark.models.ImportantDataResult
import com.app.spark.utils.*
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_feed_flick_menu.*

class FeedListActivity : BaseActivity(), OnFeedSelectedInterface, SeekBar.OnSeekBarChangeListener {
    private var token: String? = null
    private lateinit var binding: ActivityFeedListBinding
    private lateinit var viewModel: FeedListViewModel
    private var pref: SharedPrefrencesManager? = null
    private var exoPlayer: SimpleExoPlayer? = null
    private var playerView: PlayerView? = null
    private var seekBar: SeekBar? = null
    private var layoutManager: LinearLayoutManager? = null
    private var feedsAdapter: FeedsListAdapter? = null
    private var globalOtherUserId = 0
    private var selectedPosition: Int? = null
    private var globalPlay = true
    private var handler: Handler? = Handler(Looper.getMainLooper())
    private var dialog: Dialog? = null
    private var loginDetails: ImportantDataResult? = null

    var value_feed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_feed_list)
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                FeedListViewModel::class.java
            )
        pref = SharedPrefrencesManager.getInstance(this)
        token = pref?.getString(PrefConstant.ACCESS_TOKEN, "")
        loginDetails = Gson().fromJson(
            pref?.getString(PrefConstant.LOGIN_RESPONSE, ""),
            ImportantDataResult::class.java
        )
        viewModel.setUserData(
            token = token,
            loginUserId = pref?.getString(PrefConstant.USER_ID, ""),
            userId = intent.getStringExtra(IntentConstant.PROFILE_ID),
            followGroup = intent.getStringExtra(IntentConstant.FOLLOWING_GROUP)
        )
        selectedPosition = intent.getIntExtra(IntentConstant.POSITION, 0)


        binding.progressBar.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        setRecyclerView()
        observePopularProfiles()
    }

    private fun setRecyclerView() {
        if (layoutManager == null)
            this.layoutManager = binding.rvFeed.layoutManager as LinearLayoutManager
        if (exoPlayer == null)
            exoPlayer = SimpleExoPlayer.Builder(this).build()
        recyclerScrollListener()
        swipeRefresh()
    }

    private fun swipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.pagingFeedListingApi(null)
        }
    }

    override fun onResume() {
        super.onResume()
        exoPlayer?.playWhenReady = true
      //  binding.shimmerLayout.startShimmer();
    }

    private fun observePopularProfiles() {
        viewModel.feedsList.observe(this, Observer {
            binding.progressBar.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false

            if (!it.isNullOrEmpty()) {
                if (feedsAdapter == null) {
                    feedsAdapter = FeedsListAdapter(this, it.toMutableList(), this)
                    binding.rvFeed.adapter = feedsAdapter

                    // updatePlayPauseUi()


                } else {
                    if (globalOtherUserId != 0) {
                        feedsAdapter?.updateList(it)
                    }
                    if (!viewModel.getPaging())
                        feedsAdapter?.updateList(it)
                    else {
                        feedsAdapter?.pagingList(it)
                    }
                }
                viewModel.isLoading = false


                if(!value_feed) {
                    if (selectedPosition != null) {
                        if ((selectedPosition!! < feedsAdapter!!.list.size)) {
                            binding.rvFeed.scrollToPosition(selectedPosition!!)
                            selectedPosition == null
                            binding.swipeRefresh.visibility = View.VISIBLE
                            binding.tvNamaste.visibility = View.GONE
                            binding.tvWeWelcomeYou.visibility = View.GONE
                            binding.tvFollowPeoplePages.visibility = View.GONE
                            value_feed = true
                        } else {
                            viewModel.pagingFeedListingApi(feedsAdapter!!.list.size)
                        }
                    }
                }
            }


//            binding.shimmerLayout.stopShimmer();
//            binding.shimmerLayout.visibility = View.GONE
        })


        viewModel.errString.observe(this, Observer { err: String ->
            binding.progressBar.visibility = View.GONE
            if (!err.isNullOrEmpty())
                showSnackBar(binding.root, err)
        })

        viewModel.errStringFeed.observe(this, Observer { err: String ->
            binding.progressBar.visibility = View.GONE
            /* if (!err.isNullOrEmpty())
                 showSnackBar(binding.root, err)*/
        })

        viewModel.errRes.observe(this, Observer{ err: Int ->
            binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })

        viewModel.deleteFeed.observe(this, Observer {
            if (it.statusCode == 200) {
                showToastLong(this, it.APICODERESULT)
                if (isNetworkAvailable(this))
                    viewModel.pagingFeedListingApi(null)
            }
        })
    }

    private fun setPlayer(videoUrl: String, playerView: PlayerView?) {
        exoPlayer?.playWhenReady = globalPlay
        exoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
        playerView?.player = exoPlayer
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, getString(R.string.app_name))
        )
        // This is the MediaSource representing the media to be played.
        val videoSource: MediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())
                .createMediaSource(Uri.parse(videoUrl))
        // Prepare the player with the source.
        exoPlayer?.prepare(videoSource, true, false)
        setProgress()
    }

    private fun updatePlayPauseUi() {
        var items = feedsAdapter?.list
        if (!items.isNullOrEmpty()) {
            for (i in 0 until items.size) {
                if (globalPlay)
                    items[i].isPlays = "1"
                else
                    items[i].isPlays = "0"
            }
            feedsAdapter?.notifyDataSetChanged()
        }
    }

    override fun onPlayPause(isPlay: Int) {
        globalPlay = isPlay == 1
        updatePlayPauseUi()
        exoPlayer?.playWhenReady = !exoPlayer?.playWhenReady!!
    }

    override fun showPostMenu(
        userId: String,
        postId: String,
        name: String,
        profilePic: String?,
        item: String
    ) {
        postMenuDialog(userId, postId, name, profilePic, item)
    }

    override fun onLike(postId: String, isLike: Boolean) {
        viewModel.likeUnlikeApi(postId, isLike)
    }

    override fun onFlickListFollow(
        item: GetFlickResponse.Result,
        data: String
    ) {
        // in this page we not need to implement this method
    }

    override fun sendExoPlayer(list : ArrayList<SimpleExoPlayer>, exoPlayer: SimpleExoPlayer, position: Int, playerView: PlayerView) {

    }

    var currentPosition: Int = 0
    var duration: Int = 0
    private fun setProgress() {
        seekBar?.progress = 0
        seekBar?.max = exoPlayer?.duration?.toInt()!! / 1000
        currentPosition = exoPlayer!!.currentPosition.toInt()
        duration = exoPlayer!!.duration.toInt()
        if (handler == null) handler = Handler(Looper.getMainLooper())
        //Make sure you update Seekbar on UI thread
        handler?.post(mUpdateTimeTask)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        handler?.removeCallbacks(mUpdateTimeTask)
        exoPlayer?.seekTo((seekBar.progress * 1000).toLong())
        handler?.postDelayed(mUpdateTimeTask, 1000)
    }

    /*
     * Method to get Progress value.
     * */
    private val mUpdateTimeTask = object : Runnable {
        override fun run() {
            if (exoPlayer != null && exoPlayer!!.playWhenReady) {
                val mDuration = exoPlayer!!.duration.toInt() / 1000
                seekBar?.max = mDuration
                val mCurrentPosition = exoPlayer!!.currentPosition.toInt() / 1000
                seekBar?.progress = mCurrentPosition
                handler?.postDelayed(this, 1000)
            }
        }
    }

    private fun recyclerScrollListener() {
        binding.rvFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // Position of the row that is active
            var activeAdapter = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // Get the index of the first Completely visible item
                val firstCompletelyVisibleItemPosition: Int =
                    layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
                val visibleItemCount: Int = layoutManager?.childCount ?: 0
                val totalItemCount: Int = layoutManager?.itemCount ?: 0
                val pastVisibleItems: Int = layoutManager?.findFirstVisibleItemPosition() ?: 0
                if (pastVisibleItems + visibleItemCount >= totalItemCount && !viewModel.isLoading) {
                    viewModel.pagingFeedListingApi(feedsAdapter?.itemCount)
                }
                // Even if we scroll by a few millimeters the video will start playing from the beginning
                // So we need to check if the new Active row layout position is equal to the current active row layout position
                if (activeAdapter != firstCompletelyVisibleItemPosition) {
                    try {
                        val item = feedsAdapter?.list?.get(firstCompletelyVisibleItemPosition)
                        if (item != null && item.mediaType != "photo") {
                            val url: String = item.postMedia!!

                            seekBar =
                                layoutManager?.findViewByPosition(firstCompletelyVisibleItemPosition)
                                    ?.findViewById(R.id.sbSong) as SeekBar
                            playerView =
                                if (item.mediaType.equals(
                                        "video",
                                        true
                                    )
                                ) layoutManager?.findViewByPosition(
                                    firstCompletelyVisibleItemPosition
                                )?.findViewById(R.id.pvVideo) as PlayerView else null
                            // Start playing the video in Active row layout
                            updatePlayPauseUi()
                            setPlayer(url, playerView)
                        } else {
                            exoPlayer?.playWhenReady = false
                            exoPlayer?.stop()
                        }
                        if (item != null)
                            viewModel.viewCountApi(item.postId!!)
                        // assign this row layout position as active row Layout
                        activeAdapter = firstCompletelyVisibleItemPosition


                    } catch (e: NullPointerException) {
                        // Sometimes you scroll so fast that the views are not attached so it gives a NullPointerException
                    } catch (e: ArrayIndexOutOfBoundsException) {
                    }

                }
            }
        })
    }

    private fun postMenuDialog(
        otherUserId: String,
        reportId: String,
        name: String,
        profilePic: String?,
        postId: String
    ) {
        dialog?.dismiss()
        dialog = Dialog(this)
        dialog!!.setContentView(R.layout.dialog_feed_flick_menu)
        dialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog!!.show()
        if (otherUserId.equals(loginDetails?.user_id, true)) {
            dialog!!.tvReport.visibility = View.GONE
            dialog!!.tvUnfollow.visibility = View.GONE
            dialog!!.tvMute.visibility = View.GONE
            dialog!!.tvEdit.visibility = View.VISIBLE
            dialog!!.tvDelete.visibility = View.VISIBLE
        } else {
            dialog!!.tvReport.visibility = View.VISIBLE
            dialog!!.tvUnfollow.visibility = View.VISIBLE
            dialog!!.tvMute.visibility = View.VISIBLE
            dialog!!.tvEdit.visibility = View.GONE
            dialog!!.tvDelete.visibility = View.GONE
        }
        dialog!!.tvReport.setOnClickListener {
            reportDialog(reportId)
            dialog?.dismiss()
        }
        dialog!!.tvShare.setOnClickListener {
            share(this, "https://www.connectd.com/feed/${reportId}")
            dialog?.dismiss()
        }
        dialog!!.tvCopyLink.setOnClickListener {
            copyText(this, "https://www.connectd.com/feed/${reportId}")
            dialog?.dismiss()
        }
        dialog!!.tvUnfollow.setOnClickListener {
            globalOtherUserId = otherUserId.toInt()
            // viewModel.followUnfollowApi(token!!, userId!!, otherUserId, "Unfollow")
            acceptanceDialog(token!!, loginDetails!!.user_id, otherUserId, "Unfollow", postId)
            dialog?.dismiss()
        }
        dialog!!.tvMute.setOnClickListener {
            globalOtherUserId = otherUserId.toInt()
            // viewModel.followUnfollowApi(token!!, userId!!, otherUserId, "Unfollow")
            acceptanceDialog(token!!, loginDetails?.user_id!!, otherUserId, "Mute", postId)
            dialog?.dismiss()
        }
        dialog!!.tvDelete.setOnClickListener {
            globalOtherUserId = otherUserId.toInt()
            // viewModel.followUnfollowApi(token!!, userId!!, otherUserId, "Unfollow")
            acceptanceDialog(token!!, loginDetails?.user_id!!, otherUserId, "Delete", postId)
            dialog?.dismiss()
        }
        dialog!!.llDialogRoot.setOnClickListener {
            dialog!!.dismiss()
        }
        dialog!!.setCancelable(true)
        dialog!!.setCanceledOnTouchOutside(true)
    }

    private fun acceptanceDialog(
        token: String,
        userId: String,
        otherUserId: String,
        type: String,
        postId: String
    ) {
        var desc = ""
        when {
            type.equals("Unfollow", true) -> {
                desc = "Do you want to unfollow this account?\n you can follow them back anytime."
            }
            type.equals("Mute", true) -> {
                desc = "Do you want to mute all the \n posts from  @${loginDetails?.username}?."
            }
            type.equals("Delete", true) -> {
                desc = "Do you want to delete feed?."
            }
        }
        val dialog = SimpleCustomDialog(
            this,
            title = type,
            desc = desc,
            positiveBtnName = type,
            onConnectionTypeSelected = object : SimpleDialogListner {
                override fun submitSelected() {
                    if (isNetworkAvailable(this@FeedListActivity)) {
                        if (type.equals("Unfollow", true)) {
                            viewModel.followUnfollowApi(token, userId, otherUserId, "Unfollow")
                        } else if (type.equals("Delete", true)) {
                            viewModel.deleteFeedApi(token, userId, postId, "post")
                        }
                        dialog?.dismiss()
                    }
                }
            })
        dialog.show()
        dialog.setCancelable(true)
    }

    private fun reportDialog(reportId: String) {
        val dialog = SimpleCustomDialog(
            this,
            title = "Report",
            desc = "Do you want to report this post?",
            positiveBtnName = "Report",
            onConnectionTypeSelected = object : SimpleDialogListner {
                override fun submitSelected() {
                    if (isNetworkAvailable(this@FeedListActivity)) {
                        viewModel.reportPost(
                            token!!,
                            loginDetails!!.user_id,
                            reportId
                        )
                        dialog?.dismiss()
                    }
                }
            })
        dialog.show()
        dialog.setCancelable(true)
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.playWhenReady = false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        exoPlayer?.stop()
        exoPlayer?.release()
    }

}