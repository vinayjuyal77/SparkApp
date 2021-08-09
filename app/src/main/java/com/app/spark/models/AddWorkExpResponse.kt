package com.app.spark.models


import com.google.gson.annotations.SerializedName

data class AddWorkExpResponse(
    @SerializedName("APICODERESULT")
    val aPICODERESULT: String,
    @SerializedName("result")
    val result: Result,
    @SerializedName("statusCode")
    val statusCode: Int
) {
    data class Result(
        @SerializedName("created_on")
        val createdOn: String,
        @SerializedName("exp_id")
        val expId: String,
        @SerializedName("from_year")
        val fromYear: String,
        @SerializedName("place")
        val place: String,
        @SerializedName("to_year")
        val toYear: String,
        @SerializedName("user_id")
        val userId: String,
        @SerializedName("work_position")
        val workPosition: String
    )
}