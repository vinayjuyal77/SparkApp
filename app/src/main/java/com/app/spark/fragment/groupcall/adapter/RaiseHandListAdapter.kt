package com.app.spark.fragment.groupcall.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.databinding.ItemForAddUserBinding
import com.app.spark.databinding.ItemForRaiseHandListBinding
import com.app.spark.models.RaiseHandListResponse

class RaiseHandListAdapter (var context: Context, var list : ArrayList<RaiseHandListResponse.ActiveUser>) :  RecyclerView.Adapter<RaiseHandListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemForRaiseHandListBinding>(
            LayoutInflater.from(context),
            R.layout.item_for_raise_hand_list,
            parent,
            false
        )
        return ViewHolder(binding)
    }
    override fun getItemCount() = list.size
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(list[position])
    }
    var onItemAllowListener: ((Int,Int,Int,Int) -> Unit)? = null
    fun updateList(updatedList : RaiseHandListResponse.ActiveUser){
        val oldSize = list.size
        list.add(updatedList)
        notifyItemRangeInserted(oldSize,itemCount)
    }
    inner class ViewHolder(val binding: ItemForRaiseHandListBinding) :  RecyclerView.ViewHolder(binding.root){
        @RequiresApi(Build.VERSION_CODES.M)
        fun bindView(item : RaiseHandListResponse.ActiveUser){
            with(binding){
                binding.tvUserName.text=item.name
                binding.tvAllow.setOnClickListener {
                    onItemAllowListener?.invoke(adapterPosition,item.id,item.room_id,item.user_id)
                }
            }
        }
    }
}