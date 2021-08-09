package com.app.spark.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.chatroom.ChatRoomActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ItemFollowFollowingBinding
import com.app.spark.databinding.ItemSerachListBinding
import com.app.spark.models.ResultFollowing
import com.app.spark.utils.returnChatType
import com.bumptech.glide.Glide


class SearchUserListAdaptor(
    var context: Context, val list: MutableList<ResultFollowing>,
    val listner: UserSearchListner, val isFromChat: Boolean
) :
    RecyclerView.Adapter<SearchUserListAdaptor.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemSerachListBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_serach_list,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(context, list[position], position)
    }

    inner class ViewHolder(var binding: ItemSerachListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(context: Context, item: ResultFollowing, position: Int) {
            binding.tvName.text = item.username
            binding.tvUserName.text = item.name
            binding.tvFollow.visibility = View.GONE
            loadImagesProfile(context, item.profilePic, "", binding.imgProfilePic)
         //   if (!isFromChat) {
                binding.imgProfilePic.setOnClickListener {
                    listner.profileDetails(item)
                }
                binding.tvName.setOnClickListener {
                    listner.profileDetails(item)
                }
                binding.tvUserName.setOnClickListener {
                    listner.profileDetails(item)
                }
         //   } else {
                binding.rlUser.setOnClickListener {

                    listner.profileDetails(item)
//                    val intent = Intent(context, ChatRoomActivity::class.java)
//                    intent.putExtra(IntentConstant.CHAT_ID, item.userId)
//                    intent.putExtra(IntentConstant.CHAT_NAME, item.name)
//                    intent.putExtra(IntentConstant.CHAT_IMG, item.profilePic)
//                    intent.putExtra(IntentConstant.CHAT_TYPE, returnChatType(item.follow_Group))
//                    context.startActivity(intent)
                }
          //  }
        }
    }

    private fun openChat(item: ResultFollowing) {

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

    interface UserSearchListner {
        fun profileDetails(model: ResultFollowing)
    }


}