package com.app.spark.activity.setting.security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.setting.privacy.BlockedActivity
import com.app.spark.activity.setting.privacy.PrivacyViewModel
import com.app.spark.databinding.ActivityPrivacyBinding
import com.app.spark.databinding.ActivitySecurityBinding

class SecurityActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivitySecurityBinding
    lateinit var viewModel: SecurityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_setting)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_security)
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                SecurityViewModel::class.java
            )


        binding.back.setOnClickListener {

            onBackPressed()

        }

        binding.changeppassword.setOnClickListener(this)
        binding.logininfo.setOnClickListener(this)
        binding.twoFactorAuthen.setOnClickListener(this)
        binding.emailConnectd.setOnClickListener(this)
        binding.passwordInfo.setOnClickListener(this)
        binding.usernameChanges.setOnClickListener(this)
        binding.bioText.setOnClickListener(this)
        binding.logins.setOnClickListener(this)
        binding.logOuts.setOnClickListener(this)
        binding.phoneNumber.setOnClickListener(this)
        binding.blocked.setOnClickListener(this)
        binding.muted.setOnClickListener(this)
        binding.restricted.setOnClickListener(this)



    }

    override fun onClick(p0: View?) {
        when(p0)
        {
            binding.changeppassword ->

            {


                startActivity(Intent(this, ChangePasswordActivity::class.java))
            }

            binding.logininfo ->

            {


                startActivity(Intent(this, LoginInfoActivity::class.java))
            }


            binding.emailConnectd ->

            {


                startActivity(Intent(this, EmailconnectdActivity::class.java))
            }


            binding.twoFactorAuthen ->

            {


                startActivity(Intent(this, AuthenticationActivity::class.java))
            }

            binding.passwordInfo ->

            {


                startActivity(Intent(this, PasswordChangedInfo::class.java))
            }
            binding.usernameChanges ->

            {


                startActivity(Intent(this, UsernameChangesActivity::class.java)
                    .putExtra("type", "Username Changes"))
            }


            binding.logins ->

            {


                startActivity(Intent(this, UsernameChangesActivity::class.java)
                    .putExtra("type", "Your Logins"))
            }


            binding.logOuts ->

            {


                startActivity(Intent(this, UsernameChangesActivity::class.java)
                    .putExtra("type", "Your Logouts"))
            }


            binding.phoneNumber ->

            {


                startActivity(Intent(this, UsernameChangesActivity::class.java)
                    .putExtra("type", "Phone Numbers"))
            }

            binding.bioText ->

            {


                startActivity(Intent(this, UsernameChangesActivity::class.java)
                    .putExtra("type", "Bio Text"))

            }

            binding.blocked ->

            {



                startActivity(Intent(this, BlockedActivity::class.java)
                    .putExtra("type", "Blocked"))
            }

            binding.muted ->

            {



                startActivity(Intent(this, BlockedActivity::class.java)
                    .putExtra("type", "Muted"))
            }
            binding.restricted ->

            {



                startActivity(Intent(this, BlockedActivity::class.java)
                    .putExtra("type", "Restricted"))
            }
        }
    }
}