package com.app.spark.activity.gallery

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.CodeBoy.MediaFacer.mediaHolders.pictureContent
import com.app.spark.R
import com.app.spark.databinding.ItemGalleryBinding
import com.app.spark.interfaces.OnItemSelectedInterface
import com.app.spark.models.MediaModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import java.io.File
import java.util.ArrayList


class GalleryAdapter(
    var context: Context,
    val list: MutableList<MediaModel>,
    val itemInterface: OnItemSelectedInterface? = null
) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private var selectedItem = 0

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

    fun updateList(list: MediaModel) {
        this.list.add(list)
        notifyItemInserted(itemCount)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position], position, itemInterface)
    }


    inner class ViewHolder(var binding: ItemGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: MediaModel,
            position: Int,
            itemInterface: OnItemSelectedInterface?
        ) {
            if (item.mediaType == 1) {
                Glide.with(context)
                    .load(item.path)
                    .apply(RequestOptions().centerCrop())
                    .into(binding.imgPost)
            } else if (item.thumbNail != null) {
                binding.imgPost.setImageBitmap(item.thumbNail)
            }
            if (selectedItem == 0) {
                selectedItem=-1
                itemInterface?.onItemSelected(0)
            }
            binding.imgPost.setOnClickListener {
                itemInterface?.onItemSelected(position)
            }
        }

    }
}