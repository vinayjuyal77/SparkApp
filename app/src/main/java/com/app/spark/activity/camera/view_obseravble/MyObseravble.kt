package com.app.spark.activity.camera.view_obseravble

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyObseravble : ViewModel() {
    val data = MutableLiveData<String>()
    fun data(item: String) {
        data.value = item
    }
}