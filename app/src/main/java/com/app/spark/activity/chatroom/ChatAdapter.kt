package com.app.spark.activity.chatroom

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.PlayVideoActivity
import com.app.spark.activity.ShowImageActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ItemReceiverChatBinding
import com.app.spark.databinding.ItemSenderChatBinding
import com.app.spark.interfaces.AudioPlayerListener
import com.app.spark.interfaces.ChatMessageSelectedListener
import com.app.spark.models.ChatDetailResponse
import com.app.spark.models.ChatListObject
import com.app.spark.utils.date.DateTimeUtils
import com.app.spark.utils.setThumbnailFromUrl
import com.bumptech.glide.Glide
import java.io.IOException

class ChatAdapter(
    var context: Activity,
    val personalChatList: MutableList<ChatDetailResponse.Result.Data>,
    val senderId: String,
    val chatMessageSelectedListener: ChatMessageSelectedListener,
    val audioPlayerListener: AudioPlayerListener,
    val drawable: Drawable
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var inflater: LayoutInflater = LayoutInflater.from(context)
    var isSelectionInitiated = false
    var selectedMessageCount = 0
    var audio_value = 0
    public var mPlayer = MediaPlayer()




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        when (viewType) {
            ChatListObject.SENDER_MESSAGE -> {
                val senderBinding: ItemSenderChatBinding =
                    DataBindingUtil.inflate(inflater, R.layout.item_sender_chat, parent, false)
                viewHolder = SenderViewHolder(senderBinding)
            }
            ChatListObject.RECEIVER_MESSAGE -> {
                val receiverBinding: ItemReceiverChatBinding =
                    DataBindingUtil.inflate(inflater, R.layout.item_receiver_chat, parent, false)
                viewHolder = ReceiverViewHolder(receiverBinding)
            }
        }
        return viewHolder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ChatListObject.SENDER_MESSAGE -> {
                val senderViewHolder = holder as SenderViewHolder
                senderViewHolder.bindData(
                    personalChatList[position],
                    position,
                    chatMessageSelectedListener
                )
            }
            ChatListObject.RECEIVER_MESSAGE -> {
                val receiverViewHolder = holder as ReceiverViewHolder
                receiverViewHolder.bindData(
                    personalChatList[position],
                    position,
                    chatMessageSelectedListener
                )
            }
        }
    }

    private fun setSelectedCount(chatDataItem: ChatDetailResponse.Result.Data) {
        if (chatDataItem.isSelected) {
            selectedMessageCount--
        } else {
            selectedMessageCount++
        }
    }

    fun setChatSelectedStatus(isChatSelectionInitiated: Boolean) {
        isSelectionInitiated = isChatSelectionInitiated
    }

    override fun getItemCount(): Int {
        return personalChatList.size
    }

    override fun getItemViewType(position: Int): Int {
        return personalChatList[position].getType(senderId)
    }

    inner class SenderViewHolder(val senderBinding: ItemSenderChatBinding) :
        RecyclerView.ViewHolder(senderBinding.root) {
        fun bindData(
            item: ChatDetailResponse.Result.Data,
            position: Int,
            chatMessageSelectedListener: ChatMessageSelectedListener
        ) {
            if (item.msgType == "media") {
                when {
                    item.url!!.endsWith(".mp4") -> {
                        updateSenderUI(false, true, false, senderBinding)
                        setThumbnailFromUrl(context, senderBinding.imgMedia, item.url)
                    }
                    item.url.endsWith(".mp3") -> {
                        updateSenderUI(false, false, true, senderBinding)
                    }
                    else -> {
                        updateSenderUI(false, true, false, senderBinding)
                        Glide.with(context)
                            .load(item.url)
                            .centerCrop()
                            .into(senderBinding.imgMedia)
                    }
                }
            } else {
                updateSenderUI(
                    true,
                    isImageOrVideo = false,
                    isAudio = false,
                    senderBinding = senderBinding
                )
                senderBinding.tvText.text = item.msg
            }

            // Set message time
            senderBinding.txtMessageTime.text = DateTimeUtils.getCustomeDateAndTimeFromTimeStamp(
                item.dateAdded?.split("GMT")?.get(0),
                "yyyy-MM-dd HH:mm:ss",
                "hh:mm a"
            )
            senderBinding.clMessage.isSelected = item.isSelected
            if (selectedMessageCount <= 0) {
                isSelectionInitiated = false
            }


            senderBinding.clMessage.setOnLongClickListener {
                senderBinding.llTextOptions.visibility = View.GONE
                isSelectionInitiated = true
                setSelectedCount(item)

                //senderBinding.llSentMessage.setSelected(true);
                chatMessageSelectedListener.chatMessageSelected(
                    item,
                    position
                )
                true
            }
            senderBinding.imgMedia.setOnLongClickListener {
                isSelectionInitiated = true
                setSelectedCount(item)

                //senderBinding.llSentMessage.setSelected(true);
                chatMessageSelectedListener.chatMessageSelected(
                    item,
                    position
                )
                true
            }

            senderBinding.clMessage.setOnClickListener(View.OnClickListener {
                if (isSelectionInitiated) {
                    setSelectedCount(item)
                    chatMessageSelectedListener.chatMessageSelected(
                        item,
                        position
                    )
                }
            })

            senderBinding.llText.setOnClickListener {
                chatMessageSelectedListener.textMessageClick(
                    item,
                    position,
                    senderBinding.llTextOptions
                )
            }
            senderBinding.imgDeleteMsg.setOnClickListener {
                chatMessageSelectedListener.deleteMessage(item, position)
            }
            senderBinding.imgCopyMsg.setOnClickListener {

                chatMessageSelectedListener.copyMessage(item, position)

            }
            senderBinding.imgMedia.setOnClickListener(View.OnClickListener {
                if (isSelectionInitiated) {
                    setSelectedCount(item)
                    chatMessageSelectedListener.chatMessageSelected(
                        item,
                        position
                    )
                } else {
                    if (item.msgType == "media") {
                        when {
                            item.url!!.endsWith(".mp4") -> {
                                val intent = Intent(context, PlayVideoActivity::class.java)
                                intent.putExtra(
                                    IntentConstant.VIDEO_URL,
                                    item.url
                                )
                                context.startActivity(intent)
                            }
                            else -> {
                                val intent = Intent(context, ShowImageActivity::class.java)
                                intent.putExtra(
                                    IntentConstant.PICTURE,
                                    item.url
                                )
                                context.startActivity(intent)
                            }
                        }
                    }
                }
            })


            senderBinding.imgAudio.setOnClickListener(View.OnClickListener {
                if (item.url!!.endsWith(".mp3")) {


                        senderBinding.imgAudio.setImageResource(R.drawable.exo_ic_pause_circle_filled)
                          audioPlayerListener.playAudio(item, position)
                       // playaudio(item, position)


                }
            })
        }


        fun playaudio(dataItem: ChatDetailResponse.Result.Data?, position: Int)
        {

            try {
//audioMessageFile is global string. it contains the Uri to the recently recorded audio.


                    mPlayer!!.setDataSource(dataItem?.url)
                    mPlayer!!.prepare()
                    mPlayer!!.start()

//                    var time = mPlayer!!.duration
//                    Handler(Looper.getMainLooper()).postDelayed(
//                        Runnable {
//                            senderBinding.imgAudio.setImageResource(R.drawable.exo_ic_play_circle_filled)
//                            mPlayer!!.stop()
//                            mPlayer!!.release()
//
//                            audio_value = 0
//
//                        },
//                        time.toLong()
//                    )

            } catch (e: IOException) {
                    Log.e("LOG_TAG", "prepare() failed")
                }

        }


        fun stop_audio( )
        {
          //  mPlayer = MediaPlayer()
            try {
                if (mPlayer!!.isPlaying) {
                    senderBinding.imgAudio.setImageResource(R.drawable.exo_ic_play_circle_filled)
                    mPlayer!!.stop()
                    mPlayer!!.reset();
                    audio_value = 0
                    //   mPlayer.seekTo(0)
                }
            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare() failed")
            }
        }

        private fun updateSenderUI(
            isTextMessage: Boolean,
            isImageOrVideo: Boolean,
            isAudio: Boolean,
            senderBinding: ItemSenderChatBinding
        ) {
            if (isTextMessage) {
                senderBinding.tvText.visibility = View.VISIBLE
            } else {
                senderBinding.tvText.visibility = View.GONE
            }
            if (isImageOrVideo) {
                senderBinding.imgMedia.visibility = View.VISIBLE
            } else {
                senderBinding.imgMedia.visibility = View.GONE
            }
            if (isAudio) {
                senderBinding.imgAudio.visibility = View.VISIBLE
                senderBinding.imgAudio.background = drawable
            } else {
                senderBinding.imgAudio.visibility = View.GONE
            }
        }

    }

    inner class ReceiverViewHolder(val receiverBinding: ItemReceiverChatBinding) :
        RecyclerView.ViewHolder(receiverBinding.root) {
        fun bindData(item: ChatDetailResponse.Result.Data, position: Int, chatMessageSelectedListener: ChatMessageSelectedListener
        ) {
            if (item.msgType == "media") {
                when {
                    item.url!!.endsWith(".mp4") -> {
                        updateReceiverUI(
                            false,
                            true,
                            false,
                            receiverBinding
                        )
                        setThumbnailFromUrl(context, receiverBinding.imgMedia, item.url)
                    }
                    item.url.endsWith(".mp3") -> {
                        updateReceiverUI(
                            false,
                            false,
                            true,
                            receiverBinding
                        )
                    }
                    else -> {
                        updateReceiverUI(
                            false,
                            true,
                            false,
                            receiverBinding
                        )
                        Glide.with(context)
                            .load(item.url)
                            .centerCrop()
                            .into(receiverBinding.imgMedia)
                    }
                }
                chatMessageSelectedListener.downloadMedia(item.url)




            } else {
                updateReceiverUI(
                    true,
                    false,
                    false,
                    receiverBinding
                )
                receiverBinding.tvText.setText(
                    item.msg
                )

            }
            // Set message time
            receiverBinding.txtMessageTime.text = DateTimeUtils.getCustomeDateAndTimeFromTimeStamp(
                item.dateAdded?.split("GMT")?.get(0),
                "yyyy-MM-dd HH:mm:ss",
                "hh:mm a"
            )
            receiverBinding.clMessage.isSelected = item.isSelected
            if (selectedMessageCount <= 0) {
                isSelectionInitiated = false
            }


            /* if (position == personalChatList.size() - 1) {
                setDateVisibility(receiverBinding.txtDateHeader, "", "", position, true);
              */
            /*  receiverBinding.txtDateHeader.setVisibility(View.VISIBLE);
                receiverBinding.txtDateHeader.setText(DateUtils.chatTimeFormat(context, "yyyy-MM-dd HH:mm:ss", "dd-MMM, yyyy", personalChatList.get(position).getDateAdded()));
        */
            /*
            } else {
                int previousPosition = 0;
                // if (position < (personalChatList.size() - 1)) {
                //previousPosition = position + 1;
                previousPosition = position - 1;
                setDateVisibility(receiverBinding.txtDateHeader, personalChatList.get(position).getDateAdded(), personalChatList.get(previousPosition).getDateAdded(), position, false);
                //  } else {
                // previousPosition = position - 1;
                // setDateVisibility(senderBinding.txtDateHeader, personalChatList.get(position).getDateAdded(), personalChatList.get(previousPosition).getDateAdded(), position);
                // }
            }*/
            receiverBinding.clMessage.setOnLongClickListener {
                isSelectionInitiated = true
                setSelectedCount(item)
                chatMessageSelectedListener.chatMessageSelected(
                    item,
                    position
                )
                //setChatSelected();
                //receiverBinding.rlReceiverParent.setSelected(true);
                true
            }
            receiverBinding.imgMedia.setOnLongClickListener {
                isSelectionInitiated = true
                setSelectedCount(item)
                chatMessageSelectedListener.chatMessageSelected(
                    item,
                    position
                )
                //setChatSelected();
                //receiverBinding.rlReceiverParent.setSelected(true);
                true
            }
            receiverBinding.clMessage.setOnClickListener(View.OnClickListener {
                if (isSelectionInitiated) {
                    setSelectedCount(item)
                    chatMessageSelectedListener.chatMessageSelected(
                        item,
                        position
                    )
                } else {
                }
            })
            receiverBinding.imgMedia.setOnClickListener(View.OnClickListener {
                if (isSelectionInitiated) {
                    setSelectedCount(item)
                    chatMessageSelectedListener.chatMessageSelected(
                        item,
                        position
                    )
                } else {
                    if (item.msgType == "media") {
                        when {
                            item.url!!.endsWith(".mp4") -> {
                                val intent = Intent(context, PlayVideoActivity::class.java)
                                intent.putExtra(
                                    IntentConstant.VIDEO_URL,
                                    item.url
                                )
                                context.startActivity(intent)
                            }
                            else -> {
                                val intent = Intent(context, ShowImageActivity::class.java)
                                intent.putExtra(
                                    IntentConstant.PICTURE,
                                    item.url
                                )
                                context.startActivity(intent)
                            }
                        }
                    }
                }
            })
            receiverBinding.imgAudio.setOnClickListener(View.OnClickListener {
                if (item.url!!.endsWith(".mp3")) {
                    audioPlayerListener.playAudio(item, position)
                }
            })
        }

        private fun updateReceiverUI(
            isTextMessage: Boolean,
            isImageOrVideo: Boolean,
            isAudio: Boolean,
            receiverBinding: ItemReceiverChatBinding
        ) {
            if (isTextMessage) {
                receiverBinding.tvText.visibility = View.VISIBLE
            } else {
                receiverBinding.tvText.visibility = View.GONE
            }
            if (isImageOrVideo) {
                receiverBinding.imgMedia.visibility = View.VISIBLE
            } else {
                receiverBinding.imgMedia.visibility = View.GONE
            }
            if (isAudio) {
                receiverBinding.imgAudio.visibility = View.VISIBLE
              //  receiverBinding.imgAudio.background = drawable
            } else {
                receiverBinding.imgAudio.visibility = View.GONE
            }
        }
    }
}