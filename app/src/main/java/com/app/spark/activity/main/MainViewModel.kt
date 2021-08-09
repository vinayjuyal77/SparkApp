package com.app.spark.activity.main

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

class MainViewModel(application: Application) : AndroidViewModel(application) {

    fun updateDeviceTokenApi(
        header: String?,
        userId: String?,
        deviceToken: String?
    ) {

            val map = hashMapOf<String, String?>(
                "user_id" to userId,
                "device_type" to "android",
                "device_token" to deviceToken
            )
            Coroutines.main {
                try {
                val result = ApiInterface(getApplication())?.updateDeviceTokenAPI(
                    header, map
                )
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode != 200) {
                        updateDeviceTokenApi(header, userId, deviceToken)
                    }
                } else {
                    updateDeviceTokenApi(header, userId, deviceToken)
                }
                } catch (ex: Exception) {

                }
            }

    }
}