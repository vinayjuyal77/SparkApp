package com.app.spark.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ActivityShowPictureBinding
import com.bumptech.glide.Glide

class ShowImageActivity : AppCompatActivity() {
    lateinit var binding: ActivityShowPictureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_picture)

        val pic = intent.getStringExtra(IntentConstant.PICTURE)
        Glide.with(this).load(pic).into(binding.imgPic)
    }
}