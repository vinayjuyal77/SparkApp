package com.app.spark.fragment.groupcall.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.databinding.ItemGroupListBinding
import com.app.spark.models.GetRoomResponse
import com.app.spark.models.ResultFollowing
import com.app.spark.models.USER_INFO
import com.bumptech.glide.Glide

class GroupListAdapter(var context:Context,private val list: ArrayList<GetRoomResponse.UserData>)
    : RecyclerView.Adapter<GroupListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGroupListBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size
    fun updateList(updatedList : GetRoomResponse){
        list.clear()
        list.addAll(updatedList.result)
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            if (!list[position].room.room_title.isNullOrEmpty())
                holder.binding.groupName.text = list[position].room.room_title
            if (!list[position].room.user_count.toString()
                    .isNullOrEmpty()
            ) holder.binding.tvTotalUser.text = list[position].room.user_count.toString()

            if (!list[position].room.is_active.toString()
                    .isNullOrEmpty()
            ) holder.binding.tvActiveUser.text = list[position].room.is_active.toString()
            holder.itemView.setOnClickListener {
                try {
                    onItemListener?.invoke(position, list[position].room.room_id)
                }catch (e:Exception){e.printStackTrace()}
            }
            /*holder.itemView.setOnLongClickListener {
                onLongPress?.invoke(
                    position,
                    list[position].room.room_id,
                    list[position].room.created_by
                )
                true
            }*/

            if (!list[position].users.isNullOrEmpty()) {
                if (list[position].users.size >= 1) {
                    Glide.with(context).load(list[position].users[0].profile_pic)
                        .into(holder.binding.circleImageView)
                    holder.binding.tvFirstName.text = list[position].users[0].name
                }
                if (list[position].users.size >= 2) {
                    Glide.with(context).load(list[position].users[1].profile_pic)
                        .into(holder.binding.circleImageView3)
                    holder.binding.tvSecondName.text = list[position].users[1].name
                }
                if (list[position].users.size >= 3) {
                    Glide.with(context).load(list[position].users[2].profile_pic)
                        .into(holder.binding.circleImageView2)
                    holder.binding.tvThirdSecondName.text = list[position].users[2].name
                }
            }
        }catch (e:Exception){e.printStackTrace()}

//                val hours = "$hours learning hours, $country"
//                binding.topLearnerTime.text = hours
//                GlideApp.with(holder.itemView.context)
//                    .load(badgeUrl)
//                    .into(binding.topLearnerImage)
//
//                holder.itemView.setOnClickListener {
//                    Toast.makeText(holder.itemView.context, hours,
//                        Toast.LENGTH_SHORT).show()

        }

    fun onDeleteItem(postion: Int) {
        if(postion!=-1) {
            list.removeAt(postion)
            notifyDataSetChanged()
        }
    }

    var onItemListener: ((Int,Int) -> Unit)? = null
 //   var onLongPress: ((Int,Int,Int) -> Unit)? = null
    inner class ViewHolder(val binding: ItemGroupListBinding)
        :RecyclerView.ViewHolder(binding.root)

}
