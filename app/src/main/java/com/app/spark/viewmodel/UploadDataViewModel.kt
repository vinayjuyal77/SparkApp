package com.app.spark.viewmodel

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

class UploadDataViewModel(application: Application):AndroidViewModel(application) {
    var errRes= MutableLiveData<Int>()
    var errString= MutableLiveData<String>()
    var response= MutableLiveData<ImportantDataResponse>()

    fun importantDataAPI(header:String,userId:String,model:ImportantDataModel,image:String) {
        try{
            val map = HashMap<String, RequestBody>()
            map.put("user_id", getRequestBody(userId))
            map.put("name", getRequestBody(model.name))
            map.put("gender", getRequestBody(model.gender))
            map.put("location", getRequestBody(model.location))
            map.put("latitude", getRequestBody(model.lat))
            map.put("longitude", getRequestBody(model.long))
            map.put("dob", getRequestBody(model.dob))
            map.put("state", getRequestBody(model.state))
            map.put("country", getRequestBody(model.country))


            Coroutines.main {
                val result = ApiInterface(getApplication())?.completeProfileAPI(header,map,
                    if (image.isNotEmpty()) {
                    getImageFilePart("profile_pic", File(image))
                } else {
                    null
                })
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        response.postValue(result.body())
                    } else {
                        errString.postValue(result.body()?.APICODERESULT)
                    }
                } else {
                    errRes.postValue(R.string.something_went_wrong)
                }
            }
        }
        catch(ex:Exception){
            errRes.postValue(R.string.something_went_wrong)
        }
    }
}