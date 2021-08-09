package com.app.spark.create_page

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.ActivityCreateApgaeBinding
import com.app.spark.databinding.ActivityFindMeFriendBinding
import com.app.spark.dialogs.AppReportDiloag
import com.app.spark.dialogs.SelectPreferredMethodDiloag

class CreateAPgae : AppCompatActivity() {
    private lateinit var binding: ActivityCreateApgaeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_apgae)
        binding.activity = this
        binding.tvCraete.setOnClickListener {
            SelectPreferredMethodDiloag.Builder(this)
                .setOkFunction {}
                .build()
        }
    }
}