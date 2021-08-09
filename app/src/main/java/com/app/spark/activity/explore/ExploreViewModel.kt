package com.app.spark.activity.explore

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.app.spark.R
import com.app.spark.models.GetCommentsResponse
import com.app.spark.models.GetTagListResponse
import com.app.spark.models.MatchingResponse
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines
import com.app.spark.utils.isNetworkAvailable

class ExploreViewModel(application: Application) : AndroidViewModel(application){
    private var token: String? = null
    private var userId: String? = null
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    fun setUserData(token: String?, userId: String?) {
        this.token = token
        this.userId = userId
    }

    fun isOnlineStatusApi(context:Context,onlineStatus: Int, userId: Int) {
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
    var tagList = MutableLiveData<String>()
    fun getTagListApi(userId:Int,type:String) {
        if (isNetworkAvailable(getApplication())) {
            val map = hashMapOf<String, Any?>(
                "user_id" to userId,
                "tag_type" to type
            )
            Coroutines.main {
                try {
                    val result = ApiInterface(getApplication()).getTagList(map)
                    Log.d("TAG", "getTagListApi: "+result.body())
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            tagList.postValue(result.body()?.result!!.tags)
                            Log.d("TAG", "my array list: "+result.body()?.result)
                        } else {
                            errString.postValue(result.body()?.APICODERESULT)
                        }
                    } else {
                        getTagListApi(userId, type)
                        //errRes.postValue(R.string.something_went_wrong)
                    }
                } catch (ex: Exception) {
                    getTagListApi(userId, type)
                    //errRes.postValue(R.string.something_went_wrong)
                }
            }
        }
    }
    var errCode = MutableLiveData<Int>()
    var chatName:String?=null
    var chatID:Int?=null
    var chatProfile:String?=null
    fun setMatchApi(userId:Int,gender:String,minAge:Int,
                    mixAge:Int,radious:Int,
                    selectList:ArrayList<String>,latitute:String,longtitute:String) {
        if (isNetworkAvailable(getApplication())) {
            val map = hashMapOf<String, Any?>(
                "user_id" to userId,
                "userGender" to gender,
                "uAgeFrom" to minAge,
                "uAgeTo" to mixAge,
                "userDistance" to radious,
                "userTagsArr" to selectList,
                "userLat" to latitute,
                "userLong" to longtitute
            )
            Coroutines.main {
                try {
                    val result = ApiInterface(getApplication()).setMatchFound(map)
                    Log.d("TAG", "getTagListApi: "+result.body())
                    if (result.isSuccessful) {
                        Log.d("TAG", "match List "+result.body())
                        if (result.body()?.statusCode == 200) {
                            if(result.body()?.status == 0) errCode.postValue(result.body()?.status)
                            else {
                                chatID=result.body()!!.result[0].user_id
                                chatName=result.body()!!.result[0].name
                                chatProfile=result.body()!!.result[0].profile_pic
                                errCode.postValue(result.body()?.status)
                            }
                        } else {
                            errCode.postValue(result.body()?.statusCode)
                        }
                    } else {
                        setMatchApi(userId,gender,minAge,mixAge, radious, selectList, latitute, longtitute)
                        //errRes.postValue(R.string.something_went_wrong)
                    }
                } catch (ex: Exception) {
                    //errRes.postValue(R.string.something_went_wrong)
                    setMatchApi(userId,gender,minAge,mixAge, radious, selectList, latitute, longtitute)
                }
            }
        }
    }

    fun updateTagsApi(userId:Int,
                    selectList:ArrayList<String>) {
        if (isNetworkAvailable(getApplication())) {
            val map = hashMapOf<String, Any?>(
                "user_id" to userId,
                "tags" to selectList,
            )
            Coroutines.main {
                try {
                    val result = ApiInterface(getApplication()).setTagsAPI(map)
                    Log.d("TAG", "getTagListApi: "+result.body())
                    if (result.isSuccessful) {
                        Log.d("TAG", "match List "+result.body())
                        if (result.body()?.statusCode == 200) {
                            if(result.body()?.status == 0) errCode.postValue(result.body()?.status)
                            else {
                                chatID=result.body()!!.result[0].user_id
                                errCode.postValue(result.body()?.status)
                            }
                        } else {
                            errCode.postValue(result.body()?.statusCode)
                        }
                    } else {
                        updateTagsApi(userId, selectList)
                        //errRes.postValue(R.string.something_went_wrong)
                    }
                } catch (ex: Exception) {
                    updateTagsApi(userId, selectList)
                    //errRes.postValue(R.string.something_went_wrong)
                }
            }
        }
    }

    var successStatus = MutableLiveData<MatchingResponse>()
    var faliedRes = MutableLiveData<String>()
    fun setMatchApiNew(context: Context,userId:Int,gender:String,minAge:Int,
                       mixAge:Int,radious:Int,
                       selectList:ArrayList<String>,latitute:String,longtitute:String) {
        if (isNetworkAvailable(getApplication())) {
            val map = hashMapOf<String, Any?>(
                "user_id" to userId,
                "userGender" to gender,
                "uAgeFrom" to minAge,
                "uAgeTo" to mixAge,
                "userDistance" to radious,
                "userTagsArr" to selectList,
                "userLat" to latitute,
                "userLong" to longtitute
            )
            Coroutines.main {
                try {
                    val result = ApiInterface(getApplication()).setMatchFound(map)
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            successStatus.postValue(result.body())
                        } else {
                            faliedRes.postValue(result.body()?.APICODERESULT)
                        }
                    } else {
                        faliedRes.postValue(context.getString(R.string.something_went_wrong))
                    }
                } catch (ex: Exception) {
                    faliedRes.postValue(context.getString(R.string.something_went_wrong))
                }
            }
        }
    }



}