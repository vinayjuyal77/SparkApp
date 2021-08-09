package com.app.spark.activity.play_information

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.ActivityPlayInformationBinding
import com.app.spark.utils.BaseActivity

class ActivityPlayInformation:BaseActivity() {
    private lateinit var binding:ActivityPlayInformationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this, R.layout.activity_play_information)
    }
}