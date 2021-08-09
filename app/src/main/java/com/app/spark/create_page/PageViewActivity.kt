package com.app.spark.create_page

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.ActivityCreateApgaeBinding
import com.app.spark.databinding.ActivityPageViewBinding

class PageViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPageViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_page_view)
    }
}