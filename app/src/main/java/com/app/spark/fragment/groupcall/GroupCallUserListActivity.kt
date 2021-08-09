package com.app.spark.fragment.groupcall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.app.spark.R
import com.app.spark.databinding.ActivityGroupCallUserListBinding
import com.app.spark.databinding.ActivityRoomBinding
import com.app.spark.fragment.profile.ProfileViewPager
import com.google.android.material.tabs.TabLayout

class GroupCallUserListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupCallUserListBinding
    private lateinit var profileViewPager: GroupCallViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_group_call_user_list)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_group_call_user_list)

        setViewPager()
        tabSelectedListener()


        binding.cross.setOnClickListener {

            finish()
        }
    }

    private fun setViewPager() {
        profileViewPager = GroupCallViewPager(this@GroupCallUserListActivity, supportFragmentManager, "loginId")
        binding.viewpagerGroupCall.adapter = profileViewPager
        binding.viewpagerGroupCall.adapter = profileViewPager
        binding.viewpagerGroupCall.offscreenPageLimit = 0
        binding.viewpagerGroupCall.currentItem = 0


            profileViewPager.initPagerFragments(0, "")




        binding.viewpagerGroupCall.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {


            }

            override fun onPageSelected(position: Int) {

                    when (position) {

                        0 -> {
                            binding.tabLayout.getTabAt(0)?.select()
                        }
                        1 -> {
                            binding.tabLayout.getTabAt(1)?.select()
                        }


                    }



            }

        })
    }

    private fun tabSelectedListener() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> //setPostFeeds(globalProfileResponse?.result?.postArr)
                        binding.viewpagerGroupCall.currentItem = 0
                    1 -> //setPostFeeds(globalProfileResponse?.result?.personalArr)
                        binding.viewpagerGroupCall.currentItem = 1

                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

}