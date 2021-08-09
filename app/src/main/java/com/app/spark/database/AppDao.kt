package com.app.spark.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.spark.models.*

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatMessage(chatMessage: ChatDetailResponse.Result.Data)



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatMessages(chatMessage: List<ChatDetailResponse.Result.Data>)


    @Query("Select * from chatDetails where chatId=:channelId")
    fun getChatMessage(channelId: String?): LiveData<List<ChatDetailResponse.Result.Data>>



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProfile(profileGetResponse: ProfileGetResponse)



    @Query("Select * from profileDetail")
    fun getProfileData(): ProfileGetResponse



    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFeedData(chatMessage: List<FeedsResponse.Result>)


    @Query("Select * from feedResult")
    fun getFeed(): List<FeedsResponse.Result>

    @Query("Delete from feedResult")
    fun deleteallfeed()


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFlickData(chatMessage: List<GetFlickResponse.Result>)


    @Query("Select * from flickResult")
    fun getFlick(): List<GetFlickResponse.Result>

    @Query("Delete from flickResult")
    fun deleteallflick()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotification(notificationListResponse : NotificationListResponse)


    @Query("Select * from notification")
    fun getNotfication(): NotificationListResponse


}