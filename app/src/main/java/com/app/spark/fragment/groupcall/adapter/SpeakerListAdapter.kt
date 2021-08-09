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
import com.app.spark.constants.AppConstants.Initilize.MAKE_HOST_SPEAKER
import com.app.spark.constants.AppConstants.Initilize.MAKE_OTHER_USER_SPEAKER
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ItemForSpeakersBinding
import com.app.spark.fragment.groupcall.callback.RoomClickListner
import com.app.spark.models.USER_INFO
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.SharedPrefrencesManager.Companion.getInstance
import com.bumptech.glide.Glide

class SpeakerListAdapter (var context: Context, var list : ArrayList<USER_INFO>,var listner: RoomClickListner) :  RecyclerView.Adapter<SpeakerListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemForSpeakersBinding>(
            LayoutInflater.from(context),
            R.layout.item_for_speakers,
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
        //viewModel= ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(this.getApplication());
        pref = getInstance(context)
        var userId = pref!!.getString(PrefConstant.USER_ID, "")
        holder.binding.ivHost.visibility=if(position==0) View.VISIBLE else View.GONE
        if(position==0)holder.binding.cvView.setBackgroundColor(context.getColor(R.color.green_txt_color))
        else holder.binding.cvView.setBackgroundColor(context.getColor(R.color.transparent))
        Glide.with(context).load(list[position].profile_pic)
            .into(holder.binding.cvView)
        if(list[position].user_id==userId!!.toInt()) holder.binding.userName.text=context.getString(
            R.string.you)
        else holder.binding.userName.text=list[position].name
        holder.bindView(list[position])
        holder.itemView.setOnClickListener {
            if (list[position].user_id != userId!!.toInt()) {
                if(userId!!.toInt()==hostUserId) {
                    listner.onSpeakerItemClick(
                        position, list[position], MAKE_HOST_SPEAKER
                    )
                }else {
                    listner.onSpeakerItemClick(
                        position, list[position], MAKE_OTHER_USER_SPEAKER
                    )
                }
            }
        }

    }
    var onItemSpeak: ((Int) -> Unit)? = null
    fun updateList(updatedList : ArrayList<USER_INFO>){
        list.clear()
        list.addAll(updatedList)
        notifyDataSetChanged()
    }
    fun setHostId(id : Int){
        this.hostUserId=id
    }
    inner class ViewHolder(val binding: ItemForSpeakersBinding) :  RecyclerView.ViewHolder(binding.root){
        @RequiresApi(Build.VERSION_CODES.M)
        fun bindView(item : USER_INFO){
            with(binding){
            }
        }
    }
}