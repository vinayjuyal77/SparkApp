package com.app.spark.models


import com.google.gson.annotations.SerializedName

data class GetCommentsResponse(
    @SerializedName("APICODERESULT")
    val aPICODERESULT: String,
    @SerializedName("result")
    val result: List<Result>,
    @SerializedName("statusCode")
    val statusCode: Int
) {
    data class Result(
        @SerializedName("child_comment")
        val childComment: List<ChildComment>,
        @SerializedName("comment")
        val comment: String,
        @SerializedName("comment_id")
        val commentId: String,
        @SerializedName("isLiked")
        var isLiked: String,
        @SerializedName("likecount")
        var likeCount: String,
        @SerializedName("created_on")
        val createdOn: String,
        @SerializedName("profile_pic")
        val profilePic: String,
        @SerializedName("user_id")
        val userId: String,
        @SerializedName("username")
        val username: String,
        @SerializedName("user_refer_id")
        val userReferId: String,
        @SerializedName("user_refer_username")
        val userReferUsername: String
    ) {
        data class ChildComment(
            @SerializedName("comment")
            val comment: String,
            @SerializedName("comment_id")
            val commentId: String,
            @SerializedName("isLiked")
            var isLiked: String,
            @SerializedName("profile_pic")
            val profilePic: String,
            @SerializedName("likecount")
            var likeCount: String,
            @SerializedName("created_on")
            val createdOn: String,
            @SerializedName("user_id")
            val userId: String,
            @SerializedName("username")
            val username: String,
            @SerializedName("user_refer_id")
            val userReferId: String,
            @SerializedName("user_refer_username")
            val userReferUsername: String
        )
    }
}