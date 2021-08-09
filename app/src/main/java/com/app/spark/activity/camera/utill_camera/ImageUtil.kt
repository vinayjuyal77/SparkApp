package com.app.spark.activity.camera.utill_camera

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import java.io.ByteArrayOutputStream


fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
    val bytes = ByteArrayOutputStream()
    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path =
        MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "", null)
    return Uri.parse(path)
}

fun getThumnilFromUri(videoUri: Uri):Bitmap{
    return ThumbnailUtils.createVideoThumbnail(videoUri.path!!, MediaStore.Images.Thumbnails.MINI_KIND)!!
}

fun getSongThumbnail(songPath: String): ByteArray? {
    var imgByte: ByteArray?
    MediaMetadataRetriever().also {
        try {
            it.setDataSource(songPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        imgByte = it.embeddedPicture
        it.release()
    }
    return imgByte
}

fun timeConversion(value: Long): String? {
    val videoTime: String
    val dur = value.toInt()
    val hrs = dur / 3600000
    val mns = dur / 60000 % 60000
    val scs = dur % 60000 / 1000
    videoTime = if (hrs > 0) {
        String.format("%02d:%02d:%02d", hrs, mns, scs)
    } else {
        String.format("%02d:%02d", mns, scs)
    }
    return videoTime
}


