package com.app.spark.interfaces

import com.app.spark.models.ResultNotification

interface NotificationListClickItem {
    fun onNotiOverFlowMenuClickItem(item:ResultNotification)
    fun onNotiChangeConnectionItem(item:ResultNotification)

}