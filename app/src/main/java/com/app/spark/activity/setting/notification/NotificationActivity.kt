package com.app.spark.activity.setting.notification

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spark.R
import com.app.spark.activity.setting.privacy.BlockedAdapter
import com.app.spark.activity.setting.privacy.BlockedViewModel
import com.app.spark.databinding.ActivityBlockedBinding
import com.app.spark.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityNotificationBinding
    lateinit var viewModel: NotificationViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification)

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                NotificationViewModel::class.java
            )
        binding.viewmodel = viewModel


//        binding.postandflick.setOnClickListener(this)
//        binding.likeandCommet.setOnClickListener(this)
//        binding.followingAndFollower.setOnClickListener(this)
//        binding.messages.setOnClickListener(this)
//        binding.emailsSms.setOnClickListener(this)


    }

    override fun onClick(p0: View?) {
       when(p0)
       {

           binding.postandflick ->
           {
               startActivity(Intent(this, PostAndFlickActivity::class.java))
           }

           binding.likeandCommet ->
           {
               startActivity(Intent(this, LikeandCommentActivity::class.java))
           }

           binding.followingAndFollower ->
           {
               startActivity(Intent(this, FollowingandFollowers::class.java))
           }

           binding.messages ->
           {
               startActivity(Intent(this, MessageActivity::class.java))
           } binding.emailsSms ->
       {
           startActivity(Intent(this, EmailAndSMSActivity::class.java))
       }


       }
    }
}