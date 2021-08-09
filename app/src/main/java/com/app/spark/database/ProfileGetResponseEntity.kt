package com.app.spark.database


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.spark.models.GetFlickResponse
import com.app.spark.models.ProfileGetResponse
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "profileDetail"
)
data class ProfileGetResponseEntity(
    @PrimaryKey @ColumnInfo(name = "profile") val option:List<ProfileGetResponse>
)