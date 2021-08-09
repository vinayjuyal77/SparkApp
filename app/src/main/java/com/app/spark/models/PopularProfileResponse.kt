package com.app.spark.models


import com.google.gson.annotations.SerializedName

data class PopularProfileResponse(
    @SerializedName("APICODERESULT", alternate = ["statusMessage"])
    val aPICODERESULT: String,
    @SerializedName("result")
    val result: List<PopularProfile>,
    @SerializedName("statusCode")
    val statusCode: Int
) {
    data class PopularProfile(
        @SerializedName("name")
        val name: String,
        @SerializedName("postArr")
        val postArr: List<PostArr>?,
        @SerializedName("profile_pic")
        val profilePic: String,
        @SerializedName("user_id")
        val userId: String,
        @SerializedName("username")
        val username: String
    ) : ListObject() {
        data class PostArr(
            @SerializedName("post_id")
            val postId: String,
            @SerializedName("post_pic")
            val postPic: String
        )

        override fun getType(): Int {
            if (!userId.isNullOrEmpty()) {
                return TYPE_FRIENDS
            } else return TYPE_ADD_FRIENDS
        }
    }
}