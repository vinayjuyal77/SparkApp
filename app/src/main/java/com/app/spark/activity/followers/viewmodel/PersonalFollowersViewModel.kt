package com.app.spark.activity.followers.viewmodel

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

class PersonalFollowersViewModel(application: Application) : AndroidViewModel(application) {
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var response = MutableLiveData<FollowingResponse>()
    var tokenGlobal=""
    var userIdGlobal=""
   private var loginUserID=""
    var typeGlobal=""
    var followingGroupGlobal=""

     fun setLoginUserID(loginUserID: String){
        this.loginUserID=loginUserID
    }

    fun getFollowingAndFollowers(token: String, userID: String, offset: String,
            type:String,followingGroup:String) {
        try {
            tokenGlobal=token
            userIdGlobal=userID
            typeGlobal=type
            followingGroupGlobal=followingGroup
            val map = HashMap<String, String>()
            map["user_id"] = userID
            map["offset"] = offset
            map["limit"] = "10"
            map["type"] = type
            if (loginUserID==userID) {
                map["profile_type"] = "own"
                if (followingGroup.trim().isNotEmpty())
                    map["follow_group"] = followingGroup
            }else {
                map["profile_type"] = "other"
            }
            Coroutines.main {
                val result = ApiInterface(getApplication())?.getFollowingAndFollowerApi(token, map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        response.postValue(result.body())
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


    fun followUnfollowApi(
        token: String,
        userId: String,
        otherUserId: String,
        type: String = "Follow"
    ) {
        try {
            val map = HashMap<String, String>()
            map["follow_by"] = userId
            map["follow_to"] = otherUserId
            map["follow_group"] = "public"
            map["type"] = type
            Coroutines.main {
                val result = ApiInterface(getApplication())?.followUnfollowAPI(token, map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        getFollowingAndFollowers(tokenGlobal,userIdGlobal,"0",typeGlobal,followingGroupGlobal)
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