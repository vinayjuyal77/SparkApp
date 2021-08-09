package com.app.spark.models


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GetFlickResponse(
    @SerializedName("APICODERESULT")
    val aPICODERESULT: String,
    @SerializedName("result")
    val result: List<Result>,
    @SerializedName("statusCode")
    val statusCode: Int
) {
    @Entity(tableName = "flickResult"
    )
    data class Result(
        @SerializedName("comment_count")
        val commentCount: String,
        @SerializedName("created_on")
        val createdOn: String,
        @PrimaryKey
        @SerializedName("flick_id")
        val flickId: String,
        @SerializedName("flick_media")
        var flickMedia: String,
        @SerializedName("flick_thumbnail")
        var flick_thumbnail: String,
        @SerializedName("isLiked")
        var isLiked: String,
        @SerializedName("like_count")
        var likeCount: String,
        @SerializedName("profile_pic")
        val profilePic: String,
        @SerializedName("flick_info")
        val flickInfo: String,
        @SerializedName("user_id")
        val userId: String,
        @SerializedName("username")
        val username: String,
        @SerializedName("view_count")
        var viewCount: String,
        @SerializedName("isfollowing")
        var isfollowing: String,
        @SerializedName("isFollower")
        var isFollower: String,
        @SerializedName("localpath")
          var localpath: String,
        @SerializedName("value")
    var value: Boolean = false
    ) : Serializable
}