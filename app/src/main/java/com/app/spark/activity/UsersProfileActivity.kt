package com.app.spark.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.Constraints
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.app.spark.R
import com.app.spark.activity.chatroom.ChatRoomActivity
import com.app.spark.activity.edit_bio.EditBioActivity
import com.app.spark.activity.followers.ActivityFollowerFollowing
import com.app.spark.activity.personal_info.ActivityPersonalInformation
import com.app.spark.activity.play_information.ActivityPlayInformation
import com.app.spark.activity.professional_info.ActivityProfessionalInformation
import com.app.spark.bottomSheet.profilemenu.ProfileOptionBottomFragment
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.database.ConnectedAppDatabase
import com.app.spark.databinding.ActivityUsersProfileBinding
import com.app.spark.databinding.FragmentProfile2Binding
import com.app.spark.fragment.profile.ProfileFeedAdapter
import com.app.spark.fragment.profile.ProfileFlickAdapter
import com.app.spark.fragment.profile.ProfileViewPager
import com.app.spark.interfaces.DrawerListener
import com.app.spark.models.GetFlickResponse
import com.app.spark.models.ProfileGetResponse
import com.app.spark.utils.*
import com.app.spark.viewmodel.ProfileGetViewModel
import com.app.spark.viewmodel.UploadDataViewModel
import com.bumptech.glide.Glide
import com.cancan.Utility.PermissionsUtil
import com.google.android.material.tabs.TabLayout

class UsersProfileActivity : BaseActivity(), View.OnClickListener, ProfileGetViewModel.ProfileListerner{


    private lateinit var binding: ActivityUsersProfileBinding
    private lateinit var viewModel: ProfileGetViewModel
    var globalProfileResponse: ProfileGetResponse? = null
    private lateinit var pref: SharedPrefrencesManager
    private var token: String? = null
    private var loginId: String = ""
    private var userId: String = ""
    private var isFollowers = ""
    private var isProfileOpen = false
    private var isEditProfilePic = false
    private var permissionsUtil: PermissionsUtil? = null
    private lateinit var profileViewPager: ProfileViewPager
    private var connectedAppDatabase: ConnectedAppDatabase? = null
    var dataPasser: DrawerListener? = null
    var username: String = ""
    var profile_id: String = ""
    var count = 0
    var path: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_users_profile)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_users_profile)
        viewModel= ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
            ProfileGetViewModel::class.java)

        permissionsUtil = PermissionsUtil(this)
        binding.rvFeed.addItemDecoration(GridSpacingItemDecoration(3, 15, true, 0))
        pref = SharedPrefrencesManager.getInstance(this)
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")
        userId = pref.getString(PrefConstant.USER_ID, "")!!

        binding.progressBar.visibility = View.VISIBLE
        connectedAppDatabase = ConnectedAppDatabase.getInstance(this)
        



        askPermission()


        observeProfile()
        setClickListener()
        tabSelectedListener()
        if (intent?.getStringExtra(IntentConstant.PROFILE_ID).toString().trim().isNotEmpty()) {
            loginId = intent?.getStringExtra(IntentConstant.PROFILE_ID).toString()
        }
        if (loginId.trim().isNotEmpty()) {
            (binding.tabLayout.getTabAt(1)?.view as LinearLayout).visibility = View.GONE
            (binding.tabLayout.getTabAt(2)?.view as LinearLayout).visibility = View.GONE
            (binding.tabLayout.getTabAt(0)?.view as LinearLayout).visibility = View.VISIBLE
            (binding.tabLayout.getTabAt(3)?.view as LinearLayout).visibility = View.VISIBLE
            binding.imgMenu.visibility = View.VISIBLE
            binding.imgEditSmall.visibility = View.GONE
            binding.imgEdit.visibility = View.GONE
            binding.imgShare.visibility = View.GONE
            binding.imgPersonal.setOnClickListener(this)
            binding.tvPersonal.setOnClickListener(this)
            binding.tvProfessional.setOnClickListener(this)
            binding.imgProfessional.setOnClickListener(this)
            binding.tvPlay.setOnClickListener(this)
            binding.imgPlay.setOnClickListener(this)
            //   binding.back.setOnClickListener(this)

            // binding.back.visibility = View.VISIBLE
            //  binding.menu.visibility = View.GONE

        } else {
            (binding.tabLayout.getTabAt(1)?.view as LinearLayout).visibility = View.VISIBLE
            (binding.tabLayout.getTabAt(2)?.view as LinearLayout).visibility = View.VISIBLE
            (binding.tabLayout.getTabAt(0)?.view as LinearLayout).visibility = View.VISIBLE
            (binding.tabLayout.getTabAt(3)?.view as LinearLayout).visibility = View.VISIBLE
            binding.imgMenu.visibility = View.GONE
            binding.imgEditSmall.visibility = View.GONE
            binding.imgEdit.visibility = View.VISIBLE
            binding.imgShare.visibility = View.VISIBLE
            binding.tvFullName.text = pref.getString(PrefConstant.NAME,"")
            //   binding.tvLogOut.visibility = View.VISIBLE
            binding.imgPersonal.setOnClickListener(this)
            binding.tvPersonal.setOnClickListener(this)
            binding.tvProfessional.setOnClickListener(this)
            binding.imgProfessional.setOnClickListener(this)
            binding.tvPlay.setOnClickListener(this)
            binding.imgPlay.setOnClickListener(this)

            //  binding.menu.visibility = View.VISIBLE
            // binding.back.visibility = View.GONE
        }




        if(isNetworkAvailable(this))
        {
            getProfileData()

        }
        else
        {
         //   viewModel.getLcoalMainData(this, this)
        }

        setViewPager();



        binding.logout.setOnClickListener {

            viewModel?.updateDeviceTokenApi(
                pref?.getString(PrefConstant.ACCESS_TOKEN, ""),
                pref?.getString(PrefConstant.USER_ID, ""),
                ""
            )
            pref?.clear()
            clearAllgoToActivity(this, NewLoginActivity::class.java)

        }


    }


    private fun askPermission() {
        permissionsUtil?.askPermissions(this, PermissionsUtil.CAMERA,
            PermissionsUtil.STORAGE, object : PermissionsUtil.PermissionListener {
                override fun onPermissionResult(isGranted: Boolean) {
                    if (isGranted) {
                        // getAllAudio()
                    } else {
                        askPermission()
                    }
                }

            })
    }

    private fun setViewPager() {
        profileViewPager = ProfileViewPager(this, supportFragmentManager, loginId)
        binding.vpViewPagger.adapter = profileViewPager
        binding.vpViewPagger.adapter = profileViewPager
        binding.vpViewPagger.offscreenPageLimit = 0
        binding.vpViewPagger.currentItem = 0

        if (loginId.trim().isNotEmpty()) {

            profileViewPager.initPagerFragments2(0, "")

        }
        else
        {
            profileViewPager.initPagerFragments(0, "")


        }

        binding.vpViewPagger.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {


            }

            override fun onPageSelected(position: Int) {
                if (loginId.trim().isNotEmpty()) {
                    when (position) {

                        0 -> {
                            binding.tabLayout.getTabAt(0)?.select()
                        }
                        1 -> {
                            binding.tabLayout.getTabAt(3)?.select()
                        }

                    }
                } else {

                    when (position) {

                        0 -> {
                            binding.tabLayout.getTabAt(0)?.select()
                        }
                        1 -> {
                            binding.tabLayout.getTabAt(1)?.select()
                        }
                        2 -> {
                            binding.tabLayout.getTabAt(2)?.select()

                        }
                        3 -> {
                            binding.tabLayout.getTabAt(3)?.select()


                        }

                    }

                }

            }

        })
    }


    private fun getProfileData() {
        if (isNetworkAvailable(this)) {
            var userId = pref.getString(PrefConstant.USER_ID, "")
            if (loginId.trim().isEmpty()) {
                viewModel.getUserProfileData(token!!, userId!!, userId)
            } else {
                viewModel.getUserProfileData(token!!, loginId, userId!!)
            }
        } else {
            showSnackBar(
                binding.root,
                this.resources.getString(R.string.please_check_internet)
            )
        }
    }

    private fun setClickListener() {
        binding.imgEdit.setOnClickListener(this)
        binding.imgEditSmall.setOnClickListener(this)
        binding.imgShare.setOnClickListener(this)
        binding.tvFollow.setOnClickListener(this)
        binding.tvFollowersTitle.setOnClickListener(this)
        binding.tvFollowingTitle.setOnClickListener(this)
        binding.imgMenu.setOnClickListener(this)
        binding.tvFullName.setOnClickListener(this)
        //  binding.rlUserProfile.setOnClickListener(this)
        binding.tvFollowers.setOnClickListener(this)
        binding.tvFollowing.setOnClickListener(this)
        binding.menu.setOnClickListener(this)
        binding.tvMessage.setOnClickListener(this)
        binding.tvPost.setOnClickListener(this)
        binding.tvFlick.setOnClickListener(this)
        //binding.back.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(p0: View?) {
        when (p0) {
            binding.tvPlay -> {
                startActivity(Intent(this@UsersProfileActivity, ActivityPlayInformation::class.java))
            }
            binding.imgPlay -> {
                startActivity(Intent(this@UsersProfileActivity, ActivityPlayInformation::class.java))
            }
            binding.tvProfessional -> {
                startActivity(Intent(this@UsersProfileActivity, ActivityProfessionalInformation::class.java))
            }
            binding.imgProfessional -> {
                startActivity(Intent(this@UsersProfileActivity, ActivityProfessionalInformation::class.java))
            }
            binding.imgPersonal -> {
                startActivity(Intent(this@UsersProfileActivity, ActivityPersonalInformation::class.java))
            }
            binding.tvPersonal -> {
                startActivity(Intent(this@UsersProfileActivity, ActivityPersonalInformation::class.java))
            }
            binding.imgEdit -> {
                startActivityForResult(
                    Intent(this, FixYourProfileActivity::class.java),
                    IntentConstant.REQUEST_CODE
                )
                /*permissionsUtil?.askPermissions(
                    this,
                    PermissionsUtil.STORAGE,
                    PermissionsUtil.CAMERA,
                    object : PermissionsUtil.PermissionListener {
                        override fun onPermissionResult(isGranted: Boolean) {
                            if (isGranted) {
                                ImagePickerUtil.selectImage(
                                    this,
                                    { file: File, s: String ->
                                        isEditProfilePic = true
                                        Glide.with(this).load(file)
                                            .into(binding.imgProfilePic)
                                        viewModel.editBioAdd(token!!, userId, "", file.path)
                                    },
                                    "profile_pic",
                                    true,
                                    false,
                                    false,true
                                )
                            }
                        }

                    })*/

            }
            binding.tvFullName -> {
                isProfileOpen = false
                //    binding.rlProfileDetails.visibility = View.GONE
                val param = Constraints.LayoutParams(
                    Constraints.LayoutParams.MATCH_PARENT,
                    Constraints.LayoutParams.WRAP_CONTENT
                )
                param.verticalBias = 1f
//                param.bottomToBottom = binding.imgProfilePic.id
//                param.topToTop = binding.imgProfilePic.id
//                binding.rlUserProfile.layoutParams = param
                /* val animation: Animation = AnimationUtils.loadAnimation(
                     this,
                     R.anim.slide_top_bottom
                 )
                 binding.rlUserProfile.animation = animation*/
                //     binding.rlUserProfile.layoutParams = param

            }
            /*  binding.rlUserProfile -> {
                  if (isProfileOpen) {
                      isProfileOpen = false
                      binding.rlProfileDetails.visibility = View.GONE
                      binding.constrainntLayout.setBackgroundColor(0)
                      binding.linearLayout.setBackgroundColor(
                          resources.getColor(
                              R.color.bg_profile_black,
                              null
                          )
                      )
                      //   binding.tvFullName.performClick()
                  } else {
                      isProfileOpen = true
  //                    val param = Constraints.LayoutParams(
  //                        Constraints.LayoutParams.MATCH_PARENT,
  //                        Constraints.LayoutParams.MATCH_PARENT
  //                    )
  //                    param.height = 0
  //                    binding.rlUserProfile.layoutParams = param
                      val animation: Animation = AnimationUtils.loadAnimation(
                          this,
                          anim.slide_bottom_to_origin
                      )
                      binding.rlUserProfile.animation = animation
                      binding.rlProfileDetails.visibility = View.VISIBLE
                      binding.constrainntLayout.setBackgroundColor(
                          resources.getColor(
                              R.color.bg_profile_black,
                              null
                          )
                      )
                      binding.linearLayout.setBackgroundColor(0)
  
                  }
  
  
              }
  */
            binding.tvFollow -> {
                val type = if (binding.tvFollow.isSelected) {
                    "Unfollow"
                } else "Follow"
                viewModel.followUnfollowApi(token!!, userId, loginId, type)
            }
            binding.imgEditSmall -> {
                var prevBio = ""
                if (globalProfileResponse != null) {
                    prevBio = globalProfileResponse!!.result.userBio
                    val ins = Intent(this, EditBioActivity::class.java)
                    ins.putExtra(IntentConstant.PROFILE_BIO, prevBio)
                    startActivity(ins)
                }
            }
            binding.tvFollowers -> {
                if (globalProfileResponse != null) {
                    var ids = ""
                    ids = if (loginId.trim().isEmpty()) {
                        pref.getString(PrefConstant.USER_ID, "").toString()
                    } else {
                        loginId
                    }
                    val ins = Intent(this, ActivityFollowerFollowing::class.java)
                    ins.putExtra(
                        IntentConstant.PROFILE_ID, ids
                    )
                    ins.putExtra(IntentConstant.FOLLOW_LIST, 1)
                    startActivity(ins)
                }
            }
            binding.tvFollowersTitle -> {
                if (globalProfileResponse != null) {
                    var ids = ""
                    ids = if (loginId.trim().isEmpty()) {
                        pref.getString(PrefConstant.USER_ID, "").toString()
                    } else {
                        loginId
                    }
                    val ins = Intent(this, ActivityFollowerFollowing::class.java)
                    ins.putExtra(
                        IntentConstant.PROFILE_ID, ids
                    )
                    ins.putExtra(IntentConstant.FOLLOW_LIST, 1)
                    startActivity(ins)
                }
            }
            binding.tvFollowing -> {
                if (globalProfileResponse != null) {
                    var ids = ""
                    ids = if (loginId.trim().isEmpty()) {
                        pref.getString(PrefConstant.USER_ID, "").toString()
                    } else {
                        loginId
                    }
                    val ins = Intent(this, ActivityFollowerFollowing::class.java)
                    ins.putExtra(
                        IntentConstant.PROFILE_ID, ids
                    )
                    ins.putExtra(IntentConstant.FOLLOW_LIST, 0)
                    startActivity(ins)
                }
            }
            binding.tvFollowingTitle -> {
                if (globalProfileResponse != null) {
                    var ids = ""
                    ids = if (loginId.trim().isEmpty()) {
                        pref.getString(PrefConstant.USER_ID, "").toString()
                    } else {
                        loginId
                    }
                    val ins = Intent(this, ActivityFollowerFollowing::class.java)
                    ins.putExtra(
                        IntentConstant.PROFILE_ID, ids
                    )
                    ins.putExtra(IntentConstant.FOLLOW_LIST, 0)
                    startActivity(ins)
                }
            }
            binding.imgMenu -> {
                val bundle = Bundle()
                bundle.putString(IntentConstant.PROFILE_ID, loginId)
                bundle.putString(IntentConstant.CHAT_IMG, globalProfileResponse?.result?.profilePic)
                bundle.putString(IntentConstant.CHAT_NAME, globalProfileResponse?.result?.name)
                bundle.putString(IntentConstant.CHAT_TYPE, "2")
                bundle.putString(IntentConstant.IS_FOLLOWERS, isFollowers)
                bundle.putString(IntentConstant.ISPAGE_THROUGH, "profile")
                bundle.putString(
                    IntentConstant.FOLLOWING_GROUP,
                    globalProfileResponse?.result?.follow_group
                )
                val bottomSheet = ProfileOptionBottomFragment()
                bottomSheet.arguments = bundle
                bottomSheet.show(supportFragmentManager, "profile_menu")
            }

            binding.menu -> {


                dataPasser?.open(username);
            }

            binding.tvMessage -> {

                val intent = Intent(this, ChatRoomActivity::class.java)
                intent.putExtra(IntentConstant.CHAT_ID, globalProfileResponse?.result?.userId)
                intent.putExtra(IntentConstant.CHAT_NAME, globalProfileResponse?.result?.name)
                intent.putExtra(IntentConstant.CHAT_IMG, globalProfileResponse?.result?.profilePic)
                intent.putExtra(IntentConstant.CHAT_TYPE, returnChatType(globalProfileResponse?.result?.follow_group!!))
                startActivity(intent)

            }

            binding.tvPost -> {


                binding.vpViewPagger.currentItem = 0

            }

            binding.tvFlick -> {

                binding.vpViewPagger.currentItem = 3
            }



//            binding.back -> {
//
//                dataPasser?.backclick(username);
//            }
        }
    }

    private fun observeProfile() {
        viewModel.response.observe(this, Observer {

            binding.progressBar.visibility = View.GONE

            globalProfileResponse = it

//            var entity:ProfileGetResponseEntity

            binding.tvFollowers.text = it.result.follower
            binding.tvFollowing.text = it.result.following
            binding.tvPost.text = it.result.post
            binding.tvFlick.text = it.result.flicks
            binding.tvUserName.text = "@${it.result.username}"
            binding.tvFullName.text = it.result.name
            username = it.result.username
            binding.tvBio.text = it.result.userBio
            binding.tvBioTitle.text = it.result.userBio
            isFollowers = it.result.isFollowers
            binding.tabLayout.getTabAt(0)?.select()
            if (it.result.userId != userId && !it.result.follow_group.isNullOrEmpty()) {
                if (!it.result.follow_group.contains("personal")) {
                    //   (binding.tabLayout.getTabAt(1)?.view as LinearLayout).visibility = View.VISIBLE
                }
                if (!it.result.follow_group.contains("professional")) {
                    //   (binding.tabLayout.getTabAt(2)?.view as LinearLayout).visibility = View.VISIBLE
                }

            }



            //   loadImage(binding.imgProfilePic, it.result.profilePic)
            loadImage(binding.profileImage, it.result.profilePic)
            if (it.result.isPopular) {

                binding.imgVerified.visibility = View.VISIBLE
            } else {
                binding.imgVerified.visibility = View.GONE
            }
            // check and view with login id , this id is come from caller page
            if (loginId.trim().isNotEmpty()) {
                binding.tvFollow.visibility = View.VISIBLE
                binding.tvMessage.visibility= View.VISIBLE
                if (it.result.isfollowing.equals("Yes", true)) {
                    binding.tvFollow.text =
                        this.resources.getString(R.string.following)
                    binding.tvFollow.isSelected = true
                } else if (it.result.isFollowers.equals("Yes", true)) {
                    binding.tvFollow.text = this.resources.getString(R.string.follow)
                    binding.tvFollow.isSelected = false
                }
            } else {

                pref.setString(PrefConstant.PROFILE_PIC,it.result.profilePic)
                binding.tvFollow.visibility = View.GONE
                binding.tvMessage.visibility = View.GONE
            }
            setPostFeeds(it.result.postArr)
//
//            binding.shimmerLayout.stopShimmer();
//            binding.shimmerLayout.visibility = View.GONE
            //   binding.mainLinear.visibility = View.VISIBLE



            if (isNetworkAvailable(this) && loginId.trim().isEmpty()) {

//                for (i in 0 until it.result.flickArr.size) {
//
//                    DownloadTask(this, it.result.flickArr[i].flickMedia)
//
//                    var apkStorage = File(
//                        Environment.getExternalStorageDirectory()
//                            .toString() + "/" + "connectedindia"
//                    )
//                    var  downloadFileName =
//                        it.result.flickArr[i].flickMedia.substring(it.result.flickArr[i].flickMedia.lastIndexOf('/'), it.result.flickArr[i].flickMedia.length)
//
//                    it.result.flickArr.get(i)!!.localpath =
//                        apkStorage.path + downloadFileName
//
//                    Log.e("dataprofileflick", apkStorage.path + downloadFileName)
//                }





                connectedAppDatabase?.appDao()?.insertProfile(it)
            }


            //                for (i in 0 until flick_list.size) {
//
//                    Log.e("yooooo", it.result.flickArr.get(i)!!.localpath)
//
//                }

        })

        viewModel.errString.observe(this, Observer { err: String ->
            if (!err.isNullOrEmpty())
                showSnackBar(binding.tvFollowing, err)
            binding.progressBar.visibility = View.GONE

        })

        viewModel.errRes.observe(this, Observer{ err: Int ->
            if (err != null)
                showSnackBar(binding.root, getString(err))
            binding.progressBar.visibility = View.GONE

        })

        viewModel.responseEditBio.observe(this, Observer {
            if (it != null) {
                showSnackBar(binding.root, it.aPICODERESULT)
                isEditProfilePic = false
                binding.progressBar.visibility = View.GONE

            }
        })
    }

    private fun tabSelectedListener() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> //setPostFeeds(globalProfileResponse?.result?.postArr)
                        binding.vpViewPagger.currentItem = 0
                    1 -> //setPostFeeds(globalProfileResponse?.result?.personalArr)
                        binding.vpViewPagger.currentItem = 1
                    2 ->// setPostFeeds(globalProfileResponse?.result?.professionalArr)
                        binding.vpViewPagger.currentItem = 2
                    3 -> //setFlicks(globalProfileResponse?.result?.flickArr)
                        binding.vpViewPagger.currentItem = 3
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    private fun setPostFeeds(list: List<ProfileGetResponse.ResultProfile.PostArr>?) {
        if (!list.isNullOrEmpty()) {
            //   binding.rvFeed.visibility = View.VISIBLE
            val followGroup =
                if (binding.tabLayout.selectedTabPosition == 1) "personal" else if (binding.tabLayout.selectedTabPosition == 2) "professional" else ""
            binding.rvFeed.adapter =
                ProfileFeedAdapter(this, list, loginId, followGroup)
        } else {
            binding.rvFeed.visibility = View.GONE
        }
    }

    private fun setFlicks(list: List<GetFlickResponse.Result>?) {
        if (!list.isNullOrEmpty()) {
            //   binding.rvFeed.visibility = View.VISIBLE
            binding.rvFeed.adapter = ProfileFlickAdapter(this, list, loginId)
        } else {
            binding.rvFeed.visibility = View.GONE
        }
    }

    private fun loadImage(img: ImageView, imgPAth: String) {
        Glide.with(this)
            .load(imgPAth)
            .thumbnail(Glide.with(this).load(R.drawable.ic_profile))
            .placeholder(R.drawable.ic_profile)
            .centerCrop()
            .into(img)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentConstant.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            getProfileData()
        } else
            ImagePickerUtil.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsUtil?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onResume() {
        super.onResume()

        // binding.mainLinear.visibility =View.GONE
        //     binding.shimmerLayout.startShimmer()
    }

//    override fun afterDownloadPath(file: File?) {
//        if(file?.path?.contains("mp4") == true)
//        {
//            globalProfileResponse?.result?.flickArr?.get(count)?.localpath = file.path
//            count++
//
//            globalProfileResponse?.let { connectedAppDatabase?.appDao()?.insertProfile(it) }
//        }
//        else
//        {
//
//        }
//    }



    fun getdata(patht: String)
    {

        this.path = patht

        //  Toast.makeText(this, path, Toast.LENGTH_SHORT).show()


        //   Toast.makeText(this, path, Toast.LENGTH_SHORT).show()

        Log.e(
            "yoyooyoyo",
            path
        )

        if(path.contains(".mp4", ignoreCase = true))
        {
            globalProfileResponse?.result?.flickArr?.get(count)?.localpath = path
            count++





            Log.e(
                "yoyooyoyo",
                globalProfileResponse?.result?.flickArr?.get(count)?.localpath.toString()
            )

        }
        else
        {
            Toast.makeText(this, path, Toast.LENGTH_SHORT).show()
        }
    }

    override fun listner() {
        //   binding.shimmerLayout.visibility  =View.VISIBLE
        // binding.shimmerLayout.startShimmer()

    }


    override fun onBackPressed() {
        //super.onBackPressed()
        finish()
    }
}