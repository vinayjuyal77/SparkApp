package com.app.spark.activity.chat_profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.UsersProfileActivity
import com.app.spark.activity.chatroom.ChatRoomActivity
import com.app.spark.activity.group_chat_room.GroupChatRoomActivity
import com.app.spark.activity.main.MainActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityChatProfileBinding
import com.app.spark.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ChatProfileActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityChatProfileBinding
    private lateinit var chatProfileViewModel: ChatProfileViewModel
    private var userId = ""
    private var token = ""
    lateinit var pref: SharedPrefrencesManager
    private var chatID = ""
    private var name = ""
    private var chatImg = ""
    private var chatType = 2
    private var fromChat = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_profile)
        chatProfileViewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                ChatProfileViewModel::class.java
            )
        pref = SharedPrefrencesManager.getInstance(this)
        userId = pref.getString(PrefConstant.USER_ID, "")!!
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")!!
        intentValueGet()
        chatProfileViewModel.setInitialData(userId, chatID, chatType === 3)
        setClickListener()
        initializeViewModelObserver()
    }

    private fun setClickListener() {
        binding.imgBack.setOnClickListener(this)
        binding.imgChatOptionMenu.setOnClickListener(this)
        binding.imgChatMessage.setOnClickListener(this)
        binding.tvBlock.setOnClickListener(this)
        binding.tvReport.setOnClickListener(this)
        binding.imgProfilePic.setOnClickListener(this)
    }

    private fun intentValueGet() {
        chatID = intent.getStringExtra(IntentConstant.CHAT_ID).toString()
        name = intent.getStringExtra(IntentConstant.CHAT_NAME).toString()
        chatImg = intent.getStringExtra(IntentConstant.CHAT_IMG).toString()
        chatType = intent.getIntExtra(IntentConstant.CHAT_TYPE, 2)
        if (intent.hasExtra(IntentConstant.FROM_CHAT)) {
            fromChat = true
        }
        setChatUi()
        chatRoomHeaderBgSet(chatType)
    }

    private fun setChatUi() {
        binding.tvName.text = name
        Glide.with(this)
            .load(chatImg)
            .apply(RequestOptions().centerCrop())
            .into(binding.imgProfilePic)
    }

    private fun chatRoomHeaderBgSet(chatType: Int) {
        when (chatType) {
            0 -> {
                binding.bgView.setBackgroundResource(R.drawable.bg_chat_green_card)
                setTextColors(ContextCompat.getColor(this, R.color.green_text_color))
            }
            1 -> {
                binding.bgView.setBackgroundResource(R.drawable.bg_chat_blue_card)
                setTextColors(ContextCompat.getColor(this, R.color.blue_text_color))
            }
            2 -> {
                binding.bgView.setBackgroundResource(R.drawable.bg_chat_purple_card)
                setTextColors(ContextCompat.getColor(this, R.color.purple_text_color))
            }
            3 -> {
                binding.bgView.setBackgroundResource(R.drawable.bg_chat_yellow_card)
                setTextColors(ContextCompat.getColor(this, R.color.yellow_text_color))
                binding.tvBlock.setText(R.string.exit_group)
            }
        }
    }

    private fun setTextColors(color: Int) {
        binding.tabLayout.setTabTextColors(color, color)
        binding.tabLayout.setSelectedTabIndicatorColor(color)
        binding.tvMembers.setTextColor(color)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.imgBack -> finish()
            binding.imgChatOptionMenu -> {
            }
            binding.imgChatMessage -> {
                when {
                    fromChat -> {
                        finish()
                    }
                    chatType === 3 -> {
                        val intent = Intent(this, GroupChatRoomActivity::class.java)
                        intent.putExtra(IntentConstant.CHAT_ID, chatID)
                        intent.putExtra(IntentConstant.CHAT_NAME, name)
                        intent.putExtra(IntentConstant.CHAT_IMG, chatImg)
                        intent.putExtra(IntentConstant.CHAT_TYPE, chatType)
                        startActivity(intent)
                        finish()
                    }
                    else -> {
                        val intent = Intent(this, ChatRoomActivity::class.java)
                        intent.putExtra(IntentConstant.CHAT_ID, chatID)
                        intent.putExtra(IntentConstant.CHAT_NAME, name)
                        intent.putExtra(IntentConstant.CHAT_IMG, chatImg)
                        intent.putExtra(IntentConstant.CHAT_TYPE, chatType)
                        startActivity(intent)
                        finish()
                    }
                }
            }
            binding.tvBlock -> {
                if (chatType == 3) {
                    chatProfileViewModel.exitGroupChat("1")
                } else {
                    chatProfileViewModel.blockChat("1")
                }
            }
            binding.tvReport -> {
                if (chatType == 3) {
                    chatProfileViewModel.reportChat("1", "normal")
                } else {
                    chatProfileViewModel.reportChat("1", "group")
                }
            }

            binding.imgProfilePic -> {
                if (chatID != null) {
                    var userIds = pref.getString(PrefConstant.USER_ID, "")
                    if (userIds!!.trim().isNotEmpty() && !chatID.equals(userIds, true)) {
                        val ins = Intent(this@ChatProfileActivity, UsersProfileActivity::class.java)
                        ins.putExtra(IntentConstant.PROFILE_ID, chatID)
                        startActivity(ins)
                    }
                }
            }
        }
    }

    private fun initializeViewModelObserver() {

        chatProfileViewModel.userDetails.observe(this, {
            if (it != null) {
                if (!it.mediaData.isNullOrEmpty()) {
                    binding.rvMedia.adapter = ChatMediaAdapter(this, it.mediaData)
                    binding.rvMedia.addItemDecoration(GridSpacingItemDecoration(3, 20, false, 0))
                }

                if (!it.mutualFriends.isNullOrEmpty()) {
                    binding.llMembers.visibility=View.VISIBLE
                    binding.rvMembers.adapter = ChatMembersAdapter(this, it.mutualFriends)
                }else{
                    binding.llMembers.visibility=View.GONE
                }
            }
        })
        chatProfileViewModel.errString.observe(this, { err: String? ->
            if (err != null)
                showToastShort(this, err)
        })

        chatProfileViewModel.errRes.observe(this, { err: Int ->
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }
}