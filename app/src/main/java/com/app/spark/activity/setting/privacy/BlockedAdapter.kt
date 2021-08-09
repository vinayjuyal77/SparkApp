package com.app.spark.activity.setting.privacy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class BlockedAdapter(var context: Context, var type : String) : RecyclerView.Adapter<BlockedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BlockedAdapter.ViewHolder, position: Int) {


        if(type.equals("Blocked"))
        {
            holder.data_txt.text = "Blocked"
            if(position==2)
            {
                holder.data_txt.text = "Un-Blocked"
                holder.button_layout.background = ContextCompat.getDrawable(context,R.drawable.bg_solid_blue_ractangle)

            }

        }else if(type.equals("Restricted"))
        {
            holder.data_txt.text = type
            if(position==2)
            {
                holder.data_txt.text = "Un-Restricted"
                holder.button_layout.background = ContextCompat.getDrawable(context,R.drawable.bg_solid_blue_ractangle)

            }
        }

        else if(type.equals("Muted"))
        {
            holder.data_txt.text = type
            if(position==2)
            {
                holder.data_txt.text = "Un-Mute"
                holder.button_layout.background = ContextCompat.getDrawable(context,R.drawable.bg_solid_blue_ractangle)

            }
        }

        else if(type.equals("Reported"))
        {
            holder.data_txt.text = type
            if(position==2)
            {
                holder.data_txt.text = "Not Reported"
                holder.button_layout.background = ContextCompat.getDrawable(context,R.drawable.bg_solid_blue_ractangle)

            }

        }





    }

    override fun getItemCount(): Int {
        return 7
    }


    inner class  ViewHolder(view : View): RecyclerView.ViewHolder(view)
    {


        var data_txt: TextView = view.findViewById(R.id.data_txt)
        var button_layout: LinearLayout = view.findViewById(R.id.button_layout)
    }

}