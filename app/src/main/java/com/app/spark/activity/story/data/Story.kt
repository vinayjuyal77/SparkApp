package com.app.spark.activity.story.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Story(val url: String, val storyDate: Long) : Parcelable {

    fun isVideo() =  url.contains(".mp4") ||  url.contains(".webm")
}