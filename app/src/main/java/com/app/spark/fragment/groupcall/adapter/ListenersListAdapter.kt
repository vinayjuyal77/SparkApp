package com.app.spark.fragment.groupcall.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.constants.AppConstants
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ItemForListenersBinding
import com.app.spark.fragment.groupcall.callback.RoomClickListner
import com.app.spark.models.GetListResponse
import com.app.spark.models.USER_INFO
import com.app.spark.utils.SharedPrefrencesManager
import com.bumptech.glide.Glide

class ListenersListAdapter (var context: Context, var list : ArrayList<USER_INFO>,var listner: RoomClickListner) :  RecyclerView.Adapter<ListenersListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemForListenersBinding>(
            LayoutInflater.from(context),
            R.layout.item_for_listeners,
            parent,
            false
        )
        return ViewHolder(binding)
    }
    var pref: SharedPrefrencesManager? = null
    var hostUserId:Int=-1
    override fun getItemCount() = list.size
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(list[position])
        pref = SharedPrefrencesManager.getInstance(context)
        var userId = pref!!.getString(PrefConstant.USER_ID, "")
        Glide.with(context).load(list[position].profile_pic)
            .into(holder.binding.cvView)
        if(list[position].user_id==userId!!.toInt()) holder.binding.userName.text=context.getString(
                    R.string.you)
        else holder.binding.userName.text=list[position].name
        //binding.userName.text=list[position].name
        holder.bindView(list[position])

        holder.itemView.setOnClickListener {
          /*  if(list[position].user_id!=userId!!.toInt()) listner.onListnerItemClick(position,list[position].user_id,
                list[position].profile_pic, AppConstants.Initilize.MAKE_HOST_LISTENER
            )else {
                listner.onListnerItemClick(position,list[position].user_id,
                    list[position].profile_pic, AppConstants.Initilize.MAKE_OTHER_USER_LISTENER)
            }*/
            if (list[position].user_id != userId!!.toInt()) {
                if(userId!!.toInt()==hostUserId) {
                    listner.onListnerItemClick(
                        position, list[position], AppConstants.Initilize.MAKE_HOST_LISTENER
                    )
                }else {
                    listner.onListnerItemClick(
                        position, list[position], AppConstants.Initilize.MAKE_OTHER_USER_LISTENER
                    )
                }
            }

        }
    }
    var onItemListener: ((Int) -> Unit)? = null
    fun setHostId(id : Int){
        this.hostUserId=id
    }
    fun updateList(updatedList : ArrayList<USER_INFO>){
        list.clear()
        list.addAll(updatedList)
        notifyDataSetChanged()
    }
    inner class ViewHolder(val binding: ItemForListenersBinding) :  RecyclerView.ViewHolder(binding.root){
        @RequiresApi(Build.VERSION_CODES.M)
        fun bindView(item : USER_INFO){
            with(binding){
            }
        }
    }
}