package com.app.spark.activity.explore

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.ActivityFriendChatBinding
import com.app.spark.dialogs.ChatDiloagReport
import com.app.spark.utils.BaseActivity
import com.app.spark.utils.date.getRevealTime


class FriendChatActivity : BaseActivity() {
    private lateinit var binding: ActivityFriendChatBinding
    private var colorCode=-1
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_friend_chat)
        binding.activity = this
        when {
            intent.action!!.equals(getString(R.string.friend))-> {
                setEventAction(R.drawable.bg_green_ractangle,getColor(R.color.green_28C76F),
                    R.drawable.half_chat_view)
            }
            intent.action!!.equals(getString(R.string.date))-> {
                setEventAction(R.drawable.bg_red_ractangle,getColor(R.color.red_F94757),
                    R.drawable.half_chat_view_red)
            }
            intent.action!!.equals(getString(R.string.professional))-> {
                setEventAction(R.drawable.bg_blue_ractangle,getColor(R.color.bg_button_blue),
                    R.drawable.half_chat_view_blue)
            }
        }
        binding.ivMenu.setOnClickListener{
            ChatDiloagReport.Builder(this)
                .setColorText(colorCode)
                .setOkFunction {}
                .build()
        }
        setTimmer()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        cTimer!!.cancel()
    }

    var cTimer: CountDownTimer? = null
    private fun setTimmer() {
        cTimer=object : CountDownTimer(9000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTime.text= getRevealTime(millisUntilFinished)
            }
            override fun onFinish() {
                startActivity(Intent(applicationContext, SessionOverActivity::class.java).apply { action=intent.action})
            }
        }.start()
    }

    override fun onRestart() {
        super.onRestart()
        setTimmer()
    }

    private fun setEventAction(bgRactangle: Int, color: Int, bgChat: Int) {
        colorCode=color
        binding.toolbar.setBackgroundResource(bgRactangle)
        binding.llBottomChat.setBackgroundResource(bgChat)
        binding.tvRevealIdentity.setTextColor(color)
        binding.tvTime.setTextColor(color)
    }
}