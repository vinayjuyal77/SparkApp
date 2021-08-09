package com.app.spark.activity.create_chat_group

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import com.app.spark.R
import com.app.spark.models.*
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines
import com.app.spark.utils.getImageFilePart
import com.app.spark.utils.getRequestBody
import okhttp3.RequestBody
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class AddChatMembersViewModel(application: Application) : AndroidViewModel(application) {
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var response = MutableLiveData<List<ResultFollowing>>()
    var commentsResponse = MutableLiveData<String>()

    fun getFollowingAndFollowers(token: String, userID: String, offset: String,
            type:String) {
        try {
            val map = HashMap<String, String>()
            map["user_id"] = userID
            map["offset"] = offset
            map["limit"] = "20"
            map["type"] = type
            Coroutines.main {
                val result = ApiInterface(getApplication()).getFollowingAndFollowerApi(token, map)
                if (result.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        response.postValue(result.body()?.result)
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


    fun getSearchUser(token: String, userID: String, keyword: String) {
        try {
            val map = HashMap<String, String>()
            map["user_id"] = userID
            map["keyword"] = keyword

            Coroutines.main {
                val result = ApiInterface(getApplication())?.searchUserApi(token, map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        response.postValue(result.body()?.result)
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

    fun addPostAPI(
        header: String,
        userId: String,
        selectedUsersId: String,
        groupName: String,
        about: String,
        image: File?) {
        try {
            val map = hashMapOf<String, RequestBody>(
                "user_id" to getRequestBody(userId),
                "group_name" to getRequestBody(groupName),
                "add_user_ids" to getRequestBody(selectedUsersId)
            )
            about?.let {
                map["about"] = getRequestBody(about)
            }

            Coroutines.main {
                val result = ApiInterface(getApplication()).createGroupAPI(
                    header, map,
                    if (image!=null) {
                        getImageFilePart("group_image", image)
                    } else {
                        null
                    }
                )
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        commentsResponse.postValue(result.body()?.APICODERESULT)
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