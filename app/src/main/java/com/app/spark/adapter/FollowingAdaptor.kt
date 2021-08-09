package com.app.spark.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.databinding.ItemFollowFollowingBinding
import com.app.spark.models.ResultFollowing
import com.bumptech.glide.Glide


class FollowingAdaptor(
    var context: Context, val list: MutableList<ResultFollowing>,
    val listner: RemoveFollower
) :
    RecyclerView.Adapter<FollowingAdaptor.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemFollowFollowingBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_follow_following,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(context, list[position], position)
    }

    inner class ViewHolder(var binding: ItemFollowFollowingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(context: Context, item: ResultFollowing, position: Int) {
            binding.tvName.text = item.username
            binding.tvUserName.text = item.name
            if (item.myType.equals("following", true)) {
                binding.tvFollow.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_button_black)
                binding.tvFollow.text = context.resources.getString(R.string.following)
            } else {
                if (!item.isfollowing.equals("yes", true)) {
                    binding.tvFollow.text = context.resources.getString(R.string.follow)
                    binding.tvFollow.background = ContextCompat.getDrawable(context, R.drawable.ic_bg_button_green)
                } else {
                    binding.tvFollow.text = context.resources.getString(R.string.following)
                    binding.tvFollow.background = ContextCompat.getDrawable(context, R.drawable.ic_bg_button_green)
                }
            }
            loadImagesProfile(context, item.profilePic, "", binding.imgProfilePic)

            binding.tvFollow.setOnClickListener {
                if (item.isfollowing.equals("yes", true)) {
                    listner.removeFollower(item)
                    item.isfollowing = "no"
                } else {
                    listner.addFollower(item)
                    item.isfollowing = "yes"
                }
                notifyItemChanged(position)
            }
            binding.imgProfilePic.setOnClickListener {
                listner.profileDetails(item)
            }
            binding.tvName.setOnClickListener {
                listner.profileDetails(item)
            }
            binding.tvUserName.setOnClickListener {
                listner.profileDetails(item)
            }
        }
    }


    private fun loadImagesProfile(
        context: Context,
        imageUrl: String,
        thumbNail: String,
        imageView: ImageView
    ) {
        if (!thumbNail.trim().isNullOrEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                //.diskCacheStrategy(DiskCacheStrategy.NONE)
                .thumbnail(Glide.with(context).load(thumbNail))
                .into(imageView)
        } else {
            Glide.with(context)
                .load(imageUrl)
                //.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .thumbnail(Glide.with(context).load(R.drawable.ic_placeholder))
                .into(imageView)
        }
    }

    interface RemoveFollower {
        fun removeFollower(model: ResultFollowing)
        fun profileDetails(model: ResultFollowing)
        fun addFollower(model: ResultFollowing)
    }


}