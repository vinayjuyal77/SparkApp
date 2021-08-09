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

class UsernamechangesAdapter(var context: Context, var type :  String) : RecyclerView.Adapter<UsernamechangesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsernamechangesAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_logininfo, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UsernamechangesAdapter.ViewHolder, position: Int) {


        if(type == "Your Logins")
        {
            holder.city.text = "Delhi, India"

        }
       else if(type == "Your Logouts")
        {
            holder.city.text = "Delhi, India"

        }

        else if(type == "Phone Numbers")
        {
            holder.city.text = "+91989960654"

        }
        else if(type == "Bio Text")
        {
            holder.city.text = "Welcome to Connectd India"

        }
        else if(type == "Username Changes")
        {
            holder.city.text = "@rahul1223"

        }



    }

    override fun getItemCount(): Int {
        return 2
    }


    inner class  ViewHolder(view : View): RecyclerView.ViewHolder(view)
    {


        var city: TextView = view.findViewById(R.id.city)
    }

}