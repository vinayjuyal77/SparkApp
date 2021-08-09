package com.app.spark.activity.custom_gallery

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getColorStateList
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.camera.utill_camera.getSongThumbnail
import com.app.spark.activity.camera.utill_camera.timeConversion
import com.app.spark.databinding.ItemVideoListBinding
import com.bumptech.glide.Glide

class VideoListAdapter (
    val context: Context,
    private var list: MutableList<VideoModel>
) : RecyclerView.Adapter<VideoListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemVideoListBinding>(
            LayoutInflater.from(context),
            R.layout.item_video_list,
            parent,
            false
        )
        return ViewHolder(binding)
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val videoItem = list[position]
        //list.sortByDescending{it.date}
        Glide.with(context).asBitmap().load(videoItem.path).error(R.drawable.ic_baseline_video_call_24)
            .into(holder.binding.imageView)
        holder.binding.tvTime.text=if(videoItem.type) timeConversion(videoItem.duration) else ""
        holder.binding.ivVideo.visibility=if(videoItem.type) View.VISIBLE else View.GONE
        holder.binding.constrainntLayout.backgroundTintList= getColorStateList(context,if(videoItem.isSelect) R.color.select_green else android.R.color.transparent)
        holder.itemView.setOnClickListener {
            if(list.count { it.isSelect}<5 || videoItem.isSelect) {
                if (videoItem.isSelect) videoItem.isSelect = false
                else videoItem.isSelect = true
                onItemClick?.invoke(position, videoItem)
                notifyItemChanged(position)
            }else Toast.makeText(context,"only 5 item is selected at a time",Toast.LENGTH_LONG).show()
        }
    }

    var onItemClick: ((Int,VideoModel) -> Unit)? = null
    override fun getItemCount(): Int {
        return list.size
    }
    class ViewHolder(val binding: ItemVideoListBinding) :  RecyclerView.ViewHolder(binding.root){}
    fun updateList(songList: List<VideoModel>) {
        this.list = ArrayList(songList)
        notifyDataSetChanged()
    }

    fun isSelected(id: Long) {
        list.forEachIndexed { index, video ->
            video.takeIf { it.id == id}?.let {
                list[index].isSelect=false
                notifyItemChanged(index)
            }
        }
    }
}