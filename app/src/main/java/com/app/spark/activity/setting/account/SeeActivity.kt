package com.app.spark.activity.setting.account

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.app.spark.R
import com.app.spark.databinding.ActivitySavedFlickPostBinding
import com.app.spark.databinding.ActivitySeeBinding
import com.app.spark.fragment.profile.ProfileViewPager

class SeeActivity : AppCompatActivity() {


    lateinit var  binding : ActivitySeeBinding
    lateinit var viewModel: SeeViewModel
   lateinit var seeViewPager: SeeViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_see)

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                SeeViewModel::class.java
            )
        binding.viewmodel = viewModel

        setViewPager()


    }

        private fun setViewPager() {
            seeViewPager = SeeViewPager(this, supportFragmentManager, "")
        binding.viewpager.adapter = seeViewPager
            binding.viewpager.adapter  = seeViewPager
            binding.viewpager.offscreenPageLimit = 0

            binding.viewpager.currentItem = 0

            seeViewPager.initPagerFragments(0, "")



        binding.viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {


            }

            override fun onPageSelected(position: Int) {

                    when (position) {

                        0 -> {
                            binding.likeTxt.background = ContextCompat.getDrawable(this@SeeActivity,R.drawable.bg_white_white_storke)
                            binding.likeTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.colorBlack));
                            binding.commentTxt.setBackgroundResource(0)
                            binding.shareTxt.setBackgroundResource(0)
                            binding.saveTxt.setBackgroundResource(0)
                            binding.commentTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.white));
                            binding.shareTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.white));
                            binding.saveTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.white));


                            //   binding.flickTxt.setTextColor(R.color.white)

                        }
                        1 -> {
                            binding.commentTxt.background = ContextCompat.getDrawable(this@SeeActivity,R.drawable.bg_white_white_storke)
                            binding.commentTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.colorBlack));
                            binding.likeTxt.setBackgroundResource(0)
                            binding.shareTxt.setBackgroundResource(0)
                            binding.saveTxt.setBackgroundResource(0)
                            binding.likeTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.white));
                            binding.shareTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.white));
                            binding.saveTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.white));
                        }
                        2 -> {
                            binding.shareTxt.background = ContextCompat.getDrawable(this@SeeActivity,R.drawable.bg_white_white_storke)
                            binding.shareTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.colorBlack));
                            binding.commentTxt.setBackgroundResource(0)
                            binding.likeTxt.setBackgroundResource(0)
                            binding.saveTxt.setBackgroundResource(0)
                            binding.commentTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.white));
                            binding.likeTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.white));
                            binding.saveTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.white));

                        }
                        3 -> {
                            binding.saveTxt.background = ContextCompat.getDrawable(this@SeeActivity,R.drawable.bg_white_white_storke)
                            binding.saveTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.colorBlack));
                            binding.commentTxt.setBackgroundResource(0)
                            binding.shareTxt.setBackgroundResource(0)
                            binding.likeTxt.setBackgroundResource(0)
                            binding.commentTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.white));
                            binding.shareTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.white));
                            binding.likeTxt.setTextColor(ContextCompat.getColor(this@SeeActivity, R.color.white));

                        }

                    }

                }



        })
    }
}