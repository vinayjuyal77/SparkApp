package com.app.spark.fragment.feeds

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.UsersProfileActivity
import com.app.spark.activity.main.MainActivity
import com.app.spark.activity.post_comments.PostCommentActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ItemFeedBinding
import com.app.spark.interfaces.OnFeedSelectedInterface
import com.app.spark.models.FeedsResponse
import com.app.spark.utils.BlurTransformation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.alterac.blurkit.BlurKit


class FeedsAdapter(
    var context: Context,
    val list: MutableList<FeedsResponse.Result>,
    val itemInterface: OnFeedSelectedInterface? = null
) :
    RecyclerView.Adapter<FeedsAdapter.ViewHolder>() {

    private var selectedItem = 0

    var vibe = context.getSystemService(VIBRATOR_SERVICE) as Vibrator?

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val binding: ItemFeedBinding = DataBindingUtil.inflate(
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

        var viewType = 1 //Default is 1

        if (position == 0) viewType = 0 //if zero, it will be a header view

        return viewType


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


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position], position, itemInterface)
    }


    inner class ViewHolder(var binding: ItemFeedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: FeedsResponse.Result,
            position: Int,
            itemInterface: OnFeedSelectedInterface?
        ) {
            binding.item = item
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
                item.likeCount = item.likeCount!!.toInt().plus(1).toString()
            } else {
                item.isLiked = "no"
                item.likeCount = item.likeCount!!.toInt().minus(1).toString()
            }
            binding.tvLike.text = item.likeCount
        }

    }
}