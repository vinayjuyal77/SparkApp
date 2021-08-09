package com.app.spark.fragment.profile

import android.app.Activity
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
import com.app.spark.R.*
import com.app.spark.activity.FixYourProfileActivity
import com.app.spark.activity.NewLoginActivity
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
import com.app.spark.databinding.FragmentProfile2Binding
import com.app.spark.databinding.FragmentProfileBinding
import com.app.spark.interfaces.DrawerListener
import com.app.spark.models.GetFlickResponse
import com.app.spark.models.ProfileGetResponse
import com.app.spark.photoeditor.Utils.Constant
import com.app.spark.utils.*
import com.app.spark.viewmodel.ProfileGetViewModel
import com.bumptech.glide.Glide
import com.cancan.Utility.PermissionsUtil
import com.google.android.material.tabs.TabLayout
import java.io.File



class ProfileFragment : BaseFragment(), View.OnClickListener, ProfileGetViewModel.ProfileListerner{
    private lateinit var binding: FragmentProfile2Binding
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


    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = DataBindingUtil.inflate(inflater, layout.fragment_profile2, container, false)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(ProfileGetViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionsUtil = PermissionsUtil(requireActivity())
        binding.rvFeed.addItemDecoration(GridSpacingItemDecoration(3, 15, true, 0))
        pref = SharedPrefrencesManager.getInstance(requireContext())
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")
        userId = pref.getString(PrefConstant.USER_ID, "")!!

        binding.progressBar.visibility =View.VISIBLE
        connectedAppDatabase = ConnectedAppDatabase.getInstance(requireContext())
        dataPasser = context as DrawerListener





        askPermission()


        observeProfile()
        setClickListener()
        tabSelectedListener()

        if (arguments?.getString(IntentConstant.PROFILE_ID).toString().trim().isNotEmpty()) {
            loginId = arguments?.getString(IntentConstant.PROFILE_ID).toString()
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




        if(isNetworkAvailable(requireContext()))
        {
            getProfileData()

        }
        else
        {
         //   viewModel.getLcoalMainData(requireContext(), this)
        }

        setViewPager();


        binding.imgShare.setOnClickListener {


            share(requireActivity(), "https://www.connectd.com/profile/"+pref.getString(PrefConstant.USER_ID,""))


        }



        binding.logout.setOnClickListener {

            viewModel?.updateDeviceTokenApi(
                pref?.getString(PrefConstant.ACCESS_TOKEN, ""),
                pref?.getString(PrefConstant.USER_ID, ""),
                ""
            )
            pref?.clear()
            clearAllgoToActivity(requireContext(), NewLoginActivity::class.java)

        }


    }


    private fun askPermission() {
        permissionsUtil?.askPermissions(requireActivity(), PermissionsUtil.CAMERA,
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
        profileViewPager = ProfileViewPager(requireContext(), childFragmentManager, loginId)
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
        if (isNetworkAvailable(requireActivity())) {
            var userId = pref.getString(PrefConstant.USER_ID, "")
            if (loginId.trim().isEmpty()) {
                viewModel.getUserProfileData(token!!, userId!!, userId)
            } else {
                viewModel.getUserProfileData(token!!, loginId, userId!!)
            }
        } else {
            showSnackBar(
                binding.root,
                requireActivity().resources.getString(string.please_check_internet)
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
                startActivity(Intent(context, ActivityPlayInformation::class.java))
            }
            binding.imgPlay -> {
                startActivity(Intent(context, ActivityPlayInformation::class.java))
            }
            binding.tvProfessional -> {
                startActivity(Intent(context, ActivityProfessionalInformation::class.java))
            }
            binding.imgProfessional -> {
                startActivity(Intent(context, ActivityProfessionalInformation::class.java))
            }
            binding.imgPersonal -> {
                startActivity(Intent(context, ActivityPersonalInformation::class.java))
            }
            binding.tvPersonal -> {
                startActivity(Intent(context, ActivityPersonalInformation::class.java))
            }
            binding.imgEdit -> {
                startActivityForResult(
                    Intent(requireContext(), FixYourProfileActivity::class.java),
                    IntentConstant.REQUEST_CODE
                )
                /*permissionsUtil?.askPermissions(
                    requireActivity(),
                    PermissionsUtil.STORAGE,
                    PermissionsUtil.CAMERA,
                    object : PermissionsUtil.PermissionListener {
                        override fun onPermissionResult(isGranted: Boolean) {
                            if (isGranted) {
                                ImagePickerUtil.selectImage(
                                    requireContext(),
                                    { file: File, s: String ->
                                        isEditProfilePic = true
                                        Glide.with(requireContext()).load(file)
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
                     requireActivity(),
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
                        requireActivity(),
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
                    val ins = Intent(requireContext(), EditBioActivity::class.java)
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
                    val ins = Intent(requireActivity(), ActivityFollowerFollowing::class.java)
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
                    val ins = Intent(requireActivity(), ActivityFollowerFollowing::class.java)
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
                    val ins = Intent(requireActivity(), ActivityFollowerFollowing::class.java)
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
                    val ins = Intent(requireActivity(), ActivityFollowerFollowing::class.java)
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
                bottomSheet.show(childFragmentManager, "profile_menu")
            }

            binding.menu -> {


                dataPasser?.open(username);
            }

            binding.tvMessage -> {

                val intent = Intent(requireContext(), ChatRoomActivity::class.java)
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
        viewModel.response.observe(viewLifecycleOwner, Observer {

            binding.progressBar.visibility =View.GONE

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
                        requireActivity().resources.getString(string.following)
                    binding.tvFollow.isSelected = true
                } else if (it.result.isFollowers.equals("Yes", true)) {
                    binding.tvFollow.text = requireActivity().resources.getString(string.follow)
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


//
//            if (isNetworkAvailable(requireContext()) && loginId.trim().isEmpty()) {
//
//                connectedAppDatabase?.appDao()?.insertProfile(it)
//            }


            //                for (i in 0 until flick_list.size) {
//
//                    Log.e("yooooo", it.result.flickArr.get(i)!!.localpath)
//
//                }

        })

        viewModel.errString.observe(viewLifecycleOwner, Observer { err: String ->
            if (!err.isNullOrEmpty())
                showSnackBar(binding.tvFollowing, err)
            binding.progressBar.visibility =View.GONE

        })

        viewModel.errRes.observe(viewLifecycleOwner, Observer{ err: Int ->
            if (err != null)
                showSnackBar(binding.root, getString(err))
            binding.progressBar.visibility =View.GONE

        })

        viewModel.responseEditBio.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showSnackBar(binding.root, it.aPICODERESULT)
                isEditProfilePic = false
                binding.progressBar.visibility =View.GONE

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
                ProfileFeedAdapter(requireActivity(), list, loginId, followGroup)
        } else {
            binding.rvFeed.visibility = View.GONE
        }
    }

    private fun setFlicks(list: List<GetFlickResponse.Result>?) {
        if (!list.isNullOrEmpty()) {
            //   binding.rvFeed.visibility = View.VISIBLE
            binding.rvFeed.adapter = ProfileFlickAdapter(requireActivity(), list, loginId)
        } else {
            binding.rvFeed.visibility = View.GONE
        }
    }

    private fun loadImage(img: ImageView, imgPAth: String) {
        Glide.with(requireActivity())
            .load(imgPAth)
            .thumbnail(Glide.with(requireActivity()).load(drawable.ic_profile))
            .placeholder(drawable.ic_profile)
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

    }






    override fun listner() {


    }





}