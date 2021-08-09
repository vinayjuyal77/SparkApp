package com.app.spark.activity.setting.security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spark.R
import com.app.spark.databinding.ActivityAuthenticationBinding
import com.app.spark.databinding.ActivityLoginInfoBinding

class AuthenticationActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityAuthenticationBinding
    lateinit var viewModel: AuthenticationViewModel




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_setting)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                AuthenticationViewModel::class.java
            )


        binding.authenticationapp.setOnClickListener(this)
        binding.textMessage.setOnClickListener(this)
        binding.additionnMethod.setOnClickListener(this)
        binding.backupCode.setOnClickListener(this)
    }

        override fun onClick(p0: View?) {
        when(p0) {

            binding.authenticationapp ->
            {



                startActivity(Intent(this, AuthenticationAppActivity::class.java))
            }

            binding.textMessage ->
            {



                startActivity(Intent(this, TextMessageActivity::class.java))
            }

            binding.additionnMethod ->
            {



                startActivity(Intent(this, AdditionalMethodActivity::class.java))
            }

            binding.backupCode ->
            {



                startActivity(Intent(this, RecoveryActivity::class.java))
            }

        }
    }
}