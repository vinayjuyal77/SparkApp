package com.app.spark.activity.setting.security

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class LoginInfoAdapter(var context: Context) : RecyclerView.Adapter<LoginInfoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoginInfoAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_logininfo, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LoginInfoAdapter.ViewHolder, position: Int) {



        holder.city.text = "Delhi, India"

    }

    override fun getItemCount(): Int {
        return 2
    }


    inner class  ViewHolder(view : View): RecyclerView.ViewHolder(view)
    {


        var city: TextView = view.findViewById(R.id.city)
    }

}