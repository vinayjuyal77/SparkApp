package com.app.spark.models


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class EditBioResponse(
    @SerializedName("APICODERESULT")
    var aPICODERESULT: String = "",
    @SerializedName("result")
    var result: ResultBioEdit = ResultBioEdit(),
    @SerializedName("statusCode")
    var statusCode: Int = 0
):Serializable

data class ResultBioEdit(
    @SerializedName("accesstoken")
    var accesstoken: String = "",
    @SerializedName("act_type")
    var actType: String = "",
    @SerializedName("completed")
    var completed: String = "",
    @SerializedName("country_code")
    var countryCode: String = "",
    @SerializedName("dob")
    var dob: String = "",
    @SerializedName("email")
    var email: String = "",
    @SerializedName("follow_count")
    var followCount: String = "",
    @SerializedName("gender")
    var gender: String = "",
    @SerializedName("location")
    var location: String = "",
    @SerializedName("mobile")
    var mobile: String = "",
    @SerializedName("name")
    var name: String = "",
    @SerializedName("profile_pic")
    var profilePic: String = "",
    @SerializedName("user_bio")
    var userBio: String = "",
    @SerializedName("user_id")
    var userId: String = "",
    @SerializedName("username")
    var username: String = "",
    @SerializedName("verified")
    var verified: String = ""
):Serializable