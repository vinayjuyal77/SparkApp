package com.app.spark.fragment.explore

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.counsellor.ConnectCounsellorActivity
import com.app.spark.activity.explore.ExploreBottomSheetFragment
import com.app.spark.activity.explore.FindMeFriendActivity
import com.app.spark.activity.search.SearchActivity
import com.app.spark.adapter.NotificationListAdaptor
import com.app.spark.bottomSheet.profilemenu.ProfileOptionBottomFragment
import com.app.spark.bottomSheet.profilemenu.ProfileOptionViewModel
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.database.ConnectedAppDatabase
import com.app.spark.databinding.FragmentExploreBinding
import com.app.spark.dialogs.LiveSoonDialog
import com.app.spark.dialogs.ProfileConnectionTypeDialog
import com.app.spark.fragment.groupcall.RaiseHandListBottomFragment
import com.app.spark.interfaces.NotificationListClickItem
import com.app.spark.interfaces.ProfileOnConnectionTypeSelected
import com.app.spark.models.ResultNotification
import com.app.spark.utils.*
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback


class ExploreFragment : BaseFragment(), View.OnClickListener, NotificationListClickItem,
    ProfileOnConnectionTypeSelected {
    private lateinit var binding: FragmentExploreBinding
    private lateinit var viewModel: ProfileOptionViewModel
    private lateinit var pref: SharedPrefrencesManager
    private var token: String? = null
    var userId: String? = null
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: NotificationListAdaptor
    private var notificationList = ArrayList<ResultNotification>()
    private var otherProfileId = ""



    private var connectedAppDatabase : ConnectedAppDatabase? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_explore, container, false)
        viewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(ProfileOptionViewModel::class.java)
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
        userId = pref.getString(PrefConstant.USER_ID, "")
        //   viewModel.popularProfileApi(token!!, pref.getString(PrefConstant.USER_ID, "")!!)

        connectedAppDatabase = ConnectedAppDatabase.getInstance(requireContext())
        setClickListener()
        inItAdaptor()
        observePopularProfiles()
        setViewPager()
        viewModel.setUserData(token, pref.getString(PrefConstant.USER_ID, "").toString())

        if(isNetworkAvailable(requireContext()))
        {
            getNotificationList()
            recycleViewScrollListner()

        }
        else
        {
          // viewModel.getLcoalData()
        }





    }




    private fun recycleViewScrollListner() {
        binding.rvNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // Position of the row that is active
            var activeAdapter = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // Get the index of the first Completely visible item
                val firstCompletelyVisibleItemPosition: Int =
                    layoutManager.findFirstCompletelyVisibleItemPosition()
                val visibleItemCount: Int = layoutManager.childCount
                val totalItemCount: Int = layoutManager.itemCount
                val pastVisibleItems: Int = layoutManager.findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount&& !viewModel.isLoading) {
                    viewModel.pagingFeedListingApi(adapter.itemCount)
                }

            }
        })
    }


    private fun getNotificationList() {
        if (isNetworkAvailable(requireActivity())) {
            viewModel.pagingFeedListingApi(null)
        } else {
            showToastLong(
                requireActivity(),
                requireActivity().resources.getString(R.string.please_check_internet)
            )
        }
    }

    private fun inItAdaptor() {
        adapter = NotificationListAdaptor(requireActivity(), notificationList, this)
        layoutManager =
            LinearLayoutManager(requireActivity())
        binding.rvNotification.layoutManager = layoutManager
        binding.rvNotification.itemAnimator = DefaultItemAnimator()
        binding.rvNotification.adapter = adapter
    }

    private fun setClickListener() {
        binding.imgAddPost.setOnClickListener(this)
        binding.imgSearch.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.imgAddPost -> {
                mainActivityInterface?.onAddPost()
               // ExploreBottomSheetFragment().show(parentFragmentManager, "raise_hand_list")
            }
            binding.imgSearch -> {
                startActivity(Intent(requireContext(), SearchActivity::class.java))
            }
        }
    }

    private fun observePopularProfiles() {
        viewModel.responseNotifications.observe(viewLifecycleOwner, Observer {
            // binding.progressBar.visibility = View.GONE

            if (notificationList.isNotEmpty()&& !viewModel.isPaging) {
                adapter.updateList(it.result)
               // adapter.notifyDataSetChanged()
            } else {
                notificationList.addAll(it.result)
                adapter.notifyDataSetChanged()

            }
            if(notificationList.isEmpty()){
                binding.tvNoActivity.visibility=View.VISIBLE
                binding.rvNotification.visibility=View.GONE
            }else{
                binding.tvNoActivity.visibility=View.GONE
                binding.rvNotification.visibility=View.VISIBLE
            }


//            if(isNetworkAvailable(requireContext()))
//            {
//                connectedAppDatabase?.appDao()?.insertNotification(it)
//            }
        })

        viewModel.responseConnectionType.observe(viewLifecycleOwner, Observer {
            //reset
            otherProfileId = ""
            getNotificationList()
            showToastLong(requireActivity(), it.APICODERESULT)
        })

        viewModel.errString.observe(viewLifecycleOwner, Observer { err: String ->
            // binding.progressBar.visibility = View.GONE
            if (!err.isNullOrEmpty())
                showSnackBar(binding.root, err)
        })

        viewModel.errRes.observe(viewLifecycleOwner, Observer { err: Int ->
           // binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }

    private fun setViewPager() {
        val feedsPagerAdapter = ExplorePagerAdapter(
            requireContext()
        )
        binding.vpActivities.adapter = feedsPagerAdapter
        val margin = resources.getDimensionPixelSize(R.dimen._70sdp)
        binding.vpActivities.setPadding(margin, 0, margin, 0)
        binding.vpActivities.clipToPadding = false
        binding.vpActivities.pageMargin = margin / 2

        feedsPagerAdapter.onItemClick={
            ValueItem->
            if(ValueItem.equals(getString(R.string.counsellor))) {
                startActivity(Intent(requireContext(), ConnectCounsellorActivity::class.java))
                /*LiveSoonDialog.Builder(activity)
                    .setTextTitle(getString(R.string.live_soon))
                    .setTextBody(getString(R.string.constantly_working))
                    .setOkFunction {}
                    .build()*/
            }
            else startActivity(Intent(requireContext(), FindMeFriendActivity::class.java).apply {action=ValueItem})
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.isOnlineStatusApi(requireContext(),2, userId!!.toInt())
    }

    override fun onNotiOverFlowMenuClickItem(item: ResultNotification) {
        if (item != null) {
            //reset
            otherProfileId = ""
            var bundle = Bundle()
            bundle.putString(IntentConstant.PROFILE_ID, item.userId)
            bundle.putString(IntentConstant.CHAT_IMG, item.profilePic)
            bundle.putString(IntentConstant.CHAT_NAME, item.name)
            bundle.putInt(IntentConstant.CHAT_TYPE, returnChatType(item.follow_Group))
            bundle.putString(IntentConstant.IS_FOLLOWERS, "yes")
            bundle.putString(IntentConstant.FOLLOWING_GROUP,item.follow_Group)
            bundle.putString(IntentConstant.ISPAGE_THROUGH,"")
            var bottomSheet = ProfileOptionBottomFragment()
            bottomSheet.arguments = bundle
            bottomSheet.show(childFragmentManager, "profile_menu")
        }
    }

    override fun onNotiChangeConnectionItem(item: ResultNotification) {
        if (item != null) {
            otherProfileId = item.userId
            ProfileConnectionTypeDialog(item.follow_Group, requireActivity(), this).show()
        }
    }

    override fun onSelectedConnection(type: String) {
        if (type != null) {
            var myType = ""
            myType = if (type.equals("Public/Play", true)) {
                "public"
            } else {
                type
            }
            if (isNetworkAvailable(requireActivity())) {
                viewModel.connectionTypeApi(
                    token = token!!,
                    follower_id = otherProfileId,
                    follow_group = myType.toLowerCase(),
                    user_id = pref.getString(PrefConstant.USER_ID, toString())!!
                )
            }
        }
    }

}