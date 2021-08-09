package com.app.spark.activity.user_flicks

import androidx.recyclerview.widget.DiffUtil
import com.app.spark.models.FeedsResponse
import com.app.spark.models.GetFlickResponse
import com.app.spark.models.MediaModel

class FlicksDiffCallback(
    private val newMediaModel: List<GetFlickResponse.Result>,
    private val oldMediaModel: List<GetFlickResponse.Result>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldMediaModel.size
    }

    override fun getNewListSize(): Int {
        return newMediaModel.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldMediaModel[oldItemPosition].flickId == newMediaModel[newItemPosition].flickId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldMediaModel[oldItemPosition] == newMediaModel[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}