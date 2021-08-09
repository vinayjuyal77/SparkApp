package com.app.spark.fragment.groupcall.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.databinding.ItemForAddUserBinding
import com.app.spark.models.ResultFollowing
import com.bumptech.glide.Glide

class AddUserListAdapter (var context: Context, var list : ArrayList<ResultFollowing>) :  RecyclerView.Adapter<AddUserListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemForAddUserBinding>(
            LayoutInflater.from(context),
            R.layout.item_for_add_user,
            parent,
            false
        )
        return ViewHolder(binding)
    }
    override fun getItemCount() = list.size
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(list[position])
        holder.itemView.setOnClickListener {
            onItemListener?.invoke(position)
        }
    }
    var onItemListener: ((Int) -> Unit)? = null
    fun updateList(updatedList : ResultFollowing){
        val oldSize = list.size
        list.add(updatedList)
        notifyItemRangeInserted(oldSize,itemCount)
    }
    inner class ViewHolder(val binding: ItemForAddUserBinding) :  RecyclerView.ViewHolder(binding.root){
        @RequiresApi(Build.VERSION_CODES.M)
        fun bindView(item : ResultFollowing){
            with(binding){
                binding.tvUserId.text = item.username
                binding.tvUserName.text = item.name
                loadImagesProfile(context, item.profilePic, "", binding.imgProfilePic)
                binding.appCompatCheckBox.isChecked=item.isSelected
                binding.appCompatCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    item.isSelected=isChecked
                }
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