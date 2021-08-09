package com.app.spark.fragment.explore

import androidx.recyclerview.widget.DiffUtil
import com.app.spark.models.FeedsResponse
import com.app.spark.models.MediaModel
import com.app.spark.models.ResultNotification

class NotificationDiffCallback(
    private val newMediaModel: List<ResultNotification>,
    private val oldMediaModel: List<ResultNotification>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldMediaModel.size
    }

    override fun getNewListSize(): Int {
        return newMediaModel.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldMediaModel[oldItemPosition].userId == newMediaModel[newItemPosition].userId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldMediaModel[oldItemPosition] == newMediaModel[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}