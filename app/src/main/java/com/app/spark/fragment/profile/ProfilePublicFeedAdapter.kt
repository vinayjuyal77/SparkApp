package com.app.spark.fragment.profile

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.user_feeds.FeedListActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ItemGalleryBinding
import com.app.spark.models.ProfileGetResponse
import com.app.spark.utils.setThumbnailFromUrl
import com.bumptech.glide.Glide


class ProfilePublicFeedAdapter(
    var context: Activity,
    val list: List<ProfileGetResponse.ResultProfile.PostArr>,
    val userID: String, val followGroup: String
) :
    RecyclerView.Adapter<ProfilePublicFeedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemGalleryBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_gallery, parent, false)
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
            item: ProfileGetResponse.ResultProfile.PostArr,
            position: Int
        ) {
            if (item.mediaType == "photo") {
                Glide.with(context).load(item.postMedia).placeholder(R.color.gray_border)
                    .into(binding.imgPost)
            } else if (item.mediaType == "video") {
                setThumbnailFromUrl(context, binding.imgPost, item.postMedia)
            }

            binding.imgPost.setOnClickListener {
                val intent = Intent(context, FeedListActivity::class.java)
                intent.putExtra(IntentConstant.PROFILE_ID, userID)
                if (followGroup.isNotEmpty())
                    intent.putExtra(IntentConstant.FOLLOWING_GROUP, followGroup)
                intent.putExtra(IntentConstant.POSITION, position)
                context.startActivity(intent)
            }
        }

    }
}