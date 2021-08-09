package com.app.spark.activity.camera

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.app.spark.R
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines
import com.app.spark.utils.getImageFilePart
import com.app.spark.utils.getRequestBody
import com.app.spark.utils.isNetworkAvailable
import okhttp3.RequestBody
import java.io.File

class CameraViewModel (application: Application) : AndroidViewModel(application){
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var tagList = MutableLiveData<String>()

    fun addStory(userId:Int,image: File) {
        if (isNetworkAvailable(getApplication())) {
            val map = hashMapOf<String, RequestBody>(
                "user_id" to getRequestBody(userId.toString())
            )
            Coroutines.main {
                try {
                    val result = ApiInterface(getApplication()).addStory(map,getImageFilePart("story_media", image))
                    Log.d("TAG", "getTagListApi: "+result.body())
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            errRes.postValue(result.body()?.statusCode)
                        } else {
                            errString.postValue(result.body()?.APICODERESULT)
                        }
                    } else {
                        errRes.postValue(R.string.something_went_wrong)
                    }
                } catch (ex: Exception) {
                    errRes.postValue(R.string.something_went_wrong)
                }
            }
        }
    }
}