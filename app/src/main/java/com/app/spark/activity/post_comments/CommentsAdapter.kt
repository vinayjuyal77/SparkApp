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


class CommentsAdapter(
    var context: Context,
    val list: MutableList<GetCommentsResponse.Result>,
    val itemInterface: OnCommentReplyInterface? = null
) :
    RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    private var selectedItem = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemCommentBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_comment,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateList(list: List<GetCommentsResponse.Result>) {
        this.list.addAll(list)
        notifyItemInserted(itemCount)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position], position, itemInterface)
    }

    inner class ViewHolder(var binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: GetCommentsResponse.Result,
            position: Int,
            itemInterface: OnCommentReplyInterface?
        ) {
            binding.item = item
            binding.executePendingBindings()
            binding.imgLike.isSelected = item.isLiked == "yes"
            binding.tvTotalLikes.text = "${item.likeCount} Sparkks"
            binding.viewBg.visibility=View.GONE
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
                itemInterface?.onCommentReply(item.commentId, item.userId, item.username)
            }
            if (!item.childComment.isNullOrEmpty()) {
                binding.rvChildComment.visibility = View.VISIBLE
                binding.rvChildComment.adapter =
                    ChildCommentsAdapter(context, item.childComment.toMutableList(), itemInterface)
            } else {
                binding.rvChildComment.visibility = View.GONE
            }
            binding.clComment.setOnLongClickListener {
                binding.viewBg.visibility=View.VISIBLE
                itemInterface?.onCommentDeleteReport(item.commentId, item.userId,binding.viewBg)
                return@setOnLongClickListener true
            }
        }

    }
}