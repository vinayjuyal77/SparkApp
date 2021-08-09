package com.app.spark.activity.counsellor

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.ActivityCounsellorInformationBinding
import com.app.spark.utils.BaseActivity

class CounsellorInformationActivity : BaseActivity() {
    private lateinit var binding: ActivityCounsellorInformationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_counsellor_information)
        binding.activity = this

        binding.cvStartCall.setOnClickListener {
            startActivity(Intent(this, CallToCounsellorActivity::class.java))
        }
    }
}