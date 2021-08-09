package com.app.spark.interfaces

import com.app.spark.models.ChatDetailResponse

interface AudioPlayerListener {
    fun playAudio(dataItem: ChatDetailResponse.Result.Data?, position: Int)
}