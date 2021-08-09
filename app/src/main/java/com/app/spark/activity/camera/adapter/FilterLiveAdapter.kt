package com.app.spark.activity.camera.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.custom_gallery.adapter.EditingToolsAdapter
import com.app.spark.activity.custom_gallery.adapter.ToolType
import com.app.spark.databinding.ItemFilterRoundBinding
import com.app.spark.photoeditor.PhotoFilter
import com.otaliastudios.cameraview.filter.Filters
import java.util.ArrayList

class FilterLiveAdapter (val context: Context,
    var list:Array<Filters>) : RecyclerView.Adapter<FilterLiveAdapter.ViewHolder>() {
    private val mImageFilter: MutableList<Int> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemFilterRoundBinding>(
            LayoutInflater.from(context),
            R.layout.item_filter_round,
            parent,
            false
        )
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(list[position])
        holder.binding.tvFilterName.text=list[position].toString()
        holder.binding.ivFilter.setImageResource(mImageFilter[position])
        holder.itemView.setOnClickListener {
            onFilterChange?.invoke(list[position])
        }
    }
    var onFilterChange: ((Filters) -> Unit)? = null
    override fun getItemCount(): Int {
        return list.size
    }
    inner class ViewHolder(val binding: ItemFilterRoundBinding) :  RecyclerView.ViewHolder(binding.root){
        fun bindView(item : Filters){
            with(binding){
            }
        }
    }
    init {
        mImageFilter.add(R.drawable.img_none)
        mImageFilter.add(R.drawable.img_auto_fix)
        mImageFilter.add(R.drawable.img_black_and_white)
        mImageFilter.add(R.drawable.img_brightness)
        mImageFilter.add(R.drawable.img_contrast)
        mImageFilter.add(R.drawable.img_cross_process)
        mImageFilter.add(R.drawable.img_documentary)
        mImageFilter.add(R.drawable.img_duotone)
        mImageFilter.add(R.drawable.img_fill_light)
        mImageFilter.add(R.drawable.img_gamma)
        mImageFilter.add(R.drawable.img_grain)
        mImageFilter.add(R.drawable.img_grayscale)
        mImageFilter.add(R.drawable.img_hue)
        mImageFilter.add(R.drawable.img_invert_colors)
        mImageFilter.add(R.drawable.img_lomoish)
        mImageFilter.add(R.drawable.img_posterize)
        mImageFilter.add(R.drawable.img_saturation)
        mImageFilter.add(R.drawable.img_sepia)
        mImageFilter.add(R.drawable.img_sharpness)
        mImageFilter.add(R.drawable.img_temperature)
        mImageFilter.add(R.drawable.img_tint)
        mImageFilter.add(R.drawable.img_vignette)


    }
}
