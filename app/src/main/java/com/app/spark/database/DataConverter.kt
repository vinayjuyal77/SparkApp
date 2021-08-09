package com.app.spark.database

import androidx.room.TypeConverter
import com.app.spark.models.ProfileGetResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type



class  DataConverter {
    @TypeConverter
    fun fromQuestionOption(questinOption: ProfileGetResponse.ResultProfile?): String? {
        if (questinOption == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ProfileGetResponse.ResultProfile?>() {}.type
        return gson.toJson(questinOption, type)

    }

    @TypeConverter
    fun toQuestionOptions(questinOption: String?): ProfileGetResponse.ResultProfile? {
        if (questinOption == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ProfileGetResponse.ResultProfile?>() {}.type
        return gson.fromJson<ProfileGetResponse.ResultProfile>(questinOption, type)
    }
}