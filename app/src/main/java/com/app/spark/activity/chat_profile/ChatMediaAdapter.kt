package com.app.spark.activity.chat_profile

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.databinding.ItemChatAttachmentsBinding
import com.app.spark.models.ChatUserDetail
import com.app.spark.utils.setThumbnailFromUrl
import com.bumptech.glide.Glide


class ChatMediaAdapter(
    var context: Activity,
    val list: List<ChatUserDetail.Result.MediaData>
) :
    RecyclerView.Adapter<ChatMediaAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemChatAttachmentsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_chat_attachments,
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


    inner class ViewHolder(var binding: ItemChatAttachmentsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: ChatUserDetail.Result.MediaData,
            position: Int
        ) {
            if (item.mediaType == "audio") {
                binding.imgPost.setImageResource(R.drawable.ic_post_music)
            } else if (item.mediaType == "video") {
                setThumbnailFromUrl(context, binding.imgPost, item.url)
            } else {
                Glide.with(context).load(item.url).placeholder(R.drawable.ic_placeholder)
                    .into(binding.imgPost)
            }
        }

    }
}