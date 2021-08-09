package com.app.spark.fragment.chat

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.CodeBoy.MediaFacer.mediaHolders.pictureContent
import com.app.spark.R
import com.app.spark.activity.chat_profile.ChatProfileActivity
import com.app.spark.activity.chatroom.ChatRoomActivity
import com.app.spark.activity.group_chat_room.GroupChatRoomActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ItemCoversationListBinding
import com.app.spark.databinding.ItemGalleryBinding
import com.app.spark.dialogs.ChatProfileDialog
import com.app.spark.interfaces.ChatConversationListInterface
import com.app.spark.interfaces.ChatProfileDialogListner
import com.app.spark.interfaces.OnItemSelectedInterface
import com.app.spark.models.ConversationListResponse
import com.app.spark.models.MediaModel
import com.app.spark.utils.date.DateTimeUtils
import com.app.spark.utils.setThumbnailFromUrl
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import java.io.File
import java.util.ArrayList


class GroupChatAdapter(
    var context: Context,
    val list: MutableList<ConversationListResponse.Result>,
    val chatType: Int, val chatConversationListInterface: ChatConversationListInterface
) :
    RecyclerView.Adapter<GroupChatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemCoversationListBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_coversation_list,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position], position)
    }


    inner class ViewHolder(var binding: ItemCoversationListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: ConversationListResponse.Result,
            position: Int
        ) {
            binding.tvUserName.text = item.name
            if (!item.msg.isNullOrEmpty()) {
                binding.tvMessage.text = item.msg
            } else if (!item.action.isNullOrEmpty()) {
                binding.tvMessage.text = item.action
            } else if (!item.mediaType.isNullOrEmpty()) {
                binding.tvMessage.text = item.mediaType
            }
            binding.tvTime.text = DateTimeUtils.getCustomeDateAndTimeFromTimeStamp(
                item.dateAdded.split("GMT")[0],
                "EEE MMM dd yyyy HH:mm:ss",
                "hh:mm a"
            )
            Glide.with(context).load(item.profilePic).placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(binding.imgProfilePic)

            binding.rlParent.setOnClickListener {
                val intent = Intent(context, GroupChatRoomActivity::class.java)
                intent.putExtra(IntentConstant.CHAT_ID, item.userId)
                intent.putExtra(IntentConstant.CHAT_NAME, item.name)
                intent.putExtra(IntentConstant.CHAT_IMG, item.profilePic)
                intent.putExtra(IntentConstant.CHAT_TYPE, chatType)
                context.startActivity(intent)
            }
            if (item.isAdmin == "1") {
                binding.imgDelete.visibility = View.VISIBLE
            } else {
                binding.imgDelete.visibility = View.GONE
            }
            binding.imgDelete.setOnClickListener {
                chatConversationListInterface.onDelete(item.userId)
            }
            binding.imgPin.setOnClickListener {
                chatConversationListInterface.onPin(item.userId, item.pinStatus)

            }
            binding.imgMute.setOnClickListener {
                chatConversationListInterface.onMute(item.userId, item.muteStatus)

            }

            binding.imgProfilePic.setOnClickListener {
                ChatProfileDialog(
                    context,
                    item.profilePic,
                    item.name,
                    object : ChatProfileDialogListner {
                        override fun onChatMessage() {
                            val intent = Intent(context, GroupChatRoomActivity::class.java)
                            intent.putExtra(IntentConstant.CHAT_ID, item.userId)
                            intent.putExtra(IntentConstant.CHAT_NAME, item.name)
                            intent.putExtra(IntentConstant.CHAT_IMG, item.profilePic)
                            intent.putExtra(IntentConstant.CHAT_TYPE, 3)
                            context.startActivity(intent)
                        }

                        override fun onInfo() {
                            val intent = Intent(context, ChatProfileActivity::class.java)
                            intent.putExtra(IntentConstant.CHAT_ID, item.userId)
                            intent.putExtra(IntentConstant.CHAT_NAME, item.name)
                            intent.putExtra(IntentConstant.CHAT_IMG, item.profilePic)
                            intent.putExtra(IntentConstant.CHAT_TYPE, 3)
                            context.startActivity(intent)
                        }

                    }).show()
            }

        }
    }
}