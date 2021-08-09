package com.app.spark.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import com.app.spark.R
import com.app.spark.models.CommonResponse
import com.app.spark.models.OtpResponse
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines

class ForgotPasswordOtpViewModel(application: Application) : AndroidViewModel(application) {

    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var response = MutableLiveData<String>()

    fun otpAPI(otp: String, emailNumber: String) {
        try {
            val map = HashMap<String, String>()
            map["otp"] = otp
            map["type"] = "mobile"
            map["screen_type"] = "forgot"
            map["mobile"] = emailNumber
            Coroutines.main {
                val result = ApiInterface(getApplication())?.otpAPi(map)
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

    fun sendOtpAPI(emailNumber: String) {
        try {
            val map = HashMap<String, String?>()
            map["screen_type"] = "forgot"
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


}