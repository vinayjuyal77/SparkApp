package com.app.spark.activity.followers

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.app.spark.activity.followers.fragment.PersonalFragment
import com.app.spark.activity.followers.fragment.ProfessionalFragment
import com.app.spark.activity.followers.fragment.PublicFragment

import com.app.spark.models.ResultFollowing

class FollowersViewPager(private val myContext: Context, fm: FragmentManager, private var totalTabs: Int, var profile_id : String ,var listt  : ArrayList<ResultFollowing>) : FragmentPagerAdapter(fm,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    // this is for fragment tabs
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                PersonalFragment.newInstance(profile_id)
            }
            1 -> {
                ProfessionalFragment.newInstance(profile_id)
            }
            2 -> {
                PublicFragment.newInstance(profile_id)
            }
            else -> PersonalFragment.newInstance(profile_id)
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}