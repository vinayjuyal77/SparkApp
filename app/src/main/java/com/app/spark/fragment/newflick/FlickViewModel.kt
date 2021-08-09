package com.app.spark.fragment.newflick

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import android.util.Log
import com.app.spark.R
import com.app.spark.database.ConnectedAppDatabase
import com.app.spark.models.CommonResponse
import com.app.spark.models.GetFlickResponse
import com.app.spark.network.ApiInterface
import com.app.spark.network.NoConnectivityException
import com.app.spark.utils.Coroutines
import com.app.spark.utils.isNetworkAvailable

class FlickViewModel(application: Application) : AndroidViewModel(application) {

    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var errStringFliks = MutableLiveData<String>()
    var flickList = MutableLiveData<List<GetFlickResponse.Result>>()
    var deleteFeed = MutableLiveData<CommonResponse>()
    var resultFollowed = MutableLiveData<CommonResponse>()

    private var token: String? = null
    private var userId: String? = null
    private var offset: Int? = null
    var connectedAppDatabase : ConnectedAppDatabase? = null


    fun getLcoalData()
    {
        connectedAppDatabase = ConnectedAppDatabase.getInstance(getApplication())
        // connectedAppDatabase?.appDao()?.getProfileData()!!

        if(connectedAppDatabase?.appDao()?.getFlick()!=null) {

            flickList.postValue(connectedAppDatabase?.appDao()?.getFlick()!!)
        }
    }

    fun setUserData(token: String?, userId: String?) {
        this.token = token
        this.userId = userId
    }

    fun pagingFeedListingApi(offset: Int?) {
        if (offset != null && offset % 6 == 0) {
            flickListingApi(offset)
        }
    }

    fun deleteFeedApi(
        token: String,
        userId: String,
        post_id: String,
        content_type: String = "post"
    ) {
        if (isNetworkAvailable(getApplication())) {
            try {
                val map = HashMap<String, String>()
                map["user_id"] = userId
                map["post_id"] = post_id
                map["content_type"] = content_type
                Coroutines.main {
                    val result = ApiInterface(getApplication()).deleteFeedAPI(token, map)
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            //errString.postValue(result.body()?.APICODERESULT)
                            //pagingFeedListingApi(null)
                            deleteFeed.postValue(result.body())
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

    fun flickListingApi(offset: Int = 0) {
        if (isNetworkAvailable(getApplication())) {
            val map = hashMapOf<String, String?>(
                "user_id" to userId,
                "limit" to "6",
                "offset" to offset.toString()
            )
            try {
                Coroutines.main {
                    try {
                        val result = ApiInterface(getApplication())?.getFlicksApi(token, map)
                        if (result!!.isSuccessful) {
                            if (result.body()?.statusCode == 200) {
                                flickList.postValue(result.body()?.result)
                            } else {
                                errStringFliks.postValue(result.body()?.aPICODERESULT)
                            }
                        } else {
                            errRes.postValue(R.string.something_went_wrong)
                        }
                    } catch (ex: NoConnectivityException) {
                        errString.postValue(ex.message)
                    }
                }
            } catch (ex: Exception) {
                errRes.postValue(R.string.something_went_wrong)
            }
        }
    }

    fun followUnfollowApi(
        token: String,
        userId: String,
        otherUserId: String,
        type: String = "Follow"
    ) {
        if (isNetworkAvailable(getApplication())) {
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
                            // errString.postValue(result.body()?.APICODERESULT)
                            resultFollowed.postValue(result.body())
                            // flickListingApi(0)
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

    fun likeUnlikeApi(
        postId: String,
        isLike: Boolean
    ) {
        if (isNetworkAvailable(getApplication())) {
            try {
                val map = HashMap<String, String?>()
                map["user_id"] = userId
                map["post_id"] = postId
                map["content_type"] = "flick"
                if (isLike)
                    map["type"] = "Like"
                else map["type"] = "Unlike"
                Coroutines.main {
                    val result = ApiInterface(getApplication())?.likeUnlikeAPI(token, map)
                    if (result!!.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            // errString.postValue(result.body()?.APICODERESULT)
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

    fun reportPost(token: String, reported_by: String, postId: String) {
        if (isNetworkAvailable(getApplication())) {
            try {
                val map = HashMap<String, String?>()
                map["user_id"] = reported_by
                map["post_id"] = postId
                map["content_type"] = "flick"
                Coroutines.main {
                    val result = ApiInterface(getApplication())?.reportFeedApi(token, map)
                    if (result!!.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            errString.postValue(result.body()?.APICODERESULT)
                            flickListingApi(0)
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

    fun viewCountApi(postId: String) {
        if (isNetworkAvailable(getApplication())) {
            try {
                val map = HashMap<String, String?>()
                map["user_id"] = userId
                map["post_id"] = postId
                map["content_type"] = "flick"
                Coroutines.main {
                    val result = ApiInterface(getApplication())?.viewCountAPI(token, map)
                    if (result!!.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            //  errString.postValue(result.body()?.APICODERESULT)
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

}