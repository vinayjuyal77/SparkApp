package com.app.spark.activity.user_flicks

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.spark.R
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityFlickListBinding
import com.app.spark.dialogs.SimpleCustomDialog
import com.app.spark.interfaces.OnFeedSelectedInterface
import com.app.spark.interfaces.SimpleDialogListner
import com.app.spark.models.GetFlickResponse
import com.app.spark.models.ImportantDataResult
import com.app.spark.utils.*
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Renderer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_feed_flick_menu.*

class FlickListActivity : BaseActivity(), OnFeedSelectedInterface {
    private var token: String? = null
    private lateinit var binding: ActivityFlickListBinding
    private lateinit var viewModel: FlickViewModel
    private var pref: SharedPrefrencesManager? = null
    private var exoPlayer: SimpleExoPlayer? = null
    private var playerView: PlayerView? = null
    private var seekBar: SeekBar? = null
    private var layoutManager: LinearLayoutManager? = null
    private var flicksAdapter: FlicksAdapter? = null
    private var globalOtherUserId = 0
    private var selectedPosition: Int? = null
    private var globalPlay = true
    private var handler: Handler? = Handler(Looper.getMainLooper())
    private var dialog: Dialog? = null
    private var loginDetails: ImportantDataResult? = null
    private var fliksResponseList = ArrayList<GetFlickResponse.Result>()


    var value_flick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_flick_list)
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                FlickViewModel::class.java
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
            userId = intent.getStringExtra(IntentConstant.PROFILE_ID)
        )
        binding.progressBar.visibility = View.VISIBLE
        selectedPosition = intent.getIntExtra(IntentConstant.POSITION, 0)
    }

    override fun onStart() {
        super.onStart()
        setInitialData()
        observePopularProfiles()
    }

    private fun setInitialData() {
        if (exoPlayer == null)
            exoPlayer = SimpleExoPlayer.Builder(this).build()
        swipeRefresh()
    }

    private fun swipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.pagingFeedListingApi(null)
        }
        binding.imgBack.setOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        exoPlayer?.playWhenReady = true
    }

    private fun observePopularProfiles() {
        viewModel.flickList.observe(this, Observer {
            binding.progressBar.visibility = View.VISIBLE
            binding.swipeRefresh.isRefreshing = false


            if (!it.isNullOrEmpty()) {
                if (flicksAdapter == null || fliksResponseList.isEmpty()) {
                    fliksResponseList.addAll(it)
                    flicksAdapter = FlicksAdapter(
                        this,
                        fliksResponseList,
                        this
                    )
                    binding.rvFlicks.adapter = flicksAdapter
                    val view: RecyclerView? = binding.rvFlicks.getChildAt(0) as RecyclerView?
                    layoutManager = view?.layoutManager as LinearLayoutManager
                    binding.rvFlicks.registerOnPageChangeCallback(pageChangeCallback)
                } else {

                  flicksAdapter?.updateList(it)
                    //flicksAdapter?.notifyDataSetChanged()
                }

                if(!value_flick) {
                    if (selectedPosition != null) {
                        if ((selectedPosition!! < flicksAdapter!!.list.size)) {
                            //  Handler(Looper.getMainLooper()).postDelayed({
                            binding.rvFlicks.setCurrentItem(selectedPosition!!, false)
                            selectedPosition == null
                            binding.swipeRefresh.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.GONE

                            value_flick = true
                            binding.rvFlicks.registerOnPageChangeCallback(pageChangeCallback)
                            ///   }, 100)
                        } else {
                            viewModel.pagingFeedListingApi(flicksAdapter!!.list.size)
                        }
                    }

                }
                binding.progressBar.visibility = View.GONE

            }
        })


        viewModel.errString.observe(this, { err: String ->
            binding.progressBar.visibility = View.GONE
            if (!err.isNullOrEmpty())
                showSnackBar(binding.root, err)
        })
        viewModel.errRes.observe(this, { err: Int ->
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

           viewModel.resultFollowed.observe(this, Observer {
            if (it.statusCode == 200) {

                showToastLong(this, it.APICODERESULT)
//                if (isNetworkAvailable(requireActivity())) {
//                    if (fliksResponseList.isNotEmpty()) {
//                        fliksResponseList.clear()
//                    }
//                    viewModel.pagingFeedListingApi(0)
//                }
            }
        })


    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            if (position <= flicksAdapter?.itemCount!!) {
                val playerView =
                    layoutManager?.findViewByPosition(position)
                        ?.findViewById(R.id.pvVideo) as PlayerView?
                val imageview =
                    layoutManager?.findViewByPosition(position)
                        ?.findViewById(R.id.imgPost) as ImageView?
                val item = flicksAdapter?.list?.get(position)
                if (playerView != null) {
                   setPlayer(item?.flickMedia!!, playerView ,imageview!!)
                } else {
                    exoPlayer?.playWhenReady = false
                }
                if (position == (flicksAdapter?.itemCount!! - 3)) {
                    viewModel.pagingFeedListingApi(flicksAdapter?.itemCount)
                }
                viewModel.viewCountApi(item?.flickId!!)
            }

        }
    }

    private fun setPlayer(videoUrl: String, playerView: PlayerView?, imageView: ImageView) {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = SimpleExoPlayer.Builder(this).build()
        exoPlayer?.playWhenReady = true
        exoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
        playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        exoPlayer?.videoScalingMode = Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
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

        imageView.fadeOut_image(950)
    }

    inline fun View.fadeOut_image(durationMillis: Long = 400) {
        this.startAnimation(AlphaAnimation(1F, 0F).apply {
            duration = durationMillis
            fillAfter = true
        })
    }


    override fun onPlayPause(isPlay: Int) {
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
        // for flick list item
        if (isNetworkAvailable(this)) {
            if (data.equals(this.getString(R.string.following), true)) {
                viewModel.followUnfollowApi(
                    token!!,
                    loginDetails?.user_id!!,
                    item?.userId,
                    "Unfollow"
                )
            } else if (data.equals(this.getString(R.string.follow), true)) {
                viewModel.followUnfollowApi(
                    token!!,
                    loginDetails?.user_id!!,
                    item?.userId,
                    "Follow"
                )
            }

        }
    }

    override fun sendExoPlayer(list : ArrayList<SimpleExoPlayer>, exoPlayer: SimpleExoPlayer, position: Int, playerView: PlayerView) {

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
            share(this, "https://www.connectd.com/flick/${reportId}")

        }
        dialog!!.tvCopyLink.setOnClickListener {
            copyText(this, "https://www.connectd.com/flick/${reportId}")

        }
        dialog!!.tvUnfollow.setOnClickListener {
            // viewModel.followUnfollowApi(token!!, userId!!, otherUserId, "Unfollow")
            acceptanceDialog(token!!, loginDetails?.user_id!!, otherUserId, "Unfollow", postId)
            dialog?.dismiss()
        }
        dialog!!.tvMute.setOnClickListener {
            acceptanceDialog(token!!, loginDetails?.user_id!!, otherUserId, "Mute", postId)
            dialog?.dismiss()
        }
        dialog!!.tvDelete.setOnClickListener {
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
                desc = "Do you want to mute all the \n posts from  @${loginDetails?.username!!}?."
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
                    if (isNetworkAvailable(this@FlickListActivity)) {
                        if (type.equals("Unfollow", true)) {
                            viewModel.followUnfollowApi(token, userId, otherUserId, "Unfollow")
                        } else if (type.equals("Delete", true)) {
                            viewModel.deleteFeedApi(token, userId, postId, "flick")
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
            desc = "Do you want to report this flick?",
            positiveBtnName = "Report",
            onConnectionTypeSelected = object : SimpleDialogListner {
                override fun submitSelected() {
                    if (isNetworkAvailable(this@FlickListActivity)) {
                        viewModel.reportPost(
                            token!!,
                            loginDetails?.user_id!!,
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