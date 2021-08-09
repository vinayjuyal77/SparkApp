package com.app.spark.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.ParseException
import android.net.Uri
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ShareCompat
import com.app.spark.R
import com.app.spark.utils.date.DateTimeUtils
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileInputStream
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


fun isNetworkAvailable(context: Context): Boolean {

    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
        return true
    } else {
        showToastShort(context, context.getString(R.string.please_check_internet))
        return false
    }

}


fun getRequestBody(str: String?): RequestBody = RequestBody.create(
    "text/plain".toMediaTypeOrNull(),
    str.toString()
)

fun getImageFilePart(str: String, file: File): MultipartBody.Part =
    MultipartBody.Part.createFormData(
        str,
        file.name,
        file.asRequestBody(URLConnection.guessContentTypeFromName(file.name).toMediaTypeOrNull())
    )

fun getImageFilePart(str: String, file: File, mimeType: String): MultipartBody.Part =
    MultipartBody.Part.createFormData(
        str, file.name, file.asRequestBody(mimeType.toMediaTypeOrNull())
    )




fun setEditTextMaxLength(editText: EditText, length: Int) {
    val filterArray = arrayOfNulls<InputFilter>(1)
    filterArray[0] = InputFilter.LengthFilter(length)
    editText.filters = filterArray
}

fun showToastShort(context: Context?, msg: String?) {
    if (context != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun showToastLong(context: Context?, msg: String?) {
    if (context != null) Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
}

fun showSnackBar(view: View, msg: String) {
    Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show()
}

fun share(activity: Context, text: String) {
    val intent = ShareCompat.IntentBuilder.from(activity!! as Activity)
        .setType("text/plain")
        .setText(text)
        .createChooserIntent()
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    activity?.startActivity(intent)
}

fun copyText(context: Context, text: String) {
    val clipboard: ClipboardManager? =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText(context.getString(R.string.app_name), text)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
}

fun returnChatType(type: String): Int {
    return when (type) {
        "personal" -> 0
        "professional" -> 1
        else -> 2
    }
}

fun retriveVideoFrameFromVideo(context: Context, videoPath: Uri?): Bitmap? {
    var bitmap: Bitmap? = null
    var mediaMetadataRetriever: MediaMetadataRetriever? = null
    try {
        mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, videoPath)
        //   mediaMetadataRetriever.setDataSource(videoPath);
        bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        mediaMetadataRetriever?.release()
    }
    return bitmap
}

fun setThumbnailFromUrl(activity: Activity, shapeableImageView: ShapeableImageView, url: String) {
    try {
        Thread {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(url, HashMap())
            val bmFrame = mediaMetadataRetriever.getFrameAtTime(1) //unit in microsecond
            activity?.runOnUiThread {
                shapeableImageView.setImageBitmap(bmFrame)
            }

        }.start()

    } catch (e: java.lang.Exception) {
    }
}


 fun  getFileDuration( context :Activity, shapeableImageView: ShapeableImageView, file :String) {
    var result = null;
    var  retriever : MediaMetadataRetriever? = null;
    var  inputStream  : FileInputStream?= null;

    try {
        retriever = MediaMetadataRetriever()
        inputStream = FileInputStream(file)
        retriever.setDataSource(inputStream.getFD());
        val bmFrame = retriever.getFrameAtTime(1000) //unit in microsecond
        context?.runOnUiThread {
            shapeableImageView.setImageBitmap(bmFrame)
        }
    } catch (e: java.lang.Exception) {
    }
}

@SuppressLint("SimpleDateFormat")
fun parseDateToTime(time: String, inputFormat: String, outputFormat: String): String? {
    val inputPattern = inputFormat
    val outputPattern = outputFormat
    val inputFormat = SimpleDateFormat(inputPattern)
    val outputFormat = SimpleDateFormat(outputPattern)
    var date: Date? = null
    var str: String? = null
    try {
        //inputFormat.timeZone=TimeZone.getTimeZone("UTC")
        outputFormat.timeZone = TimeZone.getDefault()
        date = inputFormat.parse(time)
        str = outputFormat.format(date)
    } catch (e: ParseException) {
        e.printStackTrace()
        return str
    }
    return str
}

fun clearAllgoToActivity(context: Context, act: Class<*>?) {
    val i = Intent(context, act)
    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(i)
}

fun pickDate(context: Context?, editText: TextView?) {
    val c = Calendar.getInstance()
    val mYear = c[Calendar.YEAR]
    val mMonth = c[Calendar.MONTH]
    val mDay = c[Calendar.DAY_OF_MONTH]
    val dpd = DatePickerDialog(
            context!!,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                editText?.text = DateTimeUtils.getCustomeDateAndTimeFromTimeStamp("$year-${monthOfYear+1}-$dayOfMonth","yyyy-M-d","yyyy-MM-dd")
            },
            mYear,
            mMonth,
            mDay
    )

    //dpd.getDatePicker().setMaxDate(c.getTimeInMillis());
    dpd.datePicker.maxDate = System.currentTimeMillis() - 1000
    dpd.show()
}

fun pickCalDate(context: Context?, editText: TextView?) {
    val c = Calendar.getInstance()
    val mYear = c[Calendar.YEAR]
    val mMonth = c[Calendar.MONTH]
    val mDay = c[Calendar.DAY_OF_MONTH]
    val dpd = DatePickerDialog(
            context!!,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                if (dayOfMonth==mDay&&monthOfYear==mMonth&&year==mYear){
                    editText?.text="Present"
                }else
                editText?.text = DateTimeUtils.getCustomeDateAndTimeFromTimeStamp("$year-${monthOfYear+1}-$dayOfMonth","yyyy-M-d","yyyy-MM-dd")
            },
            mYear,
            mMonth,
            mDay
    )

    //dpd.getDatePicker().setMaxDate(c.getTimeInMillis());
    dpd.datePicker.maxDate = System.currentTimeMillis() - 1000
    dpd.show()
}

