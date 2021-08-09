package com.app.spark.activity.post_comments

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.CodeBoy.MediaFacer.mediaHolders.pictureContent
import com.app.spark.R
import com.app.spark.databinding.ItemChildCommentBinding
import com.app.spark.databinding.ItemCommentBinding
import com.app.spark.databinding.ItemGalleryBinding
import com.app.spark.interfaces.OnCommentReplyInterface
import com.app.spark.interfaces.OnItemSelectedInterface
import com.app.spark.models.GetCommentsResponse
import com.app.spark.models.MediaModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import java.io.File
import java.util.ArrayList


class ChildCommentsAdapter(
    var context: Context,
    val list: MutableList<GetCommentsResponse.Result.ChildComment>,
    val itemInterface: OnCommentReplyInterface? = null
) :
    RecyclerView.Adapter<ChildCommentsAdapter.ViewHolder>() {

    private var selectedItem = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemChildCommentBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_child_comment,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateList(list: List<GetCommentsResponse.Result.ChildComment>) {
        this.list.addAll(list)
        notifyItemInserted(itemCount)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position], position, itemInterface)
    }


    inner class ViewHolder(var binding: ItemChildCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: GetCommentsResponse.Result.ChildComment,
            position: Int,
            itemInterface: OnCommentReplyInterface?
        ) {
            binding.item = item
            binding.executePendingBindings()
            binding.imgLike.isSelected = item.isLiked == "yes"
            binding.tvTotalLikes.text = "${item.likeCount} Sparkks"
            binding.imgLike.setOnClickListener {
                binding.imgLike.isSelected = !binding.imgLike.isSelected
                itemInterface?.onCommentLike(item.commentId, binding.imgLike.isSelected)
                if (binding.imgLike.isSelected) {
                    item.isLiked = "yes"
                    item.likeCount = item.likeCount.toInt().plus(1).toString()
                } else {
                    item.isLiked = "no"
                    item.likeCount = item.likeCount.toInt().minus(1).toString()
                }
                binding.tvTotalLikes.text = "${item.likeCount} Sparkks"

            }
            binding.tvReply.setOnClickListener {
                itemInterface?.onCommentReply(item.commentId,item.userId,item.username)
            }
            binding.clComment.setOnLongClickListener {
                binding.viewBg.visibility= View.VISIBLE
                itemInterface?.onCommentDeleteReport(item.commentId, item.userId,binding.viewBg)
                return@setOnLongClickListener true
            }
        }

    }
}