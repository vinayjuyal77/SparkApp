package com.app.spark.activity.followers.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.activity.UsersProfileActivity
import com.app.spark.activity.followers.FollowingAdaptor2
import com.app.spark.activity.followers.ProfessionalFollowingAdaptor2
import com.app.spark.activity.followers.viewmodel.ProfessionalFollowersViewModel
import com.app.spark.activity.main.MainActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.FragmentProfessionalBinding
import com.app.spark.models.ImportantDataResult
import com.app.spark.models.ResultFollowing
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.showSnackBar
import com.app.spark.utils.showToastShort
import com.google.gson.Gson

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfessionalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfessionalFragment : Fragment(),
        FollowingAdaptor2.RemoveFollower {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentProfessionalBinding
    private lateinit var viewModel: ProfessionalFollowersViewModel
    private lateinit var followingAdaptor2: ProfessionalFollowingAdaptor2
    private lateinit var layoutManager: GridLayoutManager
    lateinit var list: ArrayList<ResultFollowing>


    private var type: String = "followers"
    var connectionType = "professional"
    private var offSet: Int = 0

    lateinit var pref: SharedPrefrencesManager
    private var token: String? = null
    private var userId: String? = null

    // private lateinit var adapter: FollowingAdaptor
    private var followingList = ArrayList<ResultFollowing>()
    private var loginDetails: ImportantDataResult? = null

    private var loading = true
    private var pastVisiblesItems = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)


        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_professional, container, false)
        viewModel = ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(ProfessionalFollowersViewModel::class.java)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pref = SharedPrefrencesManager.getInstance(requireContext())
        followingList = ArrayList();

        inItAdaptor()
        loginDetails = Gson().fromJson(
                pref.getString(PrefConstant.LOGIN_RESPONSE, ""),
                ImportantDataResult::class.java
        )
        if (loginDetails != null)

            viewModel.setLoginUserID(loginDetails?.user_id!!)

        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")
        userId = pref.getString(PrefConstant.USER_ID, "")

//        if (intent.hasExtra(IntentConstant.FOLLOW_LIST)) {
//            if (intent.getIntExtra(IntentConstant.FOLLOW_LIST, 0) == 1) {
//                type = "followers"
//                offSet = 0
//                binding.tabLayout.getTabAt(0)?.select()
//                onConnectionTypeSelection(3)
//            } else {
//                binding.tabLayout.getTabAt(1)?.select()
//                offSet = 0
//                type = "following"
//                onConnectionTypeSelection(3)
//            }
//        } else {
//            onConnectionTypeSelection(3)
        getFoLLowingFollow()
//        }
        recycleViewScrollListner()

        observer()
        //  setViewPager()


    }

    private fun observer() {
        viewModel.response.observe(viewLifecycleOwner, Observer {
            if (followingList.isNotEmpty()) {
                followingList.clear()
            }
            for (i in it.result.indices) {
                it.result[i].myType = type
                followingList.add(it.result[i])
            }
      //      followingList.addAll(it.result)
           followingAdaptor2.notifyDataSetChanged()
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


        viewModel.errString.observe(viewLifecycleOwner, Observer { err: String ->
            //hideView(binding.progressBar)
            showToastShort(context, err)
        })

        viewModel.errRes.observe(viewLifecycleOwner, Observer{ err: Int ->
            //  binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }


    private fun inItAdaptor() {
        followingAdaptor2 = ProfessionalFollowingAdaptor2(context!!, followingList, this)
        layoutManager = GridLayoutManager(context,2)
        binding.personalRecyclerview.layoutManager = layoutManager
        binding.personalRecyclerview.itemAnimator = DefaultItemAnimator()
        binding.personalRecyclerview.adapter = followingAdaptor2
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PersonalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
                ProfessionalFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)


                    }
                }
    }


//    private fun clearPrevPagiData() {
//        if (intent.hasExtra(IntentConstant.PROFILE_ID)) {
//            if (!intent.getStringExtra(IntentConstant.PROFILE_ID).toString()
//                    .equals(loginDetails?.user_id, false)
//            ) {
//                binding.llConnectionType.visibility = View.GONE
//            }
//        }
//        pastVisiblesItems = 0
//        visibleItemCount = 0
//        totalItemCount = 0
//        loading = false
//        if (followingList.isNotEmpty()) {
//            followingList.clear()
//        }
//        adapter.notifyDataSetChanged()
//        getFoLLowingFollow()
//    }

    private fun recycleViewScrollListner() {
        binding.personalRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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


    private fun getFoLLowingFollow() {
//        if (isNetworkAvailable(this)) {
//            token?.let { it1 ->
//                intent.getStringExtra(IntentConstant.PROFILE_ID)?.let {

        token?.let {
            param1?.let { it1 ->
                viewModel.getFollowingAndFollowers(
                        token = it,
                        type = type,
                        followingGroup = connectionType,
                        offset = offSet.toString(),
                        userID = it1
                )
            }
        }
    }

//            }
//        } else {
//
//        }


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
                val ins = Intent(context, UsersProfileActivity::class.java)
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


    private fun emptyRecycleCheck(pos: Int) {
        if (pos == 1) {
            binding.tvNamaste.visibility = View.GONE
            binding.tvWeWelcomeYou.visibility = View.GONE
            binding.tvFollowPeoplePages.visibility = View.GONE
            binding.personalRecyclerview.visibility = View.VISIBLE
        } else {
            binding.tvNamaste.visibility = View.VISIBLE
            binding.tvWeWelcomeYou.visibility = View.VISIBLE
            binding.tvFollowPeoplePages.visibility = View.VISIBLE
            binding.personalRecyclerview.visibility = View.GONE
        }
    }
}