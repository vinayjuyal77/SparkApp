package com.app.spark.fragment.feeds

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.UsersProfileActivity
import com.app.spark.activity.post_comments.PostCommentActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.IntentConstant.FeedValue
import com.app.spark.databinding.ItemAddsBinding
import com.app.spark.databinding.ItemFeedBinding
import com.app.spark.interfaces.OnFeedSelectedInterface
import com.app.spark.models.FeedsResponse
import com.app.spark.utils.BlurTransformation
import com.app.spark.utils.date.DateTimeUtils
import com.app.spark.utils.share
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import io.alterac.blurkit.BlurKit
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class FeedsAdapter2(
    var context: Context,
    val list: MutableList<FeedsResponse.Result>,
    val itemInterface: OnFeedSelectedInterface? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedItem = 0

    var vibe = context.getSystemService(VIBRATOR_SERVICE) as Vibrator?

    private var adLoader: AdLoader? = null

    var like_count = 0
    var view_count = 0



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var binding: ItemFeedBinding;
        var binding2: ItemAddsBinding;
        if (viewType == ADD_LAYOUT) {

            binding2 = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.item_adds,
                parent,
                false
            )
            return ViewHolder2(binding2)
        } else if (viewType == FEED_LAYOUT) {

            binding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.item_feed,
                parent,
                false
            )
            return ViewHolder(binding)
        }


        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_feed,
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
            return FEED_LAYOUT

        } else if (position % 6 == 0) {
            return ADD_LAYOUT
        } else {
            return FEED_LAYOUT
        }

    }


    fun updateList(it: List<FeedsResponse.Result>) {
        val diffResult = DiffUtil.calculateDiff(FeedsDiffCallback(it, list))
        list.clear()
        list.addAll(it)
        diffResult.dispatchUpdatesTo(this)
    }

    fun pagingList(it: List<FeedsResponse.Result>) {
        val size = itemCount
        list.addAll(it)
        notifyItemRangeInserted(size, it.size)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //    holder.bindData(list[position], position, itemInterface)

        when (holder) {
            is ViewHolder -> holder.bindData(list[position], position, itemInterface)
            is ViewHolder2 -> holder.bindData(list[position], position, itemInterface)
            //  is ColleagueViewHolder -> holder.bind(element as ColleagueDataModel)
            else -> throw IllegalArgumentException()
        }

    }

//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bindData(list[position], position, itemInterface)
//    }


    inner class ViewHolder(var binding: ItemFeedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: FeedsResponse.Result,
            position: Int,
            itemInterface: OnFeedSelectedInterface?
        ) {
            binding.item = item


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

                if(printDifference(date1, date2).toInt()== 0)
                {

                    view_count = 0
                    like_count =  0

                }

                else if(printDifference(date1, date2).toInt() > 0 && printDifference(date1, date2).toInt() <= 3)
                {

                    view_count = 65
                    like_count =  (10*view_count)/100

                }
                else if(printDifference(date1, date2).toInt() >= 3 && printDifference(date1, date2).toInt() <= 5)
                {
                    view_count = 105
                    like_count =  (10*view_count)/100
                }

                else if(printDifference(date1, date2).toInt() >= 5 && printDifference(date1, date2).toInt() <= 10)
                {
                    view_count = 165
                    like_count =  (10*view_count)/100
                }
                else if(printDifference(date1, date2).toInt() >= 10 && printDifference(date1, date2).toInt() <= 30)
                {
                    view_count = 350
                    like_count =  (10*view_count)/100
                }

                else if(printDifference(date1, date2).toInt() >= 30)
                {
                    view_count = 700
                    like_count =  (10*view_count)/100
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }






            Log.e("++++++", like_count.toInt().toString())
            Log.e("++++++", view_count.toInt().toString())
            binding.tvViewCount.text = (item.viewCount!!.toInt() + view_count).toString()
            binding.tvLike.text = (item.likeCount!!.toInt() + like_count.toInt()).toString()
            if (item.mediaType == "photo") {
                binding.imgPost.scaleType = ImageView.ScaleType.FIT_XY
                Glide.with(binding.imgPost).load(item.postMedia).into(binding.imgPost)
            } else if (item.mediaType == "audio") {
                binding.imgPost.setImageResource(R.drawable.ic_profile)
                if (item.profilePic!!.trim().isNotEmpty()) {
                    Glide.with(binding.imgPost).load(item.profilePic)
                        .placeholder(R.drawable.ic_profile)
                        .apply(RequestOptions.bitmapTransform(BlurTransformation(context)))
                        .into(object : CustomTarget<Drawable>() {
                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable>?
                            ) {
                                binding.imgPost.scaleType = ImageView.ScaleType.CENTER_CROP
                                binding.imgPost.setImageDrawable(resource)
                                binding.imgAudioPic.setImageDrawable(resource)
                                binding.imgPost.post {
                                    if (binding.imgPost.width > 0 && binding.imgPost.height > 0) {
                                        val bitmap =
                                            BlurKit.getInstance().fastBlur(binding.imgPost, 1, 0.1f)
                                        binding.imgPost.setImageBitmap(bitmap)
                                    }
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }

                        })
                }

            }
            binding.tvLike.isSelected = item.isLiked == "yes"
            if (item.isPlays == null) {
                if (item.mediaType.equals("video", true)) {
                    binding.imgPlayPause.setImageResource(R.drawable.ic_pause_video)
                } else {
                    binding.imgPlayPause.setImageResource(R.drawable.ic_pause_audio)
                }
            } else if (item.isPlays!!.toInt() == 0) {
                if (item.mediaType.equals("video", true)) {
                    binding.imgPlayPause.setImageResource(R.drawable.ic_play_started_white)
                } else {
                    binding.imgPlayPause.setImageResource(R.drawable.ic_play_started_24px)
                }
            } else {
                if (item.mediaType.equals("video", true)) {
                    binding.imgPlayPause.setImageResource(R.drawable.ic_pause_video)
                } else {
                    binding.imgPlayPause.setImageResource(R.drawable.ic_pause_audio)
                }
            }

            // more read functionality
            if (item.postInfo!!.trim().length > 50) {
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

            binding.imgShare.setOnClickListener {

                share(context, "https://www.connectd.com/feed/${item.postId!!}")

            }



            binding.imgProfilePic.setOnClickListener {
                val ins = Intent(context, UsersProfileActivity::class.java)
                ins.putExtra(IntentConstant.PROFILE_ID, item.userId)
                context.startActivity(ins)
            }
            binding.imgPlayPause.setOnClickListener {
                if (item.isPlays == null || item.isPlays!!.toInt() == 0) {
                    item.isPlays = "1"
                } else {
                    item.isPlays = "0"
                }
                notifyItemChanged(position)
                itemInterface?.onPlayPause(item.isPlays!!.toInt())
                //   notifyItemChanged(position)
            }




            binding.tvLike.setOnClickListener {
                onLikeEvent(item)
            }
            val gd = GestureDetector(context, object : SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (!binding.tvLike.isSelected)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vibe?.vibrate(VibrationEffect.createOneShot(500, 10))

                        } else {
                            vibe?.vibrate(500)

                        }

                    onLikeEvent(item)
                    return true
                }

                override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                    return true
                }
            })
            binding.imgPost.setOnTouchListener { v, event -> gd.onTouchEvent(event) }
            binding.pvVideo.setOnTouchListener { v, event -> gd.onTouchEvent(event) }
            binding.imgPostMenu.setOnClickListener {
                itemInterface?.showPostMenu(
                    item.userId,
                    item.postId!!,
                    item.username!!,
                    item.profilePic,
                    item.postId!!
                )
            }
            binding.tvComments.setOnClickListener {
                val intent = Intent(context, PostCommentActivity::class.java)
                intent.putExtra(IntentConstant.POST_DETAIL, item)
                intent.putExtra(IntentConstant.PAGE_FLAG, 22)
                context.startActivity(intent)
            }
            binding.executePendingBindings()
        }

        private fun onLikeEvent(item: FeedsResponse.Result) {
            binding.tvLike.isSelected = !binding.tvLike.isSelected
            itemInterface?.onLike(item.postId!!, binding.tvLike.isSelected)
            if (binding.tvLike.isSelected) {

                binding.imagegif.setVisibility(View.VISIBLE)
                Handler().postDelayed(
                    Runnable {
                        binding.imagegif.setVisibility(View.INVISIBLE)

                    },
                    1500
                )
                item.isLiked = "yes"
                item.likeCount = (item.likeCount!!.toInt() + 1).toString()

            } else {
                item.isLiked = "no"
                item.likeCount = (item.likeCount!!.toInt() - 1).toString()

            }
            binding.tvLike.text = (item.likeCount!!.toInt() + like_count).toString()

        }


            private fun printDifference( startDate : Date,  endDate : Date) : Long {
                //milliseconds
                var different = endDate.time - startDate.time;

                System.out.println("startDate : " + startDate);
                System.out.println("endDate : "+ endDate);
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
                    elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
                return elapsedDays

        }

    }


    inner class ViewHolder2(var binding: ItemAddsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: FeedsResponse.Result,
            position: Int,
            itemInterface: OnFeedSelectedInterface?
        )
        {

            binding.item = item



            adLoader = AdLoader.Builder(context, "ca-app-pub-3601101210502228/6966876520")
                .forNativeAd { ad : NativeAd ->
                    val background: ColorDrawable? = null
                    val styles =
                        NativeTemplateStyle.Builder().withMainBackgroundColor(background)
                            .build()

                    binding.nativeTemplateView?.setStyles(styles)
                    binding.nativeTemplateView?.setNativeAd(ad)
                    ///     adLoaded = true
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        // Handle the failure by logging, altering the UI, and so on.
                        Log.e("EROROR", "Failed to load native ad: $adError")
                        binding.nativeTemplateView.visibility = View.GONE
                        binding.background.visibility = View.VISIBLE

                        binding.imgPost.setImageResource(R.drawable.admob_image1)
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                    // Methods in the NativeAdOptions.Builder class can be
                    // used here to specify individual options settings.
                    .build())
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


    }


    private fun loadNativeAd() {
        // Creating  an Ad Request
        val adRequest = AdRequest.Builder().build()

        // load Native Ad with the Request
        adLoader!!.loadAd(adRequest)

        // Showing a simple Toast message to user when Native an ad is Loading
        //  Toast.makeText(context, "Native Ad is loading ", Toast.LENGTH_LONG).show()
    }


    companion object {
        private const val FEED_LAYOUT = 0
        private const val ADD_LAYOUT = 1

    }
}