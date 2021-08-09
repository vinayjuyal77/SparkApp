package com.app.spark.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import com.app.spark.R
import com.app.spark.models.*
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines

class LoginSignUpViewModel(application: Application) : AndroidViewModel(application) {

    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var response = MutableLiveData<ImportantDataResult>()
    var loginResponse = MutableLiveData<ImportantDataResponse>()
    var userNameAvailable = MutableLiveData<Boolean>()

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
                        response.postValue(result.body()?.result)
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

    fun loginAPI(type: String, password: String, etItem: String) {
        try {
            val map = HashMap<String, String>()
            map.put("type", type)
            map.put("password", password)
            map.put(type, etItem)
            Coroutines.main {
                val result = ApiInterface(getApplication())?.loginAPI(map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        loginResponse.postValue(result.body())
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

    fun verifyUserNameAPI(userName: String) {
        try {
            val map = HashMap<String, String>()
            map.put("field_value", userName)
            map.put("field_type", "username")
            Coroutines.main {
                val result = ApiInterface(getApplication())?.verifyUserNameAPI(map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        userNameAvailable.postValue(true)
                    } else {
                        userNameAvailable.postValue(false)

                        errString.postValue(result.body()?.APICODERESULT)
                    }
                } else {
                    userNameAvailable.postValue(false)
                    errRes.postValue(R.string.something_went_wrong)
                }
            }
        } catch (ex: Exception) {
            userNameAvailable.postValue(false)
            errRes.postValue(R.string.something_went_wrong)
        }
    }


}