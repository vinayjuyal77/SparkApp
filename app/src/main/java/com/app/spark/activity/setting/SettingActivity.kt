package com.app.spark.activity.setting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.search.SearchUserViewModel
import com.app.spark.activity.setting.account.YourAccountActivity
import com.app.spark.activity.setting.buyservice.BuyServiceActivity
import com.app.spark.activity.setting.notification.NotificationActivity
import com.app.spark.activity.setting.privacy.PrivacyActivity
import com.app.spark.activity.setting.purchase.PurchaseActivity
import com.app.spark.activity.setting.security.SecurityActivity
import com.app.spark.databinding.ActivitySettingBinding
import com.app.spark.utils.SharedPrefrencesManager

class SettingActivity : AppCompatActivity() {

    lateinit var binding : ActivitySettingBinding
    lateinit var viewModel : SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            //  setContentView(R.layout.activity_setting)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_setting)
           viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                SettingViewModel::class.java
            )




        binding.yourAccount.setOnClickListener {

            val intit = Intent(this, YourAccountActivity::class.java)
            startActivity(intit)
        }



        binding.buyService.setOnClickListener {

            val intit = Intent(this, BuyServiceActivity::class.java)
            startActivity(intit)
        }


        binding.privacy.setOnClickListener {

            val intit = Intent(this, PrivacyActivity::class.java)
            startActivity(intit)
        }



        binding.notification.setOnClickListener {

            val intit = Intent(this, NotificationActivity::class.java)
            startActivity(intit)
        }

        binding.security.setOnClickListener {

            val intit = Intent(this, SecurityActivity::class.java)
            startActivity(intit)
        }


        binding.purchase.setOnClickListener {

            val intit = Intent(this, PurchaseActivity::class.java)
            startActivity(intit)
        }


        binding.back.setOnClickListener {

           onBackPressed()
        }


    }
}