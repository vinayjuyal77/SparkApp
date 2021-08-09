package com.app.spark.activity.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import androidx.viewpager2.widget.ViewPager2
import com.app.spark.R
import com.app.spark.models.*
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines
import com.app.spark.utils.getImageFilePart
import com.app.spark.utils.getRequestBody
import okhttp3.RequestBody
import java.io.File

class SearchUserViewModel(application: Application) : AndroidViewModel(application) {
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var responseSearch = MutableLiveData<SearchModelResponse>()


    fun getSearchUser(token: String, userID: String, keyword: String) {
        try {
            val map = HashMap<String, String>()
            map["user_id"] = userID
            map["keyword"] = keyword

            Coroutines.main {
                val result = ApiInterface(getApplication())?.searchUserApi(token, map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        responseSearch.postValue(result.body())
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