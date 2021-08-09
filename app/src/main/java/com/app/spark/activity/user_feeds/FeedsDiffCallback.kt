package com.app.spark.activity.user_feeds

import androidx.recyclerview.widget.DiffUtil
import com.app.spark.models.FeedsResponse
import com.app.spark.models.MediaModel

class FeedsDiffCallback(
    private val newMediaModel: List<FeedsResponse.Result>,
    private val oldMediaModel: List<FeedsResponse.Result>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldMediaModel.size
    }

    override fun getNewListSize(): Int {
        return newMediaModel.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldMediaModel[oldItemPosition].postId == newMediaModel[newItemPosition].postId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldMediaModel[oldItemPosition] == newMediaModel[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}