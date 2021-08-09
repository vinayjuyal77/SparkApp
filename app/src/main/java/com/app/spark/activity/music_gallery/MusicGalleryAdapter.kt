package com.app.spark.activity.music_gallery

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.CodeBoy.MediaFacer.mediaHolders.audioContent
import com.app.spark.R
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ItemMusicGallaryBinding
import com.app.spark.interfaces.OnItemSelectedInterface
import com.app.spark.models.ImportantDataResult
import com.app.spark.utils.SharedPrefrencesManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import java.util.*


class MusicGalleryAdapter(
    var context: Context,
    val list: ArrayList<audioContent>,
    val itemInterface: OnItemSelectedInterface? = null
) :
    RecyclerView.Adapter<MusicGalleryAdapter.ViewHolder>() {

    private var selectedItem = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMusicGallaryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_music_gallary,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position], position, itemInterface)
    }


    inner class ViewHolder(var binding: ItemMusicGallaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: audioContent,
            position: Int,
            itemInterface: OnItemSelectedInterface?
        ) {
           // var file=File(item.filePath.toString())
            binding.tvTitle.text=item.name
            var  pref = SharedPrefrencesManager.getInstance(context)
            var loginDetails = Gson().fromJson(
                pref.getString(PrefConstant.LOGIN_RESPONSE, ""),
                ImportantDataResult::class.java
            )
                if(getThumbnailPath(context,item.filePath).isNullOrEmpty()){
                    Glide.with(context)
                        .load(loginDetails.profile_pic)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .apply(RequestOptions().centerCrop())
                        .into(binding.imgPost)
                }else {
                    Glide.with(context)
                        .load(getThumbnailPath(context,item.filePath))
                        .placeholder(R.drawable.ic_baseline_audiotrack_24)
                        .error(R.drawable.ic_baseline_audiotrack_24)
                        .apply(RequestOptions().centerCrop())
                        .into(binding.imgPost)
                }


            binding.imgPost.setOnClickListener {
                itemInterface?.onItemSelected(position)
            }
        }

    }


    fun getThumbnailPath(
        context: Context,
        path: String
    ): String? {
        var imageId: Long = -1
        val projection =
            arrayOf<String>(MediaStore.MediaColumns._ID)
        val selection: String = MediaStore.MediaColumns.DATA.toString() + "=?"
        val selectionArgs = arrayOf(path)
        var cursor: Cursor? = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            imageId = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID)).toLong()
            cursor.close()
        }
        var result: String? = null
        cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(
            context.contentResolver,
            imageId,
            MediaStore.Images.Thumbnails.MINI_KIND,
            null
        )
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst()
            result =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA))
            cursor.close()
        }
        return result
    }
}