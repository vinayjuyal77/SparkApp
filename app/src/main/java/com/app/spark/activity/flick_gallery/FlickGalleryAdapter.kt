package com.app.spark.activity.flick_gallery

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.CodeBoy.MediaFacer.mediaHolders.audioContent
import com.CodeBoy.MediaFacer.mediaHolders.pictureContent
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.app.spark.R
import com.app.spark.databinding.ItemGalleryBinding
import com.app.spark.interfaces.OnItemSelectedInterface
import com.app.spark.models.MediaModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import java.io.File
import java.util.ArrayList


class FlickGalleryAdapter(
    var context: Context,
    val list: ArrayList<videoContent>,
    val itemInterface: OnItemSelectedInterface? = null
) :
    RecyclerView.Adapter<FlickGalleryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemGalleryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_gallery,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position], position, itemInterface)
    }


    inner class ViewHolder(var binding: ItemGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: videoContent,
            position: Int,
            itemInterface: OnItemSelectedInterface?
        ) {

            Glide.with(context)
                .load(item.assetFileStringUri)
                .placeholder(R.color.white)
                .error(R.color.white)
                .apply(RequestOptions().centerCrop())
                .into(binding.imgPost)
            binding.imgPost.setOnClickListener {
                itemInterface?.onItemSelected(position)
            }
        }

    }
}