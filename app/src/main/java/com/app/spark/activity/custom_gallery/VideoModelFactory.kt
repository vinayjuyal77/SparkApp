package com.app.spark.activity.custom_gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VideoModelFactory (private val repository: VideoRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return VideoViewModel(repository) as T
    }
}