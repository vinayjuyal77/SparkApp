package com.app.spark.activity.custom_gallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.ActivityCustomGalleryBinding
import com.app.spark.databinding.ActivityEditorBinding

class EditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_editor)
    }
}