package com.app.spark.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import com.app.spark.R
import com.app.spark.models.*
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines

class OtpViewModel(application: Application) : AndroidViewModel(application) {

    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var response = MutableLiveData<ImportantDataResponse>()
    var response_signup = MutableLiveData<ImportantDataResult>()

    fun otpAPI(otp: String, type: String, emailNumber: String) {
        try {
            val map = HashMap<String, String>()
            map.put("otp", otp)
            map.put("type", type)
            map["screen_type"] = "signup"
            map.put(type, emailNumber)
            Coroutines.main {
                val result = ApiInterface(getApplication())?.otpAPi(map)
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
        } catch (ex: Exception) {
            errRes.postValue(R.string.something_went_wrong)
        }
    }

    fun sendOtpAPI(emailNumber: String) {
        try {
            val map = HashMap<String, String?>()
            map["screen_type"] = "signup"
            map["mobile"] = emailNumber
            Coroutines.main {
                val result = ApiInterface(getApplication())?.sendOtpApi(map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        errString.postValue(result.body()?.APICODERESULT)
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



    fun signUpAPI(userName: String, password: String, type: String, etItem: String) {
        try {
            val map = HashMap<String, String>()
            map.put("username", userName)
            map.put("type", type)
            map.put("password", password)
            map.put(type, etItem)
            Coroutines.main {
                val result = ApiInterface(getApplication())?.signUpAPi(map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        response_signup.postValue(result.body()?.result)
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