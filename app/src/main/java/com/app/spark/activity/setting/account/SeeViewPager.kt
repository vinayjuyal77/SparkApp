package com.app.spark.activity.setting.account

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.app.spark.activity.setting.account.fragment.CommentFragment
import com.app.spark.activity.setting.account.fragment.LikeFragment
import com.app.spark.activity.setting.account.fragment.SaveFragment
import com.app.spark.activity.setting.account.fragment.SharesFragment
import com.app.spark.fragment.profile.fragment.ProfileFlickFragment
import com.app.spark.fragment.profile.fragment.ProfilePersonalFragment
import com.app.spark.fragment.profile.fragment.ProfileProfessionalFragment
import com.app.spark.fragment.profile.fragment.ProfilePublicFragment

class SeeViewPager(
    private val myContext: Context,
    fm: FragmentManager,

    var profile_id: String
) : FragmentPagerAdapter(
    fm,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
)


{

    var fragments: ArrayList<Fragment> = ArrayList()


    fun initPagerFragments(mRole: Int, profilePic: String?) {
        fragments.clear()
        fragments.add(LikeFragment.newInstance(profile_id, ""))
        fragments.add(CommentFragment.newInstance(profile_id, ""))
        fragments.add(SharesFragment.newInstance(profile_id, ""))
        fragments.add(SaveFragment.newInstance(profile_id, ""))
        notifyDataSetChanged()
    }



    fun initPagerFragments2(mRole: Int, profilePic: String?) {
        fragments.clear()
        fragments.add(ProfilePublicFragment.newInstance(profile_id, ""))
        fragments.add(ProfileFlickFragment.newInstance(profile_id, ""))
        notifyDataSetChanged()
    }


    	fun frgament_list(fragment: Fragment)
	{
		fragments.add(fragment)

	}


    override fun getCount(): Int {

        return fragments.size
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }


}