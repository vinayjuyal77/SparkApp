package com.app.spark.activity.followers

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.app.spark.R
import com.app.spark.activity.UsersProfileActivity
import com.app.spark.activity.main.MainActivity
import com.app.spark.adapter.FollowingAdaptor
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.FragmentFollowersFollowingBinding
import com.app.spark.fragment.chat.viewpager.ChatsViewPager
import com.app.spark.models.ImportantDataResult
import com.app.spark.models.ResultFollowing
import com.app.spark.utils.*
import com.app.spark.viewmodel.FollowingFollowersViewModel
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson


class ActivityFollowerFollowing : BaseActivity(), FollowingAdaptor.RemoveFollower {
    private var type: String = "followers"
    private var offSet: Int = 0
    private var connectionType = ""
    private lateinit var binding: FragmentFollowersFollowingBinding
    private lateinit var viewModel: FollowingFollowersViewModel
    lateinit var pref: SharedPrefrencesManager
    private var token: String? = null
    private var userId: String? = null
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var adapter: FollowingAdaptor
    private var followingList = ArrayList<ResultFollowing>()
    private var loginDetails: ImportantDataResult? = null

    private var loading = true
    private var pastVisiblesItems = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0

    private lateinit var followersViewPager :  FollowersViewPager

    private  lateinit var fragmentManager: FragmentManager
  var profile_id : String = ""

    var a : Int = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_followers_following)
        binding.following = this
        pref = SharedPrefrencesManager.getInstance(this)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                FollowingFollowersViewModel::class.java
            )
        loginDetails = Gson().fromJson(
            pref.getString(PrefConstant.LOGIN_RESPONSE, ""),
            ImportantDataResult::class.java
        )
        if (loginDetails != null)
            binding.tvGallery.text = "@" + loginDetails!!.username

        viewModel.setLoginUserID(loginDetails?.user_id!!)
        onTabSelection()
        inItAdaptor()
        observer()
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")
        userId = pref.getString(PrefConstant.USER_ID, "")
        if (intent.hasExtra(IntentConstant.FOLLOW_LIST)) {
            if (intent.getIntExtra(IntentConstant.FOLLOW_LIST, 0) == 1) {
                type = "followers"
                offSet = 0
                binding.tabLayout.getTabAt(0)?.select()
                onConnectionTypeSelection(3)
            } else {
                binding.tabLayout.getTabAt(1)?.select()
                offSet = 0
                type = "following"
                onConnectionTypeSelection(3)
            }
        } else {
            onConnectionTypeSelection(3)
            //getFoLLowingFollow()
        }
        recycleViewScrollListner()

        profile_id = intent.getStringExtra(IntentConstant.PROFILE_ID).toString()

        setViewPager()




        binding.tvGallery.setOnClickListener {

            if(a==0)
            {
                binding.recycleViewFollowing.visibility =View.VISIBLE
                binding.vpViewPagger.visibility =View.GONE
                a=1
            }
            else{

                binding.recycleViewFollowing.visibility =View.GONE
                binding.vpViewPagger.visibility =View.VISIBLE
                a=0
            }
        }



    }

    override fun onResume() {
        super.onResume()
        clearPrevPagiData()
    }



    private fun setViewPager() {
        followersViewPager = FollowersViewPager(this@ActivityFollowerFollowing, supportFragmentManager, 3, profile_id,  followingList)
        binding.vpViewPagger.adapter = followersViewPager
        binding.vpViewPagger.adapter = followersViewPager
        binding.vpViewPagger.offscreenPageLimit = 0
        binding.vpViewPagger.currentItem  = 2
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
                        connectionType = "personal"
                        binding.llConnectionType.background =
                                ContextCompat.getDrawable(this@ActivityFollowerFollowing, R.drawable.white_button_line)
                        binding.tvPersonal.background =
                                ContextCompat.getDrawable(this@ActivityFollowerFollowing, R.drawable.bg_follow_personal)
                        binding.tvPersonal.setTextColor(
                                ContextCompat.getColor(
                                        this@ActivityFollowerFollowing,
                                        R.color.white
                                )
                        )
                        binding.tvProfessional.background =
                                ContextCompat.getDrawable(this@ActivityFollowerFollowing, R.drawable.selector_follow_connection_type)
                        binding.tvProfessional.setTextColor(
                                ContextCompat.getColor(
                                        this@ActivityFollowerFollowing,
                                        R.color.white
                                )
                        )
                        binding.tvPublic.background =
                                ContextCompat.getDrawable(this@ActivityFollowerFollowing, R.drawable.selector_follow_connection_type)
                        binding.tvPublic.setTextColor(
                                ContextCompat.getColor(
                                        this@ActivityFollowerFollowing,
                                        R.color.white
                                )
                        )
                        setTabSelected(
                                isPersonal = true,
                                isProfessional = false,
                                isPublic = false,
                                isGroup = false
                        )
                    }
                    1 -> {
                        connectionType = "professional"
                        binding.llConnectionType.background =
                                ContextCompat.getDrawable(this@ActivityFollowerFollowing, R.drawable.white_button_line)
                        binding.tvPersonal.background =
                                ContextCompat.getDrawable(this@ActivityFollowerFollowing, R.drawable.selector_follow_connection_type)
                        binding.tvPersonal.setTextColor(
                                ContextCompat.getColor(
                                        this@ActivityFollowerFollowing,
                                        R.color.white
                                )
                        )
                        binding.tvProfessional.background =
                                ContextCompat.getDrawable(this@ActivityFollowerFollowing, R.drawable.bg_follow_professional)
                        binding.tvProfessional.setTextColor(
                                ContextCompat.getColor(
                                        this@ActivityFollowerFollowing,
                                        R.color.white
                                )
                        )
                        binding.tvPublic.background =
                                ContextCompat.getDrawable(this@ActivityFollowerFollowing, R.drawable.selector_follow_connection_type)
                        binding.tvPublic.setTextColor(
                                ContextCompat.getColor(
                                        this@ActivityFollowerFollowing,
                                        R.color.white
                                )
                        )
                        setTabSelected(
                                isPersonal = false,
                                isProfessional = true,
                                isPublic = false,
                                isGroup = false
                        )
                    }
                    2 -> {
                        connectionType = "public"
                        binding.llConnectionType.background =
                                ContextCompat.getDrawable(this@ActivityFollowerFollowing, R.drawable.white_button_line)
                        binding.tvPersonal.background =
                                ContextCompat.getDrawable(this@ActivityFollowerFollowing, R.drawable.selector_follow_connection_type)
                        binding.tvPersonal.setTextColor(
                                ContextCompat.getColor(
                                        this@ActivityFollowerFollowing,
                                        R.color.white
                                )
                        )
                        binding.tvProfessional.background =
                                ContextCompat.getDrawable(this@ActivityFollowerFollowing, R.drawable.selector_follow_connection_type)
                        binding.tvProfessional.setTextColor(
                                ContextCompat.getColor(
                                        this@ActivityFollowerFollowing,
                                        R.color.white
                                )
                        )
                        binding.tvPublic.background =
                                ContextCompat.getDrawable(this@ActivityFollowerFollowing, R.drawable.bg_yellow_card_40)
                        binding.tvPublic.setTextColor(
                                ContextCompat.getColor(
                                        this@ActivityFollowerFollowing,
                                        R.color.white
                                )
                        )
                        setTabSelected(
                                isPersonal = false,
                                isProfessional = false,
                                isPublic = true,
                                isGroup = false
                        )
                    }

                }

            }

        })
    }


    private fun setTabSelected(
            isPersonal: Boolean,
            isProfessional: Boolean,
            isPublic: Boolean,
            isGroup: Boolean
    )
    {
        binding.tvPersonal.isSelected = isPersonal
        binding.tvProfessional.isSelected = isProfessional
        binding.tvPublic.isSelected = isPublic

    }

    private fun observer() {
        viewModel.response.observe(this, Observer {
            if (followingList.isNotEmpty()) {
                followingList.clear()
            }
            for (i in it.result.indices) {
                it.result[i].myType = type
                followingList.add(it.result[i])
            }
            // followingList.addAll(it.result)
            adapter.notifyDataSetChanged()
            if (followingList.isNotEmpty()) {
                emptyRecycleCheck(1)
            } else {
                emptyRecycleCheck(2)
            }
            offSet = if (it.result.isNotEmpty()) {
                +10
            } else {
                offSet
            }
        })


        viewModel.errString.observe(this, Observer { err: String ->
            //hideView(binding.progressBar)
            showToastShort(this, err)
        })

        viewModel.errRes.observe(this, { err: Int ->
            //  binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }


    private fun inItAdaptor() {
        adapter = FollowingAdaptor(this, followingList, this)
        layoutManager = GridLayoutManager(this,2)
        binding.recycleViewFollowing.layoutManager = layoutManager
        binding.recycleViewFollowing.itemAnimator = DefaultItemAnimator()
        binding.recycleViewFollowing.adapter = adapter
    }


    private fun onTabSelection() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.vpViewPagger.visibility = View.VISIBLE
                        binding.recycleViewFollowing.visibility = View.GONE
                        binding.llConnectionType.visibility = View.VISIBLE
                        type = "followers"
                        offSet = 0
                        onConnectionTypeSelection(3)
                    }
                    1 -> {
                        binding.vpViewPagger.visibility = View.GONE
                        binding.recycleViewFollowing.visibility = View.VISIBLE
                        binding.llConnectionType.visibility = View.GONE
                        connectionType = " "
                        binding.llConnectionType.background =
                            ContextCompat.getDrawable(
                                this@ActivityFollowerFollowing,
                                R.drawable.bg_follow_personal
                            )
                        binding.tvPersonal.background =
                            ContextCompat.getDrawable(
                                this@ActivityFollowerFollowing,
                                R.drawable.selector_follow_connection_type
                            )
                        binding.tvPersonal.setTextColor(
                            ContextCompat.getColor(
                                this@ActivityFollowerFollowing,
                                R.color.white
                            )
                        )
                        binding.tvProfessional.background =
                            ContextCompat.getDrawable(
                                this@ActivityFollowerFollowing,
                                R.drawable.selector_follow_connection_type
                            )
                        binding.tvProfessional.setTextColor(
                            ContextCompat.getColor(
                                this@ActivityFollowerFollowing,
                                R.color.white
                            )
                        )
                        binding.tvPublic.background =
                            ContextCompat.getDrawable(
                                this@ActivityFollowerFollowing,
                                R.drawable.selector_follow_connection_type
                            )
                        binding.tvPublic.setTextColor(
                            ContextCompat.getColor(
                                this@ActivityFollowerFollowing,
                                R.color.white
                            )
                        )
                        type = "following"
                        clearPrevPagiData()
                    }
                }
            }

        })
    }

    private fun clearPrevPagiData() {
        if (intent.hasExtra(IntentConstant.PROFILE_ID)) {
            if (!intent.getStringExtra(IntentConstant.PROFILE_ID).toString()
                    .equals(loginDetails?.user_id, false)
            ) {
                binding.llConnectionType.visibility = View.GONE
            }
        }
        pastVisiblesItems = 0
        visibleItemCount = 0
        totalItemCount = 0
        loading = false
        if (followingList.isNotEmpty()) {
            followingList.clear()
        }
        adapter.notifyDataSetChanged()
        getFoLLowingFollow()
    }

    private fun recycleViewScrollListner() {
        binding.recycleViewFollowing.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = layoutManager.childCount
                    totalItemCount = layoutManager.itemCount
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()
                    Log.e(
                        "PAGIGNATION",
                        "onScrolled: $loading$visibleItemCount$pastVisiblesItems$totalItemCount"
                    )
                    if (loading) {
                        if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                            loading = false
                            // Log.v("...", "Last Item Wow !")
                            // Do pagination.. i.e. fetch new data
                            getFoLLowingFollow()
                            loading = true
                        }
                    }
                }
            }
        })
    }

    fun onConnectionTypeSelection(pos: Int) {
        when (pos) {
            1 -> {
                binding.vpViewPagger.currentItem  = 0
                connectionType = "personal"
                binding.llConnectionType.background =
                    ContextCompat.getDrawable(this, R.drawable.white_button_line)
                binding.tvPersonal.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_follow_personal)
                binding.tvPersonal.setTextColor(
                    ContextCompat.getColor(
                        this@ActivityFollowerFollowing,
                        R.color.white
                    )
                )
                binding.tvProfessional.background =
                    ContextCompat.getDrawable(this, R.drawable.selector_follow_connection_type)
                binding.tvProfessional.setTextColor(
                    ContextCompat.getColor(
                        this@ActivityFollowerFollowing,
                        R.color.white
                    )
                )
                binding.tvPublic.background =
                    ContextCompat.getDrawable(this, R.drawable.selector_follow_connection_type)
                binding.tvPublic.setTextColor(
                    ContextCompat.getColor(
                        this@ActivityFollowerFollowing,
                        R.color.white
                    )
                )
                clearPrevPagiData()
            }
            2 -> {

                binding.vpViewPagger.currentItem  = 1
                connectionType = "professional"
                binding.llConnectionType.background =
                    ContextCompat.getDrawable(this, R.drawable.white_button_line)

                binding.tvPersonal.background =
                    ContextCompat.getDrawable(this, R.drawable.selector_follow_connection_type)
                binding.tvPersonal.setTextColor(
                    ContextCompat.getColor(
                        this@ActivityFollowerFollowing,
                        R.color.white
                    )
                )
                binding.tvProfessional.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_follow_professional)
                binding.tvProfessional.setTextColor(
                    ContextCompat.getColor(
                        this@ActivityFollowerFollowing,
                        R.color.white
                    )
                )

                binding.tvPublic.background =
                    ContextCompat.getDrawable(this, R.drawable.selector_follow_connection_type)
                binding.tvPublic.setTextColor(
                    ContextCompat.getColor(
                        this@ActivityFollowerFollowing,
                        R.color.white
                    )
                )
                clearPrevPagiData()
            }
            3 -> {
                binding.vpViewPagger.currentItem  = 2
                connectionType = "public"
                binding.llConnectionType.background =
                    ContextCompat.getDrawable(this, R.drawable.white_button_line)
                binding.tvPersonal.background =
                    ContextCompat.getDrawable(this, R.drawable.selector_follow_connection_type)
                binding.tvPersonal.setTextColor(
                    ContextCompat.getColor(
                        this@ActivityFollowerFollowing,
                        R.color.white
                    )
                )
                binding.tvProfessional.background =
                    ContextCompat.getDrawable(this, R.drawable.selector_follow_connection_type)
                binding.tvProfessional.setTextColor(
                    ContextCompat.getColor(
                        this@ActivityFollowerFollowing,
                        R.color.white
                    )
                )
                binding.tvPublic.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_yellow_card_40)
                binding.tvPublic.setTextColor(
                    ContextCompat.getColor(
                        this@ActivityFollowerFollowing,
                        R.color.white
                    )
                )
                clearPrevPagiData()
            }
        }
    }

    private fun emptyRecycleCheck(pos: Int) {
        if (pos == 1) {
            binding.tvNamaste.visibility = View.GONE
            binding.tvWeWelcomeYou.visibility = View.GONE
            binding.tvFollowPeoplePages.visibility = View.GONE
       //     binding.recycleViewFollowing.visibility = View.VISIBLE
            
        } else {
            binding.tvNamaste.visibility = View.VISIBLE
            binding.tvWeWelcomeYou.visibility = View.VISIBLE
            binding.tvFollowPeoplePages.visibility = View.VISIBLE
            binding.recycleViewFollowing.visibility = View.GONE
        }
    }

    private fun getFoLLowingFollow() {
        if (isNetworkAvailable(this)) {
            token?.let { it1 ->
                intent.getStringExtra(IntentConstant.PROFILE_ID)?.let {
                    viewModel.getFollowingAndFollowers(
                        token = it1,
                        type = type,
                        followingGroup = connectionType,
                        offset = offSet.toString(),
                        userID = it
                    )
                }
            }
        } else {
            emptyRecycleCheck(2)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun removeFollower(model: ResultFollowing) {
        token?.let {
            userId?.let { it1 ->
                viewModel.followUnfollowApi(
                    it,
                    it1,
                    model.userId,
                    "Unfollow"
                )
            }
        }
    }

    override fun profileDetails(model: ResultFollowing) {
        if (model != null) {
            var userIds = pref.getString(PrefConstant.USER_ID, "")
            if (userIds!!.trim().isNotEmpty() && !model.userId.equals(userIds, true)) {
                val ins = Intent(this@ActivityFollowerFollowing, UsersProfileActivity::class.java)
                ins.putExtra(IntentConstant.PROFILE_ID, model.userId)
                startActivity(ins)
            }
        }
    }

    override fun addFollower(model: ResultFollowing) {
        token?.let {
            userId?.let { it1 ->
                viewModel.followUnfollowApi(
                    it,
                    it1,
                    model.userId,
                    "Follow"
                )
            }
        }
    }
}