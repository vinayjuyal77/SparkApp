package com.app.spark.activity.setting.account.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class SeeLikeAdapter(var context: Context) : RecyclerView.Adapter<SeeLikeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeeLikeAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_like, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SeeLikeAdapter.ViewHolder, position: Int) {

        Glide.with(context)
            .load("loginDetails?.profile_pic")
            .placeholder(R.drawable.ic_menu_profile)
            .into(holder.title)

    }

    override fun getItemCount(): Int {
        return 5
    }


    inner class  ViewHolder(view : View): RecyclerView.ViewHolder(view)
    {


        var title: ImageView = view.findViewById(R.id.imgPost)
    }

}