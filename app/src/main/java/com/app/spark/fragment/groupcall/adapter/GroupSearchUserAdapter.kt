package com.app.spark.fragment.groupcall.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.databinding.ItemGroupListBinding
import com.app.spark.databinding.ItemGroupSearchUserListBinding

class GroupSearchUserAdapter(private val hoursList: List<String>)
    : RecyclerView.Adapter<GroupSearchUserAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGroupSearchUserListBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = 3

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {



               holder.binding.username.text = "@kunam_kumar12323"

//                val hours = "$hours learning hours, $country"
//                binding.topLearnerTime.text = hours
//                GlideApp.with(holder.itemView.context)
//                    .load(badgeUrl)
//                    .into(binding.topLearnerImage)
//
//                holder.itemView.setOnClickListener {
//                    Toast.makeText(holder.itemView.context, hours,
//                        Toast.LENGTH_SHORT).show()

        }



    inner class ViewHolder(val binding: ItemGroupSearchUserListBinding)
        :RecyclerView.ViewHolder(binding.root)

}
