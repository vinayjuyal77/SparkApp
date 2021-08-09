package com.app.spark.database

import androidx.room.TypeConverter
import com.app.spark.models.ProfileGetResponse
import com.app.spark.models.ResultNotification
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type



class  NotificationDataConverter {
    @TypeConverter
    fun fromQuestionOption(questinOption: List<ResultNotification>?): String? {
        if (questinOption == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<ResultNotification>?>() {}.type
        return gson.toJson(questinOption, type)

    }

    @TypeConverter
    fun toQuestionOptions(questinOption: String?): List<ResultNotification>? {
        if (questinOption == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<ResultNotification>?>() {}.type
        return gson.fromJson<List<ResultNotification>>(questinOption, type)
    }
}