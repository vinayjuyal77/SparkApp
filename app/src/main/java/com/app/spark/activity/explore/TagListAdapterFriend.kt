package com.app.spark.activity.explore

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.databinding.ItemTagListBinding
import com.app.spark.models.TagListModel

class TagListAdapterFriend (var context: Context, var list : ArrayList<TagListModel>,var colorCode: Int) :  RecyclerView.Adapter<TagListAdapterFriend.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemTagListBinding>(
            LayoutInflater.from(context),
            R.layout.item_tag_list,
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
            onItemClick?.invoke(list[position])
        }
    }
    var onItemClick: ((TagListModel) -> Unit)? = null
    fun refreshList(newList : ArrayList<TagListModel>?){
        this.list.clear()
        if(newList != null) {
            this.list.addAll(newList)
        }
        notifyDataSetChanged()
    }
    fun updateList(updatedList : TagListModel){
        val oldSize = list.size
        list.add(updatedList)
        notifyItemRangeInserted(oldSize,itemCount)
    }

    inner class ViewHolder(val binding: ItemTagListBinding) :  RecyclerView.ViewHolder(binding.root){
        @RequiresApi(Build.VERSION_CODES.M)
        fun bindView(item : TagListModel){
            with(binding){
                model = item
                if(list[adapterPosition].isEditable) binding.imgCancel.visibility=View.VISIBLE
                else binding.imgCancel.visibility=View.GONE
                if(list[adapterPosition].isSelected)
                    binding.tvTagName.apply {
                        setTextColor(context.getColor(R.color.white))
                        binding.llBack.backgroundTintList=context.getColorStateList(colorCode)
                    }
                else binding.tvTagName.apply {
                    setTextColor(context.getColor(R.color.black_new))
                    binding.llBack.backgroundTintList=context.getColorStateList(R.color.bg_chat)
                }
                binding.tvTagName.setOnClickListener {
                    if(list[adapterPosition].isSelected) {
                        list[adapterPosition].isSelected=false
                        binding.tvTagName.apply {
                            setTextColor(context.getColor(R.color.black_new))
                            binding.llBack.backgroundTintList=context.getColorStateList(R.color.bg_chat)
                        }
                    }
                    else {
                        list[adapterPosition].isSelected=true
                        binding.tvTagName.apply {
                            setTextColor(context.getColor(R.color.white))
                            binding.llBack.backgroundTintList=context.getColorStateList(colorCode)
                        }
                    }
                }
                binding.imgCancel.setOnClickListener {
                    list.remove(list[adapterPosition])
                    notifyItemRemoved(adapterPosition)
                }
            }
        }
    }
}