package com.app.spark.fragment.profile

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.user_flicks.FlickListActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ItemGalleryBinding
import com.app.spark.models.GetFlickResponse
import com.app.spark.utils.setThumbnailFromUrl
import com.bumptech.glide.Glide
import java.io.File
import java.util.ArrayList
import java.util.logging.Handler


class ProfileFlickAdapter(var context: Activity,
    val list: List<GetFlickResponse.Result>, val userID: String
) :
    RecyclerView.Adapter<ProfileFlickAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemGalleryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_gallery,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position], position)
    }


    inner class ViewHolder(var binding: ItemGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: GetFlickResponse.Result,
            position: Int
        ) {
         //   if(isNetworkAvailable(context)) {
              //  setThumbnailFromUrl(context, binding.imgPost, item.flickMedia)
            Glide.with(context).load(item.flick_thumbnail).into(binding.imgPost)
           // }
//            else
//            {
             //   setThumbnailFromUrl(context, binding.imgPost, item.localpath)
            //    Glide.with(context).load(item.flickMedia).into(binding.imgPost)

         //   }
            binding.imgPost.setOnClickListener {
                val intent = Intent(context, FlickListActivity::class.java)
                intent.putExtra(IntentConstant.PROFILE_ID, userID)
                intent.putExtra(IntentConstant.POSITION, position)
                context.startActivity(intent)

            }
        }

    }
}