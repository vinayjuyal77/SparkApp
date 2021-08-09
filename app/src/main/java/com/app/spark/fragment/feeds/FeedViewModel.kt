package com.app.spark.fragment.feeds

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

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var errStringFeed = MutableLiveData<String>()
    var popularProfileList = MutableLiveData<List<PopularProfileResponse.PopularProfile>>()
    var feedsList = MutableLiveData<List<FeedsResponse.Result>>()
    var deleteFeed = MutableLiveData<CommonResponse>()
    var storyResponse = MutableLiveData<StoryResponse>()


    var connectedAppDatabase : ConnectedAppDatabase? = null
    private var token: String? = null
    private var userId: String? = null
    private var offset: Int? = null
    var isLoading: Boolean = false
    private var isPaging: Boolean = false

    fun getPaging(): Boolean {
        return isPaging
    }

    fun setUserData(token: String?, userId: String?) {
        this.token = token
        this.userId = userId
    }


    fun popularProfileApi(token: String, userId: String) {
        if (isNetworkAvailable(getApplication())) {
            Coroutines.main {
                try {

                    val result = ApiInterface(getApplication()).getPopularProfile(token, userId)
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            popularProfileList.postValue(result.body()?.result)
                        } else {
                            errString.postValue(result.body()?.aPICODERESULT)
                        }
                    } else {
                        errRes.postValue(R.string.something_went_wrong)
                    }
                } catch (ex: Exception) {
                    errRes.postValue(R.string.something_went_wrong)
                }
            }
        }

    }




    fun getLcoalData()
    {
        connectedAppDatabase = ConnectedAppDatabase.getInstance(getApplication())


       // connectedAppDatabase?.appDao()?.getProfileData()!!

        Log.e("+++", connectedAppDatabase?.appDao()?.getFeed().toString())
        feedsList.postValue(connectedAppDatabase?.appDao()?.getFeed()!!)
    }


    fun pagingFeedListingApi(offset: Int?) {

        if (offset != null && offset % 10 == 0) {
            isLoading = true
            isPaging = true
            feedListingApi(offset)
        } else if (offset == null) {
            isPaging = false
            feedListingApi(0)
        }
    }



    private fun feedListingApi(offset: Int = 0) {

        if (isNetworkAvailable(getApplication())) {
            val map = hashMapOf<String, String?>(
                "user_id" to userId,
                "limit" to "10",
                "offset" to offset.toString()
            )
            try {
                Coroutines.main {
                    val result = ApiInterface(getApplication()).getFeedsApi(token, map)
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200||result.body()?.statusCode == 300) {
                            feedsList.postValue(result.body()?.result)
                        } else {
                            isLoading = false
                            errStringFeed.postValue(result.body()?.aPICODERESULT)
                        }
                    } else {
                        isLoading = false
                        errRes.postValue(R.string.something_went_wrong)
                    }
                }
            } catch (ex: Exception) {
                isLoading = false
                errRes.postValue(R.string.something_went_wrong)
            }
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
                    val result = ApiInterface(getApplication()).followUnfollowAPI(token, map)
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            //errString.postValue(result.body()?.APICODERESULT)
                            pagingFeedListingApi(null)
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
                map["content_type"] = "post"
                if (isLike)
                    map["type"] = "Like"
                else map["type"] = "Unlike"
                Coroutines.main {
                    val result = ApiInterface(getApplication()).likeUnlikeAPI(token, map)
                    if (result.isSuccessful) {
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
                map["content_type"] = "post"
                Coroutines.main {
                    val result = ApiInterface(getApplication()).reportFeedApi(token, map)
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            errString.postValue(result.body()?.APICODERESULT)
                            pagingFeedListingApi(null)
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

    fun viewCountApi(token: String, userId: String, postId: String) {
        if (isNetworkAvailable(getApplication())) {
            try {
                val map = HashMap<String, String?>()
                map["user_id"] = userId
                map["post_id"] = postId
                map["content_type"] = "post"
                Coroutines.main {
                    val result = ApiInterface(getApplication()).viewCountAPI(token, map)
                    if (result.isSuccessful) {
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


    fun storyListing(offset: Int = 0, user_id :  String) {

        if (isNetworkAvailable(getApplication())) {
            val map = hashMapOf<String, Any?>(
                "user_id" to user_id,
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