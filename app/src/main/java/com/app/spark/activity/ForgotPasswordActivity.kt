package com.app.spark.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var binding:ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_forgot_password)
        setClickListener()
    }

    private fun setClickListener(){
        binding.tvBack.setOnClickListener { finish() }
        binding.tvSubmit.setOnClickListener {
            if (binding.edtPhone.text.toString().trim().isNotEmpty()&&binding.edtPhone.text.toString().trim().length==10){
                startActivity(Intent(this,ForgotPasswordOtpActivity::class.java)
                    .putExtra(IntentConstant.EMAIL_PHONE,binding.edtPhone.text.toString().trim()))
            }
        }
    }
}