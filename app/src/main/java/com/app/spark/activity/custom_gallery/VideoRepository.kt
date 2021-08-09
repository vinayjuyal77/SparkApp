package com.app.spark.activity.custom_gallery

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.app.spark.activity.camera.adapter.Media
import com.app.spark.constants.AppConstants.MediaConstant.baseProjection
import com.app.spark.constants.AppConstants.MediaConstant.baseProjection2

class VideoRepository (private val context: Context) {
    fun getAllSongs(): List<VideoModel> {
        return songs(makeVideoCursor(),makeImageCursor())
    }
    private fun songs(cursor: Cursor?,cursorImage: Cursor?): List<VideoModel> {
        val songs = arrayListOf<VideoModel>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getVideoFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }
        if (cursorImage != null && cursorImage.moveToFirst()) {
            do {
                songs.add(getImageFromCursorImpl(cursorImage))
            } while (cursorImage.moveToNext())
        }
        cursor?.close()
        cursorImage?.close()
        return songs.sortedByDescending {it.date}
    }
    private fun getImageFromCursorImpl(cursor: Cursor): VideoModel {
        val id = cursor.getLong(1)
        val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        val date = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN))
        val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        return VideoModel(id, "", path,0,date, contentUri,false)
    }
    private fun getVideoFromCursorImpl(cursor: Cursor): VideoModel {
        val title = cursor.getString(0)
        val id = cursor.getLong(1)
        val path = cursor.getString(2)
        val duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
        val date = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN))
        val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
        return VideoModel(id, title, path,duration,date,contentUri,true)
    }
    @SuppressLint("Recycle")
    private fun makeVideoCursor(): Cursor? {
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        return try {
            context.applicationContext.contentResolver.query(
                uri,
                baseProjection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_TAKEN} DESC"
            )
        } catch (e: SecurityException) {
            null
        }
    }
    @SuppressLint("Recycle")
    private fun makeImageCursor(): Cursor? {
        val uriImage: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        return try {
            context.applicationContext.contentResolver.query(
                uriImage,
                baseProjection2,
                null,
                null,
                "${MediaStore.Images.Media.DATE_TAKEN} DESC"
                // MediaStore.Images.Media.DEFAULT_SORT_ORDER
            )
        } catch (e: SecurityException) {
            null
        }
    }
}