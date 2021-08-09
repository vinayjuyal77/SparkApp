package com.app.spark.models


import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class ChatDetailResponse(
    @SerializedName("result")
    val result: Result
) {
    data class Result(
        @SerializedName("block_status")
        val blockStatus: String,
        @SerializedName("chat_type")
        val chatType: String,
        @SerializedName("data")
        val data: List<Data>,
        @SerializedName("my_block_status")
        val myBlockStatus: String,
        @SerializedName("online_status")
        val onlineStatus: String,
        @SerializedName("profile_picture")
        val profilePicture: String
    ) {
        @Entity(tableName = "chatDetails")
        data class Data(
            @SerializedName("action")
            val action: String?,
            @SerializedName("chat_status")
            val chatStatus: String?,
            @SerializedName("chat_type")
            val chatType: String?,
            @SerializedName("date_added")
            val dateAdded: String?,
            @SerializedName("delete_for_everyone")
            val deleteForEveryone: String?,
            @SerializedName("delete_status")
            val deleteStatus: Int?,
            @SerializedName("deleted_by")
            val deletedBy: String?,
            @SerializedName("group_id")
            val groupId: Int?,
            @SerializedName("chat_id")
            var chatId: String?,
            @PrimaryKey
            @SerializedName("id")
            val id: Int,
            @SerializedName("name")
            val name: String?,
            @SerializedName("msg")
            val msg: String?,
            @SerializedName("msg_type")
            val msgType: String?,
            @SerializedName("r_id")
            val rId: Int?,
            @SerializedName("read_status")
            val readStatus: String?,
            @SerializedName("s_id")
            val sId: Int?,
            @SerializedName("thumb_url")
            val thumbUrl: String?,
            @SerializedName("url")
            val url: String?,
            var isSelected: Boolean=false
        ) : ChatListObject(){
            fun getType(userID: String?): Int {
                return when {
                    userID.isNullOrEmpty() -> {
                        DATE_TYPE
                    }
                    userID == sId.toString() -> {
                        SENDER_MESSAGE
                    }
                    else -> RECEIVER_MESSAGE
                }
            }
        }
    }
}