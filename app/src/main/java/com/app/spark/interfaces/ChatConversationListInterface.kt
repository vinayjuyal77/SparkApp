package com.app.spark.interfaces

interface ChatConversationListInterface {
    fun onDelete(otherUserId: String)
    fun onPin(otherUserId: String, pinStatus: String)
    fun onMute(otherUserId: String, muteStatus: String)
}