package com.app.spark.activity.gallery

import androidx.recyclerview.widget.DiffUtil
import com.app.spark.models.MediaModel

class MediaDiffCallback(
    private val newMediaModel: List<MediaModel>,
    private val oldMediaModel: List<MediaModel>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldMediaModel.size
    }

    override fun getNewListSize(): Int {
        return newMediaModel.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldMediaModel[oldItemPosition].id.toString() == newMediaModel[newItemPosition].id.toString()
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldMediaModel[oldItemPosition] == newMediaModel[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}