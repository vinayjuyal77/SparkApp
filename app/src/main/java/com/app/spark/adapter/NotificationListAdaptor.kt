package com.app.spark.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.UsersProfileActivity
import com.app.spark.activity.main.MainActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ItemExploreNotificationBinding
import com.app.spark.fragment.explore.NotificationDiffCallback
import com.app.spark.interfaces.NotificationListClickItem
import com.app.spark.models.ResultNotification
import com.app.spark.utils.date.DateTimeUtils
import com.bumptech.glide.Glide
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class NotificationListAdaptor(
    var context: Context, val list: MutableList<ResultNotification>,
    val listner: NotificationListClickItem
) :
    RecyclerView.Adapter<NotificationListAdaptor.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemExploreNotificationBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_explore_notification,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(context, list[position], position)
    }

    inner class ViewHolder(var binding: ItemExploreNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(context: Context, item: ResultNotification, position: Int) {
            loadImagesProfile(context, item.profilePic, "", binding.imgProfilePic)
            binding.tvTitle.text = item.message
            binding.tvComment.text = item.activity
            val format: DateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            format.timeZone = TimeZone.getTimeZone("UTC")
            val date: Date = format.parse(item.createdOn)
            binding.tvTime.text = DateTimeUtils.getTimeAgo(context, date)

            if(item.activity.equals("Follow",true)){
                binding.imgAddConnection.visibility= View.VISIBLE
                binding.imgPost.visibility=View.GONE
            }else{
                binding.imgAddConnection.visibility= View.GONE
                binding.imgPost.visibility=View.GONE
            }

            binding.imgAddConnection.setOnClickListener {
                listner.onNotiChangeConnectionItem(item)
            }

            binding.imgMenu.setOnClickListener {
                listner.onNotiOverFlowMenuClickItem(item)
            }
            binding.imgProfilePic.setOnClickListener {
                val ins = Intent(context, UsersProfileActivity::class.java)
                ins.putExtra(IntentConstant.PROFILE_ID, item.userId)
                context.startActivity(ins)
            }
        }
    }

    fun updateList(it: List<ResultNotification>) {
        val diffResult = DiffUtil.calculateDiff(NotificationDiffCallback(it, list))
        list.clear()
        list.addAll(it)
        diffResult.dispatchUpdatesTo(this)
    }


    private fun loadImagesProfile(
        context: Context,
        imageUrl: String,
        thumbNail: String,
        imageView: ImageView
    ) {
        if (!thumbNail.trim().isNullOrEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                //.diskCacheStrategy(DiskCacheStrategy.NONE)
                .thumbnail(Glide.with(context).load(thumbNail))
                .into(imageView)
        } else {
            Glide.with(context)
                .load(imageUrl)
                //.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .thumbnail(Glide.with(context).load(R.drawable.ic_placeholder))
                .into(imageView)
        }
    }


}