package com.app.spark.bottomSheet.profilemenu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import android.content.Context
import android.util.Log
import com.app.spark.R
import com.app.spark.database.ConnectedAppDatabase
import com.app.spark.models.CommonResponse
import com.app.spark.models.NotificationListResponse
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines
import com.app.spark.utils.isNetworkAvailable

class ProfileOptionViewModel(application: Application) : AndroidViewModel(application) {
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var responseReport = MutableLiveData<CommonResponse>()
    var responseBlock = MutableLiveData<CommonResponse>()
    var responseConnectionType = MutableLiveData<CommonResponse>()
    var responseNotifications = MutableLiveData<NotificationListResponse>()

    var connectedAppDatabase : ConnectedAppDatabase? = null
    private var token: String? = null
    private var userId: String? = null
    private var offset: Int? = null
     var isLoading= false
     var isPaging= false



    fun getLcoalData()
    {

        connectedAppDatabase = ConnectedAppDatabase.getInstance(getApplication())
        if(connectedAppDatabase?.appDao()?.getNotfication()!=null) {
            responseNotifications.postValue(connectedAppDatabase?.appDao()?.getNotfication())
        }

    }


    fun setUserData(token: String?, userId: String?) {
        this.token = token
        this.userId = userId
    }

    fun pagingFeedListingApi(offset: Int?=null) {
        if (offset != null && offset % 10 == 0) {
            isLoading=true
            isPaging=true
            getNotificationApi(offset)
        }else if (offset==null){
            isPaging= false
            getNotificationApi(0)
        }
    }


  private  fun getNotificationApi(offset: Int = 0) {
        try {
            val map = HashMap<String, String>()
            map["user_id"] = userId!!
            map["offset"] = offset.toString()
            map["limit"] = "10"

            Coroutines.main {
                val result = ApiInterface(getApplication())?.notificationListAPI(token!!, map)
                if (result!!.isSuccessful) {
                    isLoading=false
                    if (result.body()?.statusCode == 200) {
                        responseNotifications.postValue(result.body())
                    } else {
                        errString.postValue(result.body()?.aPICODERESULT)
                    }
                } else {
                    isLoading=false
                    errRes.postValue(R.string.something_went_wrong)
                }
            }
        } catch (ex: Exception) {
            isLoading=false
            errRes.postValue(R.string.something_went_wrong)
        }
    }


    fun connectionTypeApi(
        token: String,
        user_id: String,
        follower_id: String,
        follow_group: String
    ) {
        try {
            val map = HashMap<String, String>()
            map["user_id"] = user_id
            map["follower_id"] = follower_id
            map["follow_group"] = follow_group

            Coroutines.main {
                val result = ApiInterface(getApplication())?.folowgroupActionApi(token, map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        responseConnectionType.postValue(result.body())
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


    fun reportUser(token: String, reported_by: String, reported_to: String) {
        try {
            val map = HashMap<String, String>()
            map["reported_by"] = reported_by
            map["reported_to"] = reported_to

            Coroutines.main {
                val result = ApiInterface(getApplication())?.reportUserApi(token, map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        responseReport.postValue(result.body())
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


    fun blockedUser(token: String, blocked_by: String, blocked_to: String, type: String) {
        try {
            val map = HashMap<String, String>()
            map["blocked_by"] = blocked_by
            map["blocked_to"] = blocked_to
            map["type"] = type
            Coroutines.main {
                val result = ApiInterface(getApplication())?.blockuserAction(token, map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        responseBlock.postValue(result.body())
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

    fun isOnlineStatusApi(context: Context, onlineStatus: Int, userId: Int) {
        if (isNetworkAvailable(getApplication())) {
            val map = hashMapOf<String, Any?>(
                "user_id" to userId,
                "online_status" to onlineStatus
            )
            Coroutines.main {
                try {
                    val result = ApiInterface(getApplication()).isOnlineStatus(map)
                    Log.d("TAG", "isOnlineStatusApi: "+result.body()!!.statusCode)
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            Log.d("isOnlineStatusApi", if(onlineStatus==1) "Online" else "Offline")
                        } else {
                            errString.postValue(result.body()?.APICODERESULT)
                        }
                    } else {
                        isOnlineStatusApi(context, onlineStatus, userId)
                        //errRes.postValue(R.string.something_went_wrong)
                    }
                } catch (ex: Exception) {
                    isOnlineStatusApi(context, onlineStatus, userId)
                    //errRes.postValue(R.string.something_went_wrong)
                }
            }
        }
    }

}