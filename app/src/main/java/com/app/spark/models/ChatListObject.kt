package com.app.spark.models

abstract class ChatListObject {

    companion object {
        const val DATE_TYPE = 0
        const val SENDER_MESSAGE = 1
        const val RECEIVER_MESSAGE = 2
    }
}