package com.app.spark.models


import com.google.gson.annotations.SerializedName

data class CountryStateResponse(
    @SerializedName("APICODERESULT")
    val aPICODERESULT: String,
    @SerializedName("result")
    val result: List<Result>,
    @SerializedName("statusCode")
    val statusCode: Int
) {
    data class Result(
        @SerializedName("id")
        val id: String,
        @SerializedName("name")
        val name: String
    )
}