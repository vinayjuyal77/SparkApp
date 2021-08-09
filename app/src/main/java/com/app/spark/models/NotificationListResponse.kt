package com.app.spark.models


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "notification"
)
data class NotificationListResponse(
    @SerializedName("APICODERESULT")
    var aPICODERESULT: String = "",
     @PrimaryKey
     @SerializedName("id")
    var id: String = "",
    @SerializedName("result")
    var result: List<ResultNotification> = listOf(),
    @SerializedName("statusCode")
    var statusCode: Int = 0
) : Serializable

data class ResultNotification(
    @SerializedName("activity")
    var activity: String = "",
    @SerializedName("created_on")
    var createdOn: String = "",
    @SerializedName("message")
    var message: String = "",
    @SerializedName("profile_pic")
    var profilePic: String = "",
    @SerializedName("user_id")
    var userId: String = "",
    @SerializedName("username")
    var username: String = "",
    @SerializedName("isFollowing")
    var isFollowing: String = "",
    @SerializedName("follow_group")
    var follow_Group: String = "",
    @SerializedName("name")
    var name: String = ""
) : Serializable