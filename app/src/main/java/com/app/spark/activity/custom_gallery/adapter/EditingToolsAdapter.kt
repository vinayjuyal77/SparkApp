package com.app.spark.activity.custom_gallery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import java.util.*

class EditingToolsAdapter(val mOnItemSelected: OnItemSelected) :
    RecyclerView.Adapter<EditingToolsAdapter.ViewHolder>() {
    private val mToolList: MutableList<ToolModel> = ArrayList()

    interface OnItemSelected {
        fun onToolSelected(toolType: ToolType?)
    }

    internal inner class ToolModel(
        val mToolName: String,
        val mToolIcon: Int,
        toolType: ToolType
    ) {
        val mToolType: ToolType
        init {
            mToolType = toolType
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_editing_tools, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mToolList[position]
        holder.txtTool.text = item.mToolName
        holder.imgToolIcon.setImageResource(item.mToolIcon)
    }

    override fun getItemCount(): Int {
        return mToolList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgToolIcon: ImageView
        var txtTool: TextView

        init {
            imgToolIcon = itemView.findViewById(R.id.imgToolIcon)
            txtTool = itemView.findViewById(R.id.txtTool)
            itemView.setOnClickListener { mOnItemSelected.onToolSelected(mToolList[layoutPosition].mToolType) }
        }
    }

    init {
        mToolList.add(ToolModel("MUSIC", R.drawable.ic_music_add, ToolType.MUSIC))
        mToolList.add(ToolModel("STICKER", R.drawable.ic_smile, ToolType.STICKER))
        mToolList.add(ToolModel("EMOJI", R.drawable.ic_smile, ToolType.EMOJI))
        mToolList.add(ToolModel("TEXT", R.drawable.ic_edit_text, ToolType.TEXT))
        mToolList.add(ToolModel("FILTER", R.drawable.ic_filter, ToolType.FILTER))
        mToolList.add(ToolModel("BRUSH", R.drawable.ic_brush_24, ToolType.BRUSH))
        mToolList.add(ToolModel("SPLIT", R.drawable.ic_split, ToolType.SPLIT))
        mToolList.add(ToolModel("ERASER", R.drawable.ic_delete, ToolType.ERASER))
        mToolList.add(ToolModel("TRIM", R.drawable.ic_trim, ToolType.TRIM))
        mToolList.add(ToolModel("CROP", R.drawable.ic_crop, ToolType.CROP))
        mToolList.add(ToolModel("ROTATE", R.drawable.ic_rotate, ToolType.ROTATE))
        mToolList.add(ToolModel("SPEED", R.drawable.ic_speed, ToolType.SPEED))
        mToolList.add(ToolModel("DUPLICATE", R.drawable.ic_duplicate, ToolType.DUPLICATE))
        mToolList.add(ToolModel("TRANSITION", R.drawable.ic_transition, ToolType.TRANSITION))
    }
}