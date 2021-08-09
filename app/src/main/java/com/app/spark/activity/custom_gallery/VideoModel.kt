package com.app.spark.activity.custom_gallery
import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class VideoModel(
    val id: Long,
    val name: String,
    val path: String,
    val duration: Long,
    val date: String,
    var uriPath:Uri,
    var type:Boolean=true,
    var isSelect:Boolean=false
):Parcelable
