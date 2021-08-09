package com.app.spark.fragment.chat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.app.spark.R
import com.app.spark.activity.search.SearchActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.FragmentChatBinding
import com.app.spark.fragment.chat.viewpager.ChatsViewPager
import com.app.spark.utils.BaseFragment
import com.app.spark.utils.SharedPrefrencesManager


class ChatFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentChatBinding
    private lateinit var pagerAdaptor: ChatsViewPager


    // private lateinit var viewModel: FeedViewModel
    lateinit var pref: SharedPrefrencesManager
    var token: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        //   viewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(FeedViewModel::class.java)
        return binding.root
    }


    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = SharedPrefrencesManager.getInstance(requireContext())
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")
        setClickListener()
        setViewPager()
        binding.tvPersonal.performClick()
    }

    private fun setClickListener() {
        binding.apply {
        imgAddPost.setOnClickListener(this@ChatFragment)
        imgSearch.setOnClickListener(this@ChatFragment)
        tvPersonal.setOnClickListener(this@ChatFragment)
        tvProfessional.setOnClickListener(this@ChatFragment)
        tvPublic.setOnClickListener(this@ChatFragment)
        imgGroup.setOnClickListener(this@ChatFragment)
        }
    }

    private fun setViewPager() {
        pagerAdaptor = ChatsViewPager(requireContext(), childFragmentManager, 4)
        binding.vpViewPagger.adapter = pagerAdaptor
        binding.vpViewPagger.offscreenPageLimit = 0
        binding.vpViewPagger.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.viewBg.setBackgroundResource(R.drawable.bg_chat_green_card)
                        setTabSelected(
                            isPersonal = true,
                            isProfessional = false,
                            isPublic = false,
                            isGroup = false
                        )
                    }
                    1 -> {
                        binding.viewBg.setBackgroundResource(R.drawable.bg_chat_blue_card)
                        setTabSelected(
                            isPersonal = false,
                            isProfessional = true,
                            isPublic = false,
                            isGroup = false
                        )
                    }
                    2 -> {
                        binding.viewBg.setBackgroundResource(R.drawable.bg_chat_purple_card)
                        setTabSelected(
                            isPersonal = false,
                            isProfessional = false,
                            isPublic = true,
                            isGroup = false
                        )
                    }
                    3 -> {
                        binding.viewBg.setBackgroundResource(R.drawable.bg_chat_yellow_card)
                        setTabSelected(
                            isPersonal = false,
                            isProfessional = false,
                            isPublic = false,
                            isGroup = true
                        )
                    }
                }

            }

        })
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.tvPersonal -> {
                binding.viewBg.setBackgroundResource(R.drawable.bg_chat_green_card)
                setTabSelected(
                    isPersonal = true,
                    isProfessional = false,
                    isPublic = false,
                    isGroup = false
                )
                setFragments(0)
            }
            binding.tvProfessional -> {
                binding.viewBg.setBackgroundResource(R.drawable.bg_chat_blue_card)
                setTabSelected(
                    isPersonal = false,
                    isProfessional = true,
                    isPublic = false,
                    isGroup = false
                )
                setFragments(1)

            }
            binding.tvPublic -> {
                binding.viewBg.setBackgroundResource(R.drawable.bg_chat_purple_card)
                setTabSelected(
                    isPersonal = false,
                    isProfessional = false,
                    isPublic = true,
                    isGroup = false
                )
                setFragments(2)

            }
            binding.imgGroup -> {
                binding.viewBg.setBackgroundResource(R.drawable.bg_chat_yellow_card)
                setTabSelected(
                    isPersonal = false,
                    isProfessional = false,
                    isPublic = false,
                    isGroup = true
                )
                setFragments(3)

            }
            binding.imgAddPost -> {
                mainActivityInterface?.onAddPost()
            }
            binding.imgSearch -> {
                startActivity(
                    Intent(requireContext(), SearchActivity::class.java).putExtra(
                        IntentConstant.FROM_CHAT,
                        true
                    )
                )
            }
        }
    }

    private fun setTabSelected(
        isPersonal: Boolean,
        isProfessional: Boolean,
        isPublic: Boolean,
        isGroup: Boolean
    )
    {
        binding.apply {

            tvPersonal.isSelected = isPersonal
            tvProfessional.isSelected = isProfessional
            tvPublic.isSelected = isPublic
            imgGroup.isSelected = isGroup

        }

    }

    private fun setFragments(pos: Int) {
        binding.vpViewPagger.currentItem = pos
    }
}