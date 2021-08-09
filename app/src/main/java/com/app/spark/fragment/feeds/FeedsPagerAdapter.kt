package com.app.spark.fragment.feeds

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.UsersProfileActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.LayoutAddFriendsBinding
import com.app.spark.databinding.LayoutInitialFriendsBinding
import com.app.spark.interfaces.OnItemSelectedInterface
import com.app.spark.models.ListObject
import com.app.spark.models.PopularProfileResponse
import com.app.spark.utils.share
import com.bumptech.glide.Glide




class FeedsPagerAdapter(
    private val context: Activity,
    private val list: MutableList<PopularProfileResponse.PopularProfile>,
    val viewModel: FeedViewModel,
    val userId: String, val token: String, val listner: OnItemSelectedInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        when (viewType) {
            ListObject.TYPE_ADD_FRIENDS -> {
                val binding = DataBindingUtil.inflate<LayoutAddFriendsBinding>(
                    inflater,
                    R.layout.layout_add_friends,
                    parent,
                    false
                )
                viewHolder = AddFriendsViewHolder(binding) // view holder for normal items

            }
            ListObject.TYPE_FRIENDS -> {
                val binding = DataBindingUtil.inflate<LayoutInitialFriendsBinding>(
                    inflater,
                    R.layout.layout_initial_friends,
                    parent,
                    false
                )
                viewHolder = FriendsViewHolder(binding) // view holder for normal items
            }

        }

        return viewHolder!!
    }

    inner class AddFriendsViewHolder(var binding: LayoutAddFriendsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData() {
            binding.tvAddFriends.setOnClickListener {
                share(context, "Download Sparkk App")
            }
        }
    }

    inner class FriendsViewHolder(var binding: LayoutInitialFriendsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            item: PopularProfileResponse.PopularProfile,
            position: Int,
            itemInterface: OnItemSelectedInterface?
        ) {
            binding.tvUserName.text = item.username
            binding.tvFullName.text = item.name
            Glide.with(context).load(list[position].profilePic)
                .placeholder(R.color.gray_border)
                .error(R.color.gray_border).into(binding.imgProfilePic)
            if (!list[position].postArr.isNullOrEmpty()) {
                val postArr = item.postArr
                Glide.with(context).load(postArr!![0].postPic)
                    .placeholder(R.color.gray_border)
                    .error(R.color.gray_border).into(binding.imgPostOne)
                if (postArr.size > 1)
                    Glide.with(context).load(postArr[1].postPic)
                        .placeholder(R.color.gray_border)
                        .error(R.color.gray_border).into(binding.imgPostTwo)
                if (postArr.size > 2)
                    Glide.with(context).load(postArr[2].postPic)
                        .placeholder(R.color.gray_border)
                        .error(R.color.gray_border).into(binding.imgPostThree)
            } else {
                binding.imgPostOne.visibility = View.GONE
                binding.imgPostTwo.visibility = View.GONE
                binding.imgPostThree.visibility = View.GONE
            }
            binding.imgClose.setOnClickListener {
                list.removeAt(position)
                notifyDataSetChanged()
                listner.onItemSelected(position)
            }
            binding.tvFollow.setOnClickListener {
                if (!binding.tvFollow.isSelected) {
                    viewModel.followUnfollowApi(token, userId, item.userId)
                    binding.tvFollow.isSelected = true
                    binding.tvFollow.text = context.getString(R.string.followed)
                } else {
                    viewModel.followUnfollowApi(token, userId, item.userId, "Unfollow")
                    binding.tvFollow.isSelected = false
                    binding.tvFollow.text = context.getString(R.string.follow)
                }
            }
            binding.imgProfilePic.setOnClickListener {
                val ins = Intent(context, UsersProfileActivity::class.java)
                ins.putExtra(IntentConstant.PROFILE_ID, item.userId)
                context.startActivity(ins)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ListObject.TYPE_ADD_FRIENDS -> {
                val viewHolder = holder as AddFriendsViewHolder
                viewHolder.bindData()
            }
            ListObject.TYPE_FRIENDS -> {
                val viewHolder = holder as FriendsViewHolder
                viewHolder.bindData(list[position], position, listner)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].type
    }

}
