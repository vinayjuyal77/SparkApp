package com.app.spark.models


import com.google.gson.annotations.SerializedName

data class UploadMediaResponse(
    @SerializedName("APICODERESULT")
    val aPICODERESULT: String?,
    @SerializedName("result")
    val result: String?,
    @SerializedName("statusCode")
    val statusCode: Int?
)