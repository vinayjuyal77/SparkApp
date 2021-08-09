package com.app.spark.models


import com.google.gson.annotations.SerializedName

data class AddEducationResponse(
    @SerializedName("APICODERESULT")
    val aPICODERESULT: String,
    @SerializedName("result")
    val result: Result,
    @SerializedName("statusCode")
    val statusCode: Int
) {
    data class Result(
        @SerializedName("college_name")
        val collegeName: String,
        @SerializedName("created_on")
        val createdOn: String,
        @SerializedName("education_id")
        val educationId: String,
        @SerializedName("education_title")
        val educationTitle: String,
        @SerializedName("end_date")
        val endDate: String,
        @SerializedName("start_date")
        val startDate: String,
        @SerializedName("user_id")
        val userId: String
    )
}