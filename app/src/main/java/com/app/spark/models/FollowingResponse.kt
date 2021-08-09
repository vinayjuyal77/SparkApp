package com.app.spark.models


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FollowingResponse(
    @SerializedName("APICODERESULT")
    var aPICODERESULT: String = "",
    @SerializedName("result")
    var result: List<ResultFollowing> = listOf(),
    @SerializedName("statusCode")
    var statusCode: Int = 0,
    @SerializedName("type")
    var type: String = ""
) : Serializable

data class ResultFollowing(
    @SerializedName("name")
    var name: String = "",
    @SerializedName("profile_pic")
    var profilePic: String = "",
    @SerializedName("user_id")
    var userId: String = "",
    @SerializedName("username")
    var username: String = "",
    @SerializedName("isfollowing")
    var isfollowing: String = "",
    @SerializedName("typeMy")
    var myType: String = "",
    @SerializedName("isFollowing")
    var isFollowing: String = "",
    @SerializedName("follow_group")
    var follow_Group: String = "",
    var isSelected: Boolean = false
)