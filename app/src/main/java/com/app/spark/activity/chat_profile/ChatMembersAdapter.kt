package com.app.spark.activity.chat_profile

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.CodeBoy.MediaFacer.mediaHolders.audioContent
import com.CodeBoy.MediaFacer.mediaHolders.pictureContent
import com.app.spark.R
import com.app.spark.databinding.ItemChatAttachmentsBinding
import com.app.spark.databinding.ItemChatMutualGroupBinding
import com.app.spark.databinding.ItemGalleryBinding
import com.app.spark.databinding.ItemMusicGallaryBinding
import com.app.spark.interfaces.OnItemSelectedInterface
import com.app.spark.models.ChatUserDetail
import com.app.spark.models.MediaModel
import com.app.spark.utils.setThumbnailFromUrl
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import java.io.File
import java.util.ArrayList


class ChatMembersAdapter(
    var context: Activity,
    val list: List<ChatUserDetail.Result.MutualFriend>
) :
    RecyclerView.Adapter<ChatMembersAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemChatMutualGroupBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_chat_mutual_group,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position], position)
    }


    inner class ViewHolder(var binding: ItemChatMutualGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: ChatUserDetail.Result.MutualFriend,
            position: Int
        ) {

            Glide.with(context).load(item.profilePic).placeholder(R.drawable.ic_placeholder)
                .into(binding.imgProfilePic)
            binding.tvTitle.text = item.name
        }

    }
}