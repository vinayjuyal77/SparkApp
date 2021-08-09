package com.app.spark.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import com.app.spark.R
import com.app.spark.models.CommonResponse
import com.app.spark.models.CountryStateResponse
import com.app.spark.models.OtpResponse
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines

class ImportantDataViewModel(application: Application) : AndroidViewModel(application) {

    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var countryList = MutableLiveData<List<CountryStateResponse.Result>>()
    var stateList = MutableLiveData<List<CountryStateResponse.Result>>()

    fun getCountryListAPI() {
        try {
            val map = HashMap<String, String>()
            map.put("type", "country")
            Coroutines.main {
                val result = ApiInterface(getApplication())?.countryStateAPI(map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        countryList.postValue(result.body()?.result)
                    } else {
                        errString.postValue(result.body()?.aPICODERESULT)
                    }
                } else {
                    errRes.postValue(R.string.something_went_wrong)
                }
            }
        } catch (ex: Exception) {
            errRes.postValue(R.string.something_went_wrong)
        }
    }

    fun getStateListAPI(countryId: String) {
        try {
            val map = HashMap<String, String>()
            map.put("type", "state")
            map.put("country_id", countryId)
            Coroutines.main {
                val result = ApiInterface(getApplication())?.countryStateAPI(map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        stateList.postValue(result.body()?.result)
                    } else {
                        errString.postValue(result.body()?.aPICODERESULT)
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