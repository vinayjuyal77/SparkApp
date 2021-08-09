package com.app.spark.activity.post_comments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import com.app.spark.R
import com.app.spark.models.*
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines
import com.app.spark.utils.getImageFilePart
import com.app.spark.utils.getRequestBody
import okhttp3.RequestBody
import java.io.File
import java.util.*

class PostCommentsViewModel(application: Application) : AndroidViewModel(application) {
    val errRes = MutableLiveData<Int>()
    val errString = MutableLiveData<String>()
    val response = MutableLiveData<String>()
    val postDetails = MutableLiveData<FeedsResponse.Result>(null)
    val flickDetails = MutableLiveData<GetFlickResponse.Result>(null)
    val commentsList = MutableLiveData<List<GetCommentsResponse.Result>>()
    private var token: String? = null
    private var userId: String? = null
    fun setPostId(
        postDetails: FeedsResponse.Result?,
        flickDetails: GetFlickResponse.Result?,
        userId: String?,
        token: String?
    ) {
        if (postDetails != null)
            this.postDetails.value = postDetails
        if (flickDetails != null)
            this.flickDetails.value = flickDetails
        this.userId = userId
        this.token = token
    }

    fun getCommentAPI() {
        try {
            val map = hashMapOf<String, String?>(
                "user_id" to userId
            )

            if (postDetails.value != null) {
                map["post_id"] = postDetails.value?.postId
                map["content_type"] = "post"
            }
            if (flickDetails.value != null) {
                map["post_id"] = flickDetails.value?.flickId
                map["content_type"] = "flick"
            }
            Coroutines.main {
                val result = ApiInterface(getApplication())?.getCommentsApi(
                    token, map
                )
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        commentsList.postValue(result.body()?.result)
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

    fun addCommentAPI(postInfo: String?, parentCommentId: String?, userReferId: String?) {
        try {
            val map = hashMapOf<String, String?>(
                "user_id" to userId,
                "comment" to postInfo

            )
            if (postDetails.value != null) {
                map["post_id"] = postDetails.value?.postId
                map["content_type"] = "post"
            }
            if (flickDetails.value != null) {
                map["post_id"] = flickDetails.value?.flickId
                map["content_type"] = "flick"
            }
            if (parentCommentId != null) {
                map["parent_id"] = parentCommentId
            }
            if (userReferId != null) {
                map["user_refer_id"] = userReferId
            }
            Coroutines.main {
                val result = ApiInterface(getApplication())?.commentAPI(
                    token, map
                )
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        response.postValue(result.body()?.APICODERESULT)
                        getCommentAPI()
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

    fun likeUnlikeApi(
        commentId: String,
        isLike: Boolean
    ) {
        try {
            val map = HashMap<String, String?>()
            map["user_id"] = userId
            map["comment_id"] = commentId
            if (postDetails.value != null) {
                map["content_type"] = "post"
            }
            if (flickDetails.value != null) {
                map["content_type"] = "flick"
            }
            if (isLike)
                map["type"] = "Like"
            else map["type"] = "Unlike"
            Coroutines.main {
                val result = ApiInterface(getApplication())?.commentLikeUnlikeAPI(token, map)
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

    fun reportPost(isUnfollow: Boolean) {
        if (isUnfollow) {
            unfollowApi()
            blockedUser()
        }
        try {
            val map = HashMap<String, String?>()
            map["user_id"] = userId
            if (postDetails.value != null) {
                map["post_id"] = postDetails.value?.postId
                map["content_type"] = "post"
            }
            if (flickDetails.value != null) {
                map["post_id"] = flickDetails.value?.flickId
                map["content_type"] = "flick"
            }

            Coroutines.main {
                val result = ApiInterface(getApplication())?.reportFeedApi(token, map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
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

    fun deleteComment(commentId: String?) {
        try {
            val map = HashMap<String, String?>()
            map["user_id"] = userId
            map["comment_id"] = commentId
            if (postDetails.value != null) {
                map["content_type"] = "post"
            }
            if (flickDetails.value != null) {
                map["content_type"] = "flick"
            }
            Coroutines.main {
                val result = ApiInterface(getApplication())?.deleteCommentAPI(token, map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        errString.postValue(result.body()?.APICODERESULT)
                        getCommentAPI()
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

    private fun unfollowApi() {
        try {
            val map = HashMap<String, String>()
            map["follow_by"] = userId!!
            map["follow_to"] = postDetails.value!!.userId
            map["follow_group"] = "public"
            map["type"] = "Unfollow"
            Coroutines.main {
                val result = ApiInterface(getApplication())?.followUnfollowAPI(token!!, map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
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

    fun blockedUser() {
        try {
            val map = HashMap<String, String>()
            map["blocked_by"] = userId!!

            if (postDetails.value != null) {
                map["blocked_to"] = postDetails.value?.userId!!
            }
            if (flickDetails.value != null) {
                map["blocked_to"] = flickDetails.value?.userId!!
            }
            map["type"] = "block"
            Coroutines.main {
                val result = ApiInterface(getApplication())?.blockuserAction(token!!, map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
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