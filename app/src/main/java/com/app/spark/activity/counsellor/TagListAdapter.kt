package com.app.spark.activity.counsellor

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.databinding.ItemTagListBinding
import com.app.spark.models.TagListModel

internal class TagListAdapter(var context: Context,var list : ArrayList<TagListModel>) :  RecyclerView.Adapter<TagListAdapter.ViewHolder>() {
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
    fun updateList(updatedList : ArrayList<TagListModel>){
        val oldSize = list.size
        this.list.addAll(updatedList)
        notifyItemRangeInserted(oldSize,itemCount)
    }

    inner class ViewHolder(val binding: ItemTagListBinding) :  RecyclerView.ViewHolder(binding.root){
        @RequiresApi(Build.VERSION_CODES.M)
        fun bindView(item : TagListModel){
            with(binding){
                model = item
                if(list[adapterPosition].isSelected)
                    binding.tvTagName.apply {
                        setTextColor(context.getColor(R.color.white))
                        binding.llBack.setBackgroundResource(R.drawable.bg_select_yellow)
                    }
                else binding.tvTagName.apply {
                    setTextColor(context.getColor(R.color.black_new))
                    binding.llBack.setBackgroundResource(R.drawable.round_select_gray)
                }

                binding.tvTagName.setOnClickListener {
                    if(list[adapterPosition].isSelected) {
                        list[adapterPosition].isSelected=false
                        binding.tvTagName.apply {
                            setTextColor(context.getColor(R.color.black_new))
                            binding.llBack.setBackgroundResource(R.drawable.round_select_gray)
                        }
                    }
                    else {
                        list[adapterPosition].isSelected=true
                        binding.tvTagName.apply {
                            setTextColor(context.getColor(R.color.white))
                            binding.llBack.setBackgroundResource(R.drawable.bg_select_yellow)
                        }
                    }
                }
            }
        }
    }
}

