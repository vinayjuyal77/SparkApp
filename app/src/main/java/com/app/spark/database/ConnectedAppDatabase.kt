package com.app.spark.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.spark.models.*

@Database(entities = [ChatDetailResponse.Result.Data::class, ProfileGetResponse::class, FeedsResponse.Result::class,
    GetFlickResponse.Result :: class, NotificationListResponse::class], version = 2, exportSchema = false)
@TypeConverters(DataConverter::class, DataConvetorPrr::class, NotificationDataConverter::class)
abstract class ConnectedAppDatabase: RoomDatabase() {
    abstract fun appDao(): AppDao?

    companion object {
        private const val dbName = "connected_db"
        private var instance: ConnectedAppDatabase? = null
        @Synchronized
        fun getInstance(context: Context): ConnectedAppDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConnectedAppDatabase::class.java,
                    dbName
                )
                    .fallbackToDestructiveMigration().allowMainThreadQueries().build()
            }
            return instance
        }
    }
}