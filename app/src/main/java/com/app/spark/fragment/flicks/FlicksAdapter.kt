package com.app.spark.fragment.flicks

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.*
import android.view.animation.AlphaAnimation
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
import com.app.spark.utils.isNetworkAvailable
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.gson.Gson


class FlicksAdapter(
    var context: Context,
    var list: MutableList<GetFlickResponse.Result>,
    val itemInterface: OnFeedSelectedInterface? = null
) :
    RecyclerView.Adapter<FlicksAdapter.ViewHolder>() {


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
                        list.removeAt(position)
                        notifyItemRemoved(position)
                    } else {
                        binding.tvFollow.fadeIn(2000)
                        binding.tvFollow.text = context.getString(R.string.following)
                    }
                    notifyItemChanged(position)
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
            binding.executePendingBindings()
        }

        private fun onLikeEvent(item: GetFlickResponse.Result) {
            binding.tvLike.isSelected = !binding.tvLike.isSelected
            itemInterface?.onLike(item.flickId, binding.tvLike.isSelected)
            if (binding.tvLike.isSelected) {
                item.isLiked = "yes"
                item.likeCount = item.likeCount.toInt().plus(1).toString()
                binding.imagegif.setVisibility(View.VISIBLE)
                Handler().postDelayed(
                    Runnable {
                        binding.imagegif.setVisibility(View.INVISIBLE)

                    },
                    1500
                )
            } else {
                item.isLiked = "no"
                item.likeCount = item.likeCount.toInt().minus(1).toString()
            }
            binding.tvLike.text = item.likeCount
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
}