package com.app.spark.utils

import android.graphics.Typeface.BOLD
import android.graphics.drawable.Drawable
import android.graphics.fonts.FontStyle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.app.spark.utils.date.DateTimeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import de.hdodenhof.circleimageview.CircleImageView
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object BindingAdapters {
    @BindingAdapter("app:imageUrl", "app:placeholder", requireAll = true)
    @JvmStatic
    fun loadImage(view: CircleImageView, imageUrl: String?, placeholder: Drawable) {
        if (imageUrl != null) {
            Glide.with(view).load(imageUrl)
                .thumbnail(
                    Glide.with(view).load(imageUrl).apply(
                        RequestOptions().override(200)
                    )
                )
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(placeholder).error(placeholder).into(view)
        } else {
            try {
                view.setImageDrawable(placeholder)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @BindingAdapter("app:time")
    @JvmStatic
    fun setTime(view: TextView, time: String) {
        val format: DateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        format.timeZone = TimeZone.getTimeZone("UTC")
        val date: Date = format.parse(time)
        view.text = DateTimeUtils.getTimeAgo(view.context, date)
    }

    @BindingAdapter("app:commentTime")
    @JvmStatic
    fun setCommentTime(view: TextView, time: String) {
        val format: DateFormat =
            SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss", Locale.ENGLISH)
        format.timeZone = TimeZone.getTimeZone("UTC")
        val date: Date = format.parse(time.split("GMT")[0].trim())
        view.text = DateTimeUtils.getTimeAgo(view.context, date)
    }

    @BindingAdapter("android:text", "app:userName")
    @JvmStatic
    fun setComment(view: TextView, text: String, userName: String) {
        view.text = SpannableStringBuilder(userName).apply {
            setSpan(
                StyleSpan(BOLD),
                0,
                userName.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            append("  ")
            append(text)

        }
    }

    @BindingAdapter("app:mediaType")
    @JvmStatic
    fun setViewVisibility(view: ImageView, mediaType: String) {
        if (mediaType != "image") {
            view.visibility = View.VISIBLE
        } else view.visibility = View.GONE

    }
}