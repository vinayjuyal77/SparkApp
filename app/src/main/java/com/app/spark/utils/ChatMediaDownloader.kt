package com.app.spark.utils

import android.app.DownloadManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.util.Log

import java.io.File
import java.util.concurrent.Executors

class ChatMediaDownloader(val context: Context) {

    private var downLoadID: Long? = null

    fun setupAppResources( downloadUrl: String) {
        Executors.newSingleThreadExecutor().execute {
            val uri = Uri.parse(downloadUrl)
           val filename = uri.lastPathSegment
            downloadFile(filename!!,downloadUrl)
        }
    }

    private fun downloadFile(fileName: String, downloadUrl: String) {
        try {
            if (File(
                    context.getExternalFilesDir(ROOT_PATH),
                    fileName
                ).exists()
            )
            {
                return
            }
            File(context.getExternalFilesDir(ROOT_PATH), fileName)
            //get url of app on server
            val uri = Uri.parse(downloadUrl)
            //set download manager
            val request: DownloadManager.Request = DownloadManager.Request(uri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
//        request.setDescription(context.getString(R.string.download))
//        request.setTitle(context.getString(R.string.app_name))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            //set destination
            request.setDestinationInExternalFilesDir(context, Companion.ROOT_PATH, fileName)
            // get download service and enqueue file
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downLoadID = downloadManager.enqueue(request)
        } catch (e: Exception) {
            Log.v(this.javaClass.simpleName, e.toString())
        }

    }

    companion object {
        const val ROOT_PATH = "Assets"
    }

}