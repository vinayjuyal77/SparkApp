package com.app.spark.models


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FeedsResponse(
    @SerializedName("APICODERESULT")
    val aPICODERESULT: String,
    @SerializedName("result")
    var result: List<Result>,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("isPlay")
    val isPlay: Int=0

) {
    @Entity(tableName = "feedResult"
    )
    data class Result(
        @SerializedName("comment_count")
        val commentCount: String?,
        @SerializedName("created_on")
        val createdOn: String?,
        @SerializedName("like_count")
        var likeCount: String?,
        @SerializedName("isLiked")
        var isLiked: String?,

        @SerializedName("user_id")
        val userId: String,
        @SerializedName("media_type")
        val mediaType: String?,
        @PrimaryKey
        @SerializedName("post_id")
        var postId: String,
        @SerializedName("post_info")
        val postInfo: String?,
        @SerializedName("post_media")
        var postMedia: String?,
        @SerializedName("local_media")
        var local_media: String?,
        @SerializedName("profile_pic")
        var profilePic: String?,
        @SerializedName("username")
        val username: String?,

        @SerializedName("plays")
        var isPlays: String?,

        @SerializedName("view_count")
        var viewCount: String?,

    ):Serializable
}