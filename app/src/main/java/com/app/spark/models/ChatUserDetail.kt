package com.app.spark.models


import com.google.gson.annotations.SerializedName

data class ChatUserDetail(
    @SerializedName("result")
    val result: Result
) {
    data class Result(
        @SerializedName("chat_type")
        val chatType: String,
        @SerializedName("mediaData")
        val mediaData: List<MediaData>,
        @SerializedName("mutualFriends")
        val mutualFriends: List<MutualFriend>,
        @SerializedName("name")
        val name: String,
        @SerializedName("online_status")
        val onlineStatus: String,
        @SerializedName("profile_picture")
        val profilePicture: String,
        @SerializedName("user_id")
        val userId: Int
    ) {
        data class MediaData(
            @SerializedName("media_type")
            val mediaType: Any?,
            @SerializedName("url")
            val url: String
        )

        data class MutualFriend(
            @SerializedName("chat_type")
            val chatType: String,
            @SerializedName("name")
            val name: String,
            @SerializedName("profile_pic")
            val profilePic: String,
            @SerializedName("user_id")
            val userId: String
        )
    }
}