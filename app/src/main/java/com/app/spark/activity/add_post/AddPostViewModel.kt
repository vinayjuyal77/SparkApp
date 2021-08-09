package com.app.spark.activity.add_post

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import com.app.spark.R
import com.app.spark.models.ImportantDataModel
import com.app.spark.models.ImportantDataResponse
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines
import com.app.spark.utils.getImageFilePart
import com.app.spark.utils.getRequestBody
import okhttp3.RequestBody
import java.io.File
import java.util.*

class AddPostViewModel(application: Application) : AndroidViewModel(application) {
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var response = MutableLiveData<String>()

    fun addPostAPI(
        header: String,
        userId: String,
        postInfo: String?,
        postType: String,
        mediaType: String,
        image: String) {
        try {
            val map = hashMapOf<String, RequestBody>(
                "user_id" to getRequestBody(userId),
                "post_type" to getRequestBody(postType.toLowerCase(Locale.getDefault())),
                "media_type" to getRequestBody(mediaType.toLowerCase(Locale.getDefault()))
            )
            postInfo?.let {
                map["post_info"] = getRequestBody(postInfo)
            }

            Coroutines.main {
                val result = ApiInterface(getApplication())?.addPostAPI(
                    header, map,
                    if (image.isNotEmpty()) {
                        getImageFilePart("post_media", File(image))
                    } else {
                        null
                    }
                )

                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        response.postValue(result.body()?.APICODERESULT)
                    } else {
                        errString.postValue(result.body()?.APICODERESULT)
                    }
                } else {
                    errRes.postValue(R.string.something_went_wrong)
                }
            }
        } catch (ex: Exception) {
            errRes.postValue(R.string.something_went_wrong)
        }
    }
}