package com.app.spark.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

import com.app.spark.R
import com.app.spark.models.CommonResponse
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines

class ChangeEmailMobileViewModel(application: Application):AndroidViewModel(application) {


    var errRes= MutableLiveData<Int>()
    var errString= MutableLiveData<String>()
    var response= MutableLiveData<CommonResponse>()
    fun changeSignUpAPI(id:String,type:String,etItem:String) {
        try{
            val map = HashMap<String, String>()
            map.put("user_id",id)
            map.put("type",type)
            map.put(type,etItem)
            Coroutines.main {
                val result = ApiInterface(getApplication())?.editsignupAPI(map)
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