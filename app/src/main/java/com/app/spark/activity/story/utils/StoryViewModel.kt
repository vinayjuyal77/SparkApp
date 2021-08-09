package com.app.spark.activity.story.utils


import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.app.spark.R
import com.app.spark.activity.story.dataClass.StoryResponse
import com.app.spark.database.ConnectedAppDatabase
import com.app.spark.models.CommonResponse
import com.app.spark.models.FeedsResponse
import com.app.spark.models.PopularProfileResponse
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.isNetworkAvailable

class StoryViewModel(application: Application) : AndroidViewModel(application) {

    var storyResponse = MutableLiveData<StoryResponse>()
    var errString = MutableLiveData<String>()
    var errRes = MutableLiveData<Int>()









     fun storyListing(offset: Int = 0) {

        if (isNetworkAvailable(getApplication())) {
            val map = hashMapOf<String, Any?>(
                "user_id" to "201",
                "limit" to "10",
                "offset" to offset.toString()
            )
            try {
                Coroutines.main {
                    val result = ApiInterface(getApplication()).getusersStories(map)
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200||result.body()?.statusCode == 300) {
                            storyResponse.postValue(result.body())
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




}