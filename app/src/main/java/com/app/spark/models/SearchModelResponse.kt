package com.app.spark.models


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SearchModelResponse(
    @SerializedName("APICODERESULT")
    var aPICODERESULT: String = "",
    @SerializedName("result")
    var result: List<ResultFollowing> = listOf(),
    @SerializedName("statusCode")
    var statusCode: Int = 0
):Serializable
