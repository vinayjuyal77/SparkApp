package com.app.spark.activity.gallery

import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.database.MergeCursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.spark.models.MediaModel
import com.app.spark.utils.retriveVideoFrameFromVideo
import kotlinx.coroutines.*
import okhttp3.internal.wait
import kotlin.coroutines.CoroutineContext

/**
 * Use Coroutines To Load Images
 */
class ImageViewModel(application: Application) : AndroidViewModel(application), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
    val imagesVideos: MutableList<MediaModel> = mutableListOf()
    private val imagesVideosList = MutableLiveData<List<MediaModel>>()
    private var bucketList: MutableList<String>? = mutableListOf()
    private var imagesLiveData: MutableLiveData<MediaModel> = MutableLiveData()
    fun getImageList(): LiveData<MediaModel> {
        return imagesLiveData
    }

    /**
     * Getting All Images Path.
     *
     * Required Storage Permission
     *
     * @return ArrayList with images Path
     */
    internal suspend fun loadImagesfromSDCard() {
        val columns = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Files.FileColumns.BUCKET_ID,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DISPLAY_NAME
        )
        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
        val queryUri = MediaStore.Files.getContentUri("external")

        val externalCursor = getApplication<Application>().contentResolver.query(
            queryUri,
            columns,
            selection,
            null,  // Selection args (none).
            MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        )

        val internalCursor = getApplication<Application>().contentResolver.query(
            MediaStore.Files.getContentUri("internal"),
            columns,
            selection,
            null,  // Selection args (none).
            MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC" // Sort order.
        )
        val cursors = arrayOfNulls<Cursor>(2)
        cursors[0] = externalCursor
        cursors[1] = internalCursor
        val imagecursor = MergeCursor(cursors)
        var thumbNail: Bitmap? = null
        while (imagecursor.moveToNext()) {
            val imageId: Long =
                imagecursor.getLong(imagecursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
            val name: String =
                imagecursor.getString(imagecursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME))
                    ?: "image_${System.currentTimeMillis()}"
            var path: Uri? = null
            var filePath =
                imagecursor.getString(imagecursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
            val date: String =
                imagecursor.getString(imagecursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED))
            val bucketName: String? =
                imagecursor.getString(imagecursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME))
            val mediaType: Int =
                imagecursor.getInt(imagecursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE))
            val mimeType: String =
                imagecursor.getString(imagecursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))

            if (mediaType == 3) {
                path = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    imageId
                )
                thumbNail = retriveVideoFrameFromVideo(getApplication(), path)

            }
            if (!bucketName.isNullOrEmpty() && !bucketList?.contains(bucketName)!!) {
                bucketList?.add(bucketName)
            }
            val mediaModel = MediaModel(
                date,
                imageId,
                mediaType,
                mimeType,
                name,
                filePath,
                bucketName,
                thumbNail
            )
            imagesLiveData.postValue(mediaModel)
            imagesVideos.add(mediaModel)
            delay(150)
        }
        imagecursor?.close()
    }

    fun getAllImages() {
        launch(Dispatchers.IO) {
            loadImagesfromSDCard()
        }
    }

}