package com.app.spark.fragment.profile

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.app.spark.fragment.profile.fragment.ProfileFlickFragment
import com.app.spark.fragment.profile.fragment.ProfilePersonalFragment
import com.app.spark.fragment.profile.fragment.ProfileProfessionalFragment
import com.app.spark.fragment.profile.fragment.ProfilePublicFragment

class ProfileViewPager(
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
        fragments.add(ProfilePublicFragment.newInstance(profile_id, ""))
        fragments.add(ProfilePersonalFragment.newInstance(profile_id, ""))
        fragments.add(ProfileProfessionalFragment.newInstance(profile_id, ""))
        fragments.add(ProfileFlickFragment.newInstance(profile_id, ""))
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