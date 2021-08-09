package com.app.spark.fragment.chat.viewpager

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.app.spark.fragment.chat.GroupChatFragment
import com.app.spark.fragment.chat.PersonalChatFragment
import com.app.spark.fragment.chat.ProfessionalChatFragment
import com.app.spark.fragment.chat.PublicChatFragment

class ChatsViewPager(private val myContext: Context, fm: FragmentManager, private var totalTabs: Int) : FragmentPagerAdapter(fm,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    // this is for fragment tabs
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                PersonalChatFragment()
            }
            1 -> {
                ProfessionalChatFragment()
            }
            2 -> {
                PublicChatFragment()
            }
            3 -> {
                GroupChatFragment()
            }
            else -> PersonalChatFragment()
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}