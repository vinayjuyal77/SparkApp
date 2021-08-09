package com.app.spark.interfaces

import com.app.spark.models.ChatDetailResponse

interface DrawerListener {
    fun open(dataItem: String)
    fun colse(dataItem: String)

    fun backclick(dataItem: String)
}