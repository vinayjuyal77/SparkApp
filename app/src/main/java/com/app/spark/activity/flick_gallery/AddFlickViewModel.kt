package com.app.spark.activity.flick_gallery

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

class AddFlickViewModel(application: Application) : AndroidViewModel(application) {
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var response = MutableLiveData<String>()

    fun addFlickAPI(
        header: String,
        userId: String,
        postInfo: String?,
        postType: String,
        image: String,
        image_thumbnail: String,
    ) {
        try {
            val map = hashMapOf<String, RequestBody>(
                "user_id" to getRequestBody(userId),
                "flick_type" to getRequestBody(postType.toLowerCase(Locale.getDefault()))
            )
            postInfo?.let {
                map["flick_info"] = getRequestBody(postInfo)
            }

            Coroutines.main {
                val result = ApiInterface(getApplication()).addFlickAPI(
                    header, map,
                    if (image.isNotEmpty()) {
                        getImageFilePart("flick_media", File(image))

                    } else {
                        null
                    }
                ,
                    getImageFilePart("flick_thumbnail", File(image_thumbnail))
                )
                if (result.isSuccessful) {
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