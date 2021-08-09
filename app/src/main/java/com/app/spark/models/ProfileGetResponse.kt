package com.app.spark.models


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "profileDetail"
)
data class ProfileGetResponse(
    @PrimaryKey
    @SerializedName("id")
    var id: String = "",
    @SerializedName("APICODERESULT")
    var aPICODERESULT: String = "",
    @SerializedName("result")
    var result: ResultProfile = ResultProfile(),
    @SerializedName("statusCode")
    var statusCode: Int = 0,

) : Serializable {

    data class ResultProfile(
        @SerializedName("isFollower")
        var isFollowers: String = "",
        @SerializedName("verified")
        var verified: Boolean = false,
        @SerializedName("isPopular")
        var isPopular: Boolean = false,
        @SerializedName("isfollowing")
        var isfollowing: String = "",
        @SerializedName("flickArr")
        var flickArr: List<GetFlickResponse.Result> = listOf(),
        @SerializedName("flicks")
        var flicks: String = "",
        @SerializedName("follower")
        var follower: String = "",
        @SerializedName("following")
        var following: String = "",
        @SerializedName("name")
        var name: String = "",
        @SerializedName("personalArr")
        var personalArr: List<PostArr> = listOf(),
        @SerializedName("post")
        var post: String = "",
        @SerializedName("postArr")
        var postArr: List<PostArr> = listOf(),
        @SerializedName("professionalArr")
        var professionalArr: List<PostArr> = listOf(),
        @SerializedName("profile_pic")
        var profilePic: String = "",
        @SerializedName("user_bio")
        var userBio: String = "",
        @SerializedName("gender")
        var gender: String = "",
        @SerializedName("dob")
        var dob: String = "",
        @SerializedName("location")
        var location: String = "",
        @SerializedName("latitude")
        var lat: String = "",
        @SerializedName("longitude")
        var long: String = "",
        @PrimaryKey
        @SerializedName("user_id")
        var userId: String = "",
        @SerializedName("username")
        var username: String = "",
        @SerializedName("follow_group")
        var follow_group: String = ""

    ) : Serializable {


        data class PostArr(

            @SerializedName("created_on")
            var createdOn: String = "",
            @SerializedName("media_type")
            var mediaType: String = "",
            @SerializedName("post_id")
            var postId: String = "",
            @SerializedName("post_info")
            var postInfo: String = "",
            @SerializedName("post_media")
            var postMedia: String = ""
        ) : Serializable
    }
}