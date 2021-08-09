package com.app.spark.activity.story.customview

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.app.spark.activity.story.data.StoryUser
import com.app.spark.activity.story.dataClass.StoryResult
import com.app.spark.activity.story.screen.StoryDisplayFragment


//class StoryPagerAdapter constructor(fragmentManager: FragmentManager, private val storyList: ArrayList<StoryUser>)
class StoryPagerAdapter constructor(fragmentManager: FragmentManager, private val storyList: ArrayList<StoryResult>)
    : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = StoryDisplayFragment.newInstance(position, storyList[position])

    override fun getCount(): Int {
        return storyList.size
    }

    fun findFragmentByPosition(viewPager: ViewPager, position: Int): Fragment? {
        try {
            val f = instantiateItem(viewPager, position)
            return f as? Fragment
        } finally {
            finishUpdate(viewPager)
        }
    }
}