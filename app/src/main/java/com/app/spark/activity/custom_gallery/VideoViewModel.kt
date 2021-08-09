package com.app.spark.activity.custom_gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class VideoViewModel(private val repository: VideoRepository) : ViewModel() {
    private val videoList = MutableLiveData<List<VideoModel>>()
    val videoLiveData: LiveData<List<VideoModel>> = videoList
    init {
        loadLibraryContent()
    }
    private fun loadLibraryContent() = viewModelScope.launch {
        videoList.value = loadSongs.await()
    }
    private val loadSongs: Deferred<List<VideoModel>>
        get() = viewModelScope.async(Dispatchers.IO) { repository.getAllSongs() }
    fun forceReload() = viewModelScope.launch {
        val list = loadSongs.await()
        try {
            if (videoList.value!!.size != list.size) {
                videoList.value = list
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }
}