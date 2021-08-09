package com.app.spark.activity.group_chat_room

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
import com.app.spark.databinding.ItemGalleryBinding
import com.app.spark.databinding.ItemMusicGallaryBinding
import com.app.spark.interfaces.OnItemSelectedInterface
import com.app.spark.models.MediaModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import java.io.File
import java.util.ArrayList


class DocAdapter(
    var context: Context,
    val list: List<MediaModel>,
    val itemInterface: OnItemSelectedInterface? = null
) :
    RecyclerView.Adapter<DocAdapter.ViewHolder>() {


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
        return if (list.size > 11) 11 else list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position], position, itemInterface)
    }


    inner class ViewHolder(var binding: ItemChatAttachmentsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: MediaModel,
            position: Int,
            itemInterface: OnItemSelectedInterface?
        ) {
            binding.tvViewLibrary.visibility = View.VISIBLE

            if (position < (itemCount-1)) {
                binding.tvViewLibrary.text= item.name
            }

            binding.imgPost.setOnClickListener {
                itemInterface?.onItemSelected(position,itemCount)
            }
        }

    }
}