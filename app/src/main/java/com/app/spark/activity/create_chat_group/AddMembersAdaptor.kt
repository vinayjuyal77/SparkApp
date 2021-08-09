package com.app.spark.activity.create_chat_group

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
import com.app.spark.databinding.ItemChatAddMembersBinding
import com.app.spark.databinding.ItemFollowFollowingBinding
import com.app.spark.models.ResultFollowing
import com.bumptech.glide.Glide


class AddMembersAdaptor(
    var context: Context, val list: List<ResultFollowing>
) :
    RecyclerView.Adapter<AddMembersAdaptor.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemChatAddMembersBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_chat_add_members,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getMembersList(): List<ResultFollowing> {
        return list
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(context, list[position], position)
    }

    inner class ViewHolder(var binding: ItemChatAddMembersBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(context: Context, item: ResultFollowing, position: Int) {
            binding.tvName.text = item.name
            loadImagesProfile(context, item.profilePic, "", binding.imgProfilePic)
            if (item.isSelected) {
                binding.imgSelected.visibility = View.VISIBLE
            } else binding.imgSelected.visibility = View.GONE

            binding.rlMember.setOnClickListener {
                item.isSelected = !item.isSelected
                notifyItemChanged(position)
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

}