package com.app.spark.activity.user_feeds

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.app.spark.R
import com.app.spark.models.CommonResponse
import com.app.spark.models.FeedsResponse
import com.app.spark.models.PopularProfileResponse
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines
import com.app.spark.utils.isNetworkAvailable

class FeedListViewModel(application: Application) : AndroidViewModel(application) {

    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var errStringFeed = MutableLiveData<String>()
    var feedsList = MutableLiveData<List<FeedsResponse.Result>>()
    var deleteFeed = MutableLiveData<CommonResponse>()

    private var token: String? = null
    private var userId: String? = null
    private var loginId: String? = null
    private var followGroup: String? = null
    private var offset: Int? = null
    var isLoading: Boolean = false
    private var isPaging: Boolean = false

    fun getPaging(): Boolean {
        return isPaging
    }

    fun setUserData(token: String?, userId: String?, loginUserId: String?, followGroup: String?) {
        this.token = token
        if (!userId.isNullOrEmpty())
            this.userId = userId
        else this.userId = loginUserId
        this.loginId = loginUserId
        this.followGroup = followGroup
        feedListingApi(0)
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
                "login_id" to loginId,
                "post_type" to "post",
                "limit" to "10",
                "offset" to offset.toString()
            )
            if (!followGroup.isNullOrEmpty()) {
                map["follow_group"] = followGroup
            }
            try {
                Coroutines.main {
                    val result = ApiInterface(getApplication()).getProfilePostApi(token, map)
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
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

    fun viewCountApi(postId: String) {
        if (isNetworkAvailable(getApplication())) {
            try {
                val map = HashMap<String, String?>()
                map["user_id"] = loginId
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


}