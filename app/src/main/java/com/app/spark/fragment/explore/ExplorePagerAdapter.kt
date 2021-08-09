package com.app.spark.fragment.explore

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.app.spark.R
import com.app.spark.databinding.ItemActivityConnectBinding
import com.app.spark.utils.showToastShort
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


class ExplorePagerAdapter(
    private val context: Context
) : PagerAdapter() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return 100
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val binding = DataBindingUtil.inflate<ItemActivityConnectBinding>(
            inflater,
            R.layout.item_activity_connect,
            container, //king
            false
        )
        when {
            position % 4 == 0 -> {


                binding.clParent.setBackgroundResource(R.drawable.bg_red_card)
                binding.imgActivity.setImageResource(R.drawable.ic_activity_red_heart)
                binding.tvTitle.text = context.getString(R.string.date)
                binding.tvInfo.text = context.getString(R.string.lets_find_something_special)
                binding.tvConnect.setTextColor(ContextCompat.getColor(context, R.color.red_text_color))

            }
            position % 4 == 1 -> {

                binding.clParent.setBackgroundResource(R.drawable.bg_green_card)
                binding.imgActivity.setImageResource(R.drawable.ic_activity_hands_helping)
                binding.tvTitle.text = context.getString(R.string.friend)
                binding.tvInfo.text = context.getString(R.string.lets_make_friends)
                binding.tvConnect.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.green_text_color
                    )
                )
            }
            position % 4 == 2 -> {

                binding.clParent.setBackgroundResource(R.drawable.bg_blue_card)
                binding.imgActivity.setImageResource(R.drawable.ic_activity_tie)
                binding.tvTitle.text = context.getString(R.string.professional)
                binding.tvInfo.text = context.getString(R.string.lets_connect_pro)
                binding.tvConnect.setTextColor(ContextCompat.getColor(context, R.color.blue_text_color))
            }
            else -> {
                binding.clParent.setBackgroundResource(R.drawable.bg_yellow_card)
                binding.imgActivity.setImageResource(R.drawable.ic_activity_help)
                binding.tvTitle.text = context.getString(R.string.counsellor)
                binding.tvInfo.text = context.getString(R.string.let_us_help_you)
                binding.tvConnect.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.yellow_text_color
                    )
                )
            }
        }
        binding.tvConnect.setOnClickListener {
           //showToastShort(context,context.getString(R.string.coming_soon))
            when {
                position % 4 == 0 -> {
                    onItemClick?.invoke(context.getString(R.string.date))
                }
                position % 4 == 1 -> {
                    onItemClick?.invoke(context.getString(R.string.friend))
                }
                position % 4 == 2 -> {
                    onItemClick?.invoke(context.getString(R.string.professional))
                }
                position % 4 == 3 -> {
                    onItemClick?.invoke(context.getString(R.string.counsellor))
                }
            }

        }
        container.addView(binding.root)
        return binding.root

    }
    var onItemClick: ((String) -> Unit)? = null

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }

    private fun imageSet(img:ImageView,drawable: Drawable){
        Glide.with(context)
            .load(drawable)
            .apply(
                RequestOptions.placeholderOf(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .override(100,100))
            .into(img);
    }
}
