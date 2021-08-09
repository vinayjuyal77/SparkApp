package com.app.spark.activity.user_flicks

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.VideoView
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.UsersProfileActivity
import com.app.spark.activity.main.MainActivity
import com.app.spark.activity.post_comments.PostCommentActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ItemFlicksBinding
import com.app.spark.interfaces.OnFeedSelectedInterface
import com.app.spark.models.GetFlickResponse
import com.app.spark.models.ImportantDataResult
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.date.DateTimeUtils
import com.app.spark.utils.isNetworkAvailable
import com.app.spark.utils.share
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.gson.Gson
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class FlicksAdapter(
    var context: Context,
    var list: MutableList<GetFlickResponse.Result>,
    val itemInterface: OnFeedSelectedInterface? = null
) :
    RecyclerView.Adapter<FlicksAdapter.ViewHolder>() {


    var like_count = 0
    var view_count = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemFlicksBinding = DataBindingUtil.inflate(
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

    fun updateList(it: List<GetFlickResponse.Result>) {
        val size = itemCount

        list.addAll(it)
        notifyDataSetChanged()
     //   notifyItemRangeInserted(size, it.size)
        /* val diffResult = DiffUtil.calculateDiff(FlicksDiffCallback(it, list))
         diffResult.dispatchUpdatesTo(this)*/
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position], position, itemInterface)
    }


    inner class ViewHolder(var binding: ItemFlicksBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: GetFlickResponse.Result,
            position: Int,
            itemInterface: OnFeedSelectedInterface?
        ) {
            binding.item = item



            Glide.with(context).load(item.flick_thumbnail).into(binding.imgPost)
            binding.imgPost.visibility  =View.VISIBLE
           // setPlayerVideoview(item.flickMedia, binding.pvVideo1, binding.imgPost)


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

                    view_count = 650
                    like_count =  (10*view_count)/100

                }
                else if(printDifference(date1, date2).toInt() >= 3 && printDifference(date1, date2).toInt() <= 5)
                {
                    view_count = 1050
                    like_count =  (10*view_count)/100
                }

                else if(printDifference(date1, date2).toInt() >= 5 && printDifference(date1, date2).toInt() <= 10)
                {
                    view_count = 1650
                    like_count =  (10*view_count)/100
                }
                else if(printDifference(date1, date2).toInt() >= 10 && printDifference(date1, date2).toInt() <= 30)
                {
                    view_count = 3500
                    like_count =  (10*view_count)/100
                }

                else if(printDifference(date1, date2).toInt() >= 30)
                {
                    view_count = 7000
                    like_count =  (10*view_count)/100
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }






            Log.e("++++++", like_count.toInt().toString())
            Log.e("++++++", view_count.toInt().toString())
            binding.tvViews.text = (item.viewCount!!.toInt() + view_count).toString()
            binding.tvLike.text = (item.likeCount!!.toInt() + like_count.toInt()).toString()

            binding.tvLike.isSelected = item.isLiked == "yes"
            var pref = SharedPrefrencesManager.getInstance(context)
            var loginDetails = Gson().fromJson(
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
                     //   list.removeAt(position)
                      //  notifyItemRemoved(position)
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
                    onLikeEvent(item)
                    return true
                }

                override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                    return true
                }
            })
         //   binding.pvVideo.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
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



            binding.imgShare.setOnClickListener {

                share(context, "https://www.connectd.com/flick/${item.flickId!!}")

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
                item.likeCount = (item.likeCount!!.toInt() - 1 ).toString()
            }
            binding.tvLike.text = (item.likeCount.toInt() + like_count).toString()
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



    private fun setPlayerVideoview(videoUrl: String, videoView: VideoView, shapeableImageView: ImageView) {

//            shapeableImageView.visibility = View.VISIBLE
//            videoView.visibility = View.INVISIBLE
        // videoView.setVideoPath(videoUrl)
        videoView.setVideoURI(videoUrl.toUri())
        videoView.seekTo( 1 );





        videoView.setOnPreparedListener {


//                videoView.visibility = View.VISIBLE
//                shapeableImageView.visibility = View.GONE

            it.start()
            shapeableImageView.visibility = View.INVISIBLE



            //   var video_ration = it.videoWidth / it.videoHeight
            //    var screen_ratio = videoView.width / videoView.height

//
        }
        videoView.setOnCompletionListener {

            //  shapeableImageView.visibility = View.GONE
            //   videoView.visibility = View.VISIBLE

            it.start()

        }
        videoView.setOnErrorListener(object : MediaPlayer.OnErrorListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                Log.d("video", "setOnErrorListener ")
              //  videoView.setVideoURI("http://15.206.254.159/app/uploads/flick/1625837746657.mp4".toUri())
              //  videoView.seekTo( 1 );
                return true

            }
        })







    }

}