package com.app.spark.database

import androidx.room.TypeConverter
import com.app.spark.models.ProfileGetResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class  DataConvetorPrr {
    @TypeConverter
    fun fromQuestionOption(questinOption: ProfileGetResponse.ResultProfile.PostArr?): String? {
        if (questinOption == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ProfileGetResponse.ResultProfile.PostArr?>() {}.type
        return gson.toJson(questinOption, type)
    }

    @TypeConverter
    fun toQuestionOptions(questinOption: String?): ProfileGetResponse.ResultProfile.PostArr? {
        if (questinOption == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ProfileGetResponse.ResultProfile.PostArr?>() {}.type
        return gson.fromJson<ProfileGetResponse.ResultProfile.PostArr>(questinOption, type)
    }
}