package com.app.spark.activity.change_password

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import com.app.spark.R
import com.app.spark.models.CommonResponse
import com.app.spark.models.OtpResponse
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines

class ChangePasswordViewModel(application: Application) : AndroidViewModel(application) {

    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var response = MutableLiveData<String>()

    fun changePasswordAPI(emailNumber: String,password:String) {
        try {
            val map = HashMap<String, String?>()
            map["mobile"] = emailNumber
            map["password"] = password
            Coroutines.main {
                val result = ApiInterface(getApplication())?.resetPasswordApi(map)
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