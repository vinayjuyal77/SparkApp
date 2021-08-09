package com.app.spark.activity.custom_gallery

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.camera.utill_camera.timeConversion
import com.app.spark.databinding.ItemAddListBinding
import com.app.spark.databinding.ItemVideoListBinding
import com.app.spark.models.TagListModel
import com.bumptech.glide.Glide
import java.lang.Exception

class AddItemAdapter(
    val context: Context,
    var list: MutableList<VideoModel>
) : RecyclerView.Adapter<AddItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemAddListBinding>(
            LayoutInflater.from(context),
            R.layout.item_add_list,
            parent,
            false
        )
        return ViewHolder(binding)
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(list[position])
        val videoItem = list[position]
        Glide.with(context).asBitmap().load(videoItem.uriPath).error(R.drawable.ic_baseline_video_call_24)
            .into(holder.binding.imageView)
        holder.binding.tvTime.text= timeConversion(videoItem.duration)
    }
    var onItemClaer: ((Long) -> Unit)? = null
    override fun getItemCount(): Int {
        return list.size
    }
    inner class ViewHolder(val binding: ItemAddListBinding) :  RecyclerView.ViewHolder(binding.root){
        fun bindView(item : VideoModel){
            with(binding){
                binding.ivClear.setOnClickListener {
                    onItemClaer?.invoke(item.id)
                    list.remove(list[adapterPosition])
                    notifyItemRemoved(adapterPosition)
                }
            }
        }
    }
    fun updateList(updatedList : VideoModel){
        val oldSize = list.size
        list.add(updatedList)
        notifyItemRangeInserted(oldSize,itemCount)
    }

    fun removeItem(videoModel: VideoModel){
        try {
            list.forEachIndexed { index, video ->
                video.takeIf { it.id == videoModel.id }?.let {
                    list.remove(list[index])
                    notifyItemRemoved(index)
                }
            }
        }catch (e:Exception) {e.printStackTrace()}
    }
}