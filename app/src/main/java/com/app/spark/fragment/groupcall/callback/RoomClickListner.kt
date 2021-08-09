package com.app.spark.fragment.groupcall.callback

import com.app.spark.models.USER_INFO

interface RoomClickListner {
    fun onSpeakerItemClick(pos:Int, userData: USER_INFO, type:String)
    fun onListnerItemClick(pos:Int,userData: USER_INFO,type:String)
}


interface RaiseHandCallbackListner {
    fun onRaiseHandCallbackClick()
}