package com.app.spark.interfaces

import android.widget.LinearLayout
import com.app.spark.models.ChatDetailResponse

interface ChatMessageSelectedListener {
    fun chatMessageSelected(chatItem: ChatDetailResponse.Result.Data?, position: Int)
    fun deleteMessage(chatItem: ChatDetailResponse.Result.Data?, position: Int)
    fun copyMessage(chatItem: ChatDetailResponse.Result.Data?, position: Int)
    fun textMessageClick(chatItem: ChatDetailResponse.Result.Data?, position: Int,view: LinearLayout)
    fun downloadMedia(downloadUrl:String)
}