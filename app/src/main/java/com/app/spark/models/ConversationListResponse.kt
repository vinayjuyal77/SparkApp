package com.app.spark.models


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class ConversationListResponse(
    @SerializedName("result")
    val result: List<Result>
) {

    @Entity(tableName = "chatlist")
    data class Result(
        @SerializedName("date_added")
        val dateAdded: String,
        @SerializedName("msg")
        val msg: String,
        @SerializedName("action")
        val action: String,
        @SerializedName("msg_type")
        val msgType: String,
        @SerializedName("media_type")
        val mediaType: String,
        @SerializedName("group_type")
        val groupType: String,
        @SerializedName("chat_type")
        val chatType: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("profile_pic")
        val profilePic: String,
        @SerializedName("pin_status")
        val pinStatus: String,
        @SerializedName("mute_status")
        val muteStatus: String,
        @SerializedName("is_admin")
        val isAdmin: String,
        @PrimaryKey
        @SerializedName("user_id")
        val userId: String
    )
}