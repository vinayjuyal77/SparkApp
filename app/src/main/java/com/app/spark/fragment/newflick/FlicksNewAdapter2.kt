package com.app.spark.fragment.newflick

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.UsersProfileActivity
import com.app.spark.activity.post_comments.PostCommentActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ItemAddsFlickBinding
import com.app.spark.databinding.ItemFlicksBinding
import com.app.spark.interfaces.OnFeedSelectedInterface
import com.app.spark.models.GetFlickResponse
import com.app.spark.models.ImportantDataResult
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.date.DateTimeUtils
import com.app.spark.utils.isNetworkAvailable
import com.app.spark.utils.share
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.gson.Gson
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class FlicksNewAdapter2(
    var context: Context,
    var list: MutableList<GetFlickResponse.Result>,
    val itemInterface: OnFeedSelectedInterface? = null,

    ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var adLoader: AdLoader? = null

    var like_count = 0
    var view_count = 0
  ///  var exoPlayer = SimpleExoPlayer.Builder(context).build()
    var exoPlayer: SimpleExoPlayer? = null

    var global_position = 0
    var global_value = "0"
    var listt = ArrayList<SimpleExoPlayer>()



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var binding: ItemFlicksBinding
        var binding2: ItemAddsFlickBinding
        if (viewType == FLICK_LAYOUT) {
            binding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.item_flicks,
                parent,
                false
            )
            return ViewHolder(binding)

        } else if (viewType == ADD_LAYOUT) {
            binding2 = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.item_adds_flick,
                parent,
                false
            )
            return ViewHolder2(binding2)

        }

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_flicks,
            parent,
            false
        )
        return ViewHolder(binding)

    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun getItemViewType(position: Int): Int {


        if (position == 0) {
            return FLICK_LAYOUT

        } else if (position % 8 == 0) {
            return ADD_LAYOUT
        } else {
            return FLICK_LAYOUT
        }

    }


    fun updateList(it: List<GetFlickResponse.Result>) {
        val size = itemCount
        if (list.isNotEmpty()) {
            for (i in list.indices) {
                for (j in it.indices) {
                    if (list[i].flickId.toInt() == it[j].flickId.toInt()) {
                        list[i].isFollower = it[j].isFollower
                        list[i].isfollowing = it[j].isfollowing
                    } else {
                        list.add(it[j])
                    }
                }
            }
        } else {
            list.addAll(it)
        }
        notifyItemRangeInserted(size, it.size)
        /* val diffResult = DiffUtil.calculateDiff(FlicksDiffCallback(it, list))
         diffResult.dispatchUpdatesTo(this)*/
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FlicksNewAdapter2.ViewHolder -> holder.bindData(
                list[position],
                position,
                itemInterface
            )
            is FlicksNewAdapter2.ViewHolder2 -> holder.bindData(
                list[position],
                position,
                itemInterface
            )
            //  is ColleagueViewHolder -> holder.bind(element as ColleagueDataModel)
            else -> throw IllegalArgumentException()
        }
    }


    inner class ViewHolder(var binding: ItemFlicksBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bindData(
            item: GetFlickResponse.Result,
            position: Int,
            itemInterface: OnFeedSelectedInterface?
        ) {
            binding.item = item



               // setPlayer(item.flickMedia, binding.pvVideo, position, item.value)






            val c: Date = Calendar.getInstance().getTime()
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val current_date: String = df.format(c)
            Log.e("current date", current_date);
            Log.e("createondate date", item.createdOn!!);


            val obj = DateTimeUtils()
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

            try {
                val date1 = simpleDateFormat.parse(item.createdOn!!)
                val date2 = simpleDateFormat.parse(current_date)

                if (printDifference(date1, date2).toInt() == 0) {

                    view_count = 0
                    like_count = 0

                } else if (printDifference(date1, date2).toInt() > 0 && printDifference(
                        date1,
                        date2
                    ).toInt() <= 3
                ) {

                    view_count = 650
                    like_count = (10 * view_count) / 100

                } else if (printDifference(date1, date2).toInt() >= 3 && printDifference(
                        date1,
                        date2
                    ).toInt() <= 5
                ) {
                    view_count = 1050
                    like_count = (10 * view_count) / 100
                } else if (printDifference(date1, date2).toInt() >= 5 && printDifference(
                        date1,
                        date2
                    ).toInt() <= 10
                ) {
                    view_count = 1650
                    like_count = (10 * view_count) / 100
                } else if (printDifference(date1, date2).toInt() >= 10 && printDifference(
                        date1,
                        date2
                    ).toInt() <= 30
                ) {
                    view_count = 3500
                    like_count = (10 * view_count) / 100
                } else if (printDifference(date1, date2).toInt() >= 30) {
                    view_count = 7000
                    like_count = (10 * view_count) / 100
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }






            Log.e("++++++", like_count.toInt().toString())
            Log.e("++++++", view_count.toInt().toString())
            binding.tvViews.text = (item.viewCount!!.toInt() + view_count).toString()
            binding.tvLike.text = (item.likeCount!!.toInt() + like_count.toInt()).toString()

            binding.tvLike.isSelected = item.isLiked == "yes"
            val pref = SharedPrefrencesManager.getInstance(context)
            val loginDetails = Gson().fromJson(
                pref.getString(PrefConstant.LOGIN_RESPONSE, ""),
                ImportantDataResult::class.java
            )
            if (item.userId.toInt() != loginDetails.user_id.toInt()) {
                binding.viewDot.visibility = View.VISIBLE
                binding.tvFollow.visibility = View.VISIBLE
                if (item.isfollowing.equals("yes", true)) {
                    binding.tvFollow.text = context.getString(R.string.following)
                    binding.tvFollow.fadeIn(2000)
                } else if (item.isFollower.equals("yes", true)) {
                    binding.tvFollow.text = context.getString(R.string.follow)
                    binding.tvFollow.fadeOut(2000)
                }
            } else {
                binding.viewDot.visibility = View.GONE
                binding.tvFollow.visibility = View.GONE
            }
            // for flicks follow click functionality
            binding.tvFollow.setOnClickListener {
                var data = binding.tvFollow.text.toString()
                data = if (binding.tvFollow.text.toString().trim()
                        .equals(context.getString(R.string.following), true)
                ) {
                    binding.tvFollow.text.toString()
                } else {
                    binding.tvFollow.text.toString()
                }
                if (isNetworkAvailable(context)) {
                    if (data.equals(context.getString(R.string.following), true)) {
                        binding.tvFollow.text = context.getString(R.string.follow)
                        binding.tvFollow.fadeOut(800)
                        // list.removeAt(position)
                        //notifyItemRemoved(position)
                    } else {
                        binding.tvFollow.fadeIn(2000)
                        binding.tvFollow.text = context.getString(R.string.following)
                    }
                    // notifyItemChanged(position)
                    itemInterface?.onFlickListFollow(item, data)
                }
            }
            binding.imgProfilePic.setOnClickListener {
                val ins = Intent(context, UsersProfileActivity::class.java)
                ins.putExtra(IntentConstant.PROFILE_ID, item.userId)
                context.startActivity(ins)
            }
            binding.tvLike.setOnClickListener {
                onLikeEvent(item)

            }
            val gd = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (!binding.tvLike.isSelected)
                        onLikeEvent(item)
                    return true
                }

                override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                    return true
                }
            })
           // binding.pvVideo.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            binding.pvVideo.setOnTouchListener { v, event -> gd.onTouchEvent(event) }
            binding.imgMenu.setOnClickListener {
                itemInterface?.showPostMenu(
                    item.userId,
                    item.flickId,
                    item.username,
                    item.profilePic,
                    item.flickId
                )
            }

            binding.imgShare.setOnClickListener {

                share(context, "https://www.connectd.com/flick/${item.flickId!!}")

            }
            // more read functionality
            if (item.flickInfo.trim().length > 70) {
                binding.llMore.visibility = View.VISIBLE
                binding.tvCaption.visibility = View.GONE
                binding.tvReadMOre.visibility = View.VISIBLE
            } else {
                binding.llMore.visibility = View.GONE
                binding.tvCaption.visibility = View.VISIBLE
                binding.tvReadMOre.visibility = View.GONE
            }


            binding.tvReadMOre.setOnClickListener {
                if (binding.tvReadMOre.text.toString()
                        .equals(context.getString(R.string.read_more), true)
                ) {
                    binding.tvReadMOre.text = "Read Less"
                    binding.llMore.visibility = View.GONE
                    binding.tvCaption.visibility = View.VISIBLE
                } else {
                    binding.tvReadMOre.text = context.getString(R.string.read_more)
                    binding.llMore.visibility = View.VISIBLE
                    binding.tvCaption.visibility = View.GONE
                }
            }
            binding.tvComments.setOnClickListener {
                val intent = Intent(context, PostCommentActivity::class.java)
                intent.putExtra(IntentConstant.FLICK_DETAIL, item)
                intent.putExtra(IntentConstant.PAGE_FLAG, 2)
                context.startActivity(intent)
            }
            binding.executePendingBindings()
        }

        private fun onLikeEvent(item: GetFlickResponse.Result) {
            binding.tvLike.isSelected = !binding.tvLike.isSelected
            itemInterface?.onLike(item.flickId, binding.tvLike.isSelected)
            if (binding.tvLike.isSelected) {
                item.isLiked = "yes"
                item.likeCount = (item.likeCount!!.toInt() + 1).toString()
                binding.imagegif.setVisibility(View.VISIBLE)
                Handler().postDelayed(
                    Runnable {
                        binding.imagegif.setVisibility(View.INVISIBLE)

                    },
                    1500
                )
            } else {
                item.isLiked = "no"
                item.likeCount = (item.likeCount!!.toInt() - 1).toString()
            }
            binding.tvLike.text = (item.likeCount.toInt() + like_count).toString()
        }

        private fun printDifference(startDate: Date, endDate: Date): Long {
            //milliseconds
            var different = endDate.time - startDate.time;

            System.out.println("startDate : " + startDate);
            System.out.println("endDate : " + endDate);
            System.out.println("different : " + different);

            var secondsInMilli = 1000;
            var minutesInMilli = secondsInMilli * 60;
            var hoursInMilli = minutesInMilli * 60;
            var daysInMilli = hoursInMilli * 24;

            var elapsedDays = different / daysInMilli;
            different = different % daysInMilli;

            var elapsedHours = different / hoursInMilli;
            different = different % hoursInMilli;

            var elapsedMinutes = different / minutesInMilli;
            different = different % minutesInMilli;

            var elapsedSeconds = different / secondsInMilli;

            System.out.printf(
                "%d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds
            );
            return elapsedDays

        }


        private fun setPlayer(
            videoUrl: String,
            playerView: PlayerView?,
            position: Int,
            play_value: Boolean
        ) {

            //   exoPlayer?.stop()
            //    exoPlayer?.release()
            exoPlayer = SimpleExoPlayer.Builder(context).build()
              exoPlayer?.playWhenReady = true


            exoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
            playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            exoPlayer?.videoScalingMode = Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT
            playerView?.player = exoPlayer

            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.getString(R.string.app_name))
            )

            val videoSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())
                    .createMediaSource(Uri.parse(videoUrl))
            // Prepare the player with the source.
            exoPlayer?.prepare(videoSource, true, false)





            listt.add(exoPlayer!!)
            itemInterface!!.sendExoPlayer(listt, exoPlayer!!, position, playerView!!)
            global_position = position


            Log.e("EXOPLAYER", exoPlayer.toString())





        }


    }


    fun videoplay(exoPlayer: SimpleExoPlayer, position: Int) {

        Log.e("EXOPLAYER", listt.size.toString())
        Log.e("positionSelecetedADA", position.toString())

        if(listt.size>position) {

            for (i in 0 until listt.size) {




                if (listt.get(i) == listt.get(position)) {
                    Log.e("positionSelecetedPLAY", i.toString())
                    //   listt.get(i).playWhenReady = true
                    listt.get(i).seekTo(0)
                    listt.get(i).playWhenReady = true
                } else {
                    listt.get(i).seekTo(0)
                    listt.get(i).playWhenReady = false
                    //   listt.get(i).stop()

                  //  listt.get(i).stop()
                }


            }

        }

    }


    inline fun View.fadeIn(durationMillis: Long = 250) {
        this.startAnimation(AlphaAnimation(0F, 1F).apply {
            duration = durationMillis
            fillAfter = true
        })
    }

    inline fun View.fadeOut(durationMillis: Long = 250) {
        this.startAnimation(AlphaAnimation(1F, 0F).apply {
            duration = durationMillis
            fillAfter = true
        })
    }


    inner class ViewHolder2(var binding: ItemAddsFlickBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: GetFlickResponse.Result,
            position: Int,
            itemInterface: OnFeedSelectedInterface?
        ) {
            binding.item = item


            adLoader = AdLoader.Builder(context, "ca-app-pub-3601101210502228/2223823916")
                .forNativeAd { ad: NativeAd ->
                    val background: ColorDrawable? = null
                    val styles =
                        NativeTemplateStyle.Builder().withMainBackgroundColor(background)
                            .build()




                    binding.nativeTemplateView?.setStyles(styles)
                    binding.nativeTemplateView?.setNativeAd(ad)


                       // exoPlayer = SimpleExoPlayer.Builder(context).build()
                       // itemInterface!!.sendExoPlayer(listt, exoPlayer!!, position, binding.pvVideo!!)
                       // listt.add(exoPlayer!!)

                    ///     adLoaded = true
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        // Handle the failure by logging, altering the UI, and so on.

                        binding.nativeTemplateView.visibility = View.GONE
                        binding.pvVideo.visibility = View.VISIBLE

//                            setPlayer(
//                                "http://15.206.254.159/app/uploads/flick/1625837746657.mp4",
//                                binding.pvVideo,
//                                position,
//                                item.value
//                            )


                        Log.e("EROROR", "Failed to load native ad: $adError")
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build()
                )
                .build()


//            adLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110").withAdListener(object : AdListener() {
//                override fun onAdFailedToLoad(errorCode: Int) {
//                    Toast.makeText(context, "Failed to load native ad: $errorCode",
//                        Toast.LENGTH_SHORT).show()
//                    Log.e("EROROR","Failed to load native ad: $errorCode",)
//                }
//            }).build()


            loadNativeAd();


        }


        private fun setPlayer(
            videoUrl: String,
            playerView: PlayerView?,
            position: Int,
            play_value: Boolean
        ) {

            //   exoPlayer?.stop()
            //    exoPlayer?.release()
            exoPlayer = SimpleExoPlayer.Builder(context).build()
            //  exoPlayer?.playWhenReady = true
            exoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
            playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            exoPlayer?.videoScalingMode = Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT
            playerView?.player = exoPlayer
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.getString(R.string.app_name))
            )

            val videoSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())
                    .createMediaSource(Uri.parse(videoUrl))
            // Prepare the player with the source.
            exoPlayer?.prepare(videoSource, true, false)



            itemInterface!!.sendExoPlayer(listt, exoPlayer!!, position, playerView!!)
            global_position = position
            listt.add(exoPlayer!!)

            Log.e("EXOPLAYER", exoPlayer.toString())


        }


    }


    private fun loadNativeAd() {
        // Creating  an Ad Request
        val adRequest = AdRequest.Builder().build()

        // load Native Ad with the Request
        adLoader!!.loadAd(adRequest)

        // Showing a simple Toast message to user when Native an ad is Loading
        // Toast.makeText(context, "Native Ad is loading ", Toast.LENGTH_LONG).show()
    }


    companion object {
        private const val FLICK_LAYOUT = 0
        private const val ADD_LAYOUT = 1

    }


}