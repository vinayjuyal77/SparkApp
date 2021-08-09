package com.app.spark.models


import android.graphics.Bitmap
import android.net.Uri
import com.google.gson.annotations.SerializedName

data class MediaModel(
    @SerializedName("date")
    val date: String,
    @SerializedName("id")
    val id: Long,
    @SerializedName("mediaType")
    val mediaType: Int,
    @SerializedName("mimeType")
    val mimeType: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("path")
    val path: String?,
    @SerializedName("bucketName")
    val bucketName: String?,
    @SerializedName("thumbNail")
    val thumbNail: Bitmap?
)