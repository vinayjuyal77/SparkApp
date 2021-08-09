package com.app.spark.fragment.profile.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.app.spark.R
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.FragmentProfileFlickBinding
import com.app.spark.fragment.profile.ProfileFlickAdapter
import com.app.spark.fragment.profile.ProfileViewPager
import com.app.spark.models.GetFlickResponse
import com.app.spark.models.ProfileGetResponse
import com.app.spark.utils.*
import com.app.spark.viewmodel.ProfileGetViewModel
import com.cancan.Utility.PermissionsUtil

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFlickFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class ProfileFlickFragment :  Fragment() {
    // TODO: Rename and change types of parameters
    private var param2: String? = null


    lateinit var binding: FragmentProfileFlickBinding
    lateinit var viewModel: ProfileGetViewModel

    private var globalProfileResponse: ProfileGetResponse? = null
    private lateinit var pref: SharedPrefrencesManager
    private var token: String? = null
    private var loginId: String = ""
    private var userId: String = ""
    private var isFollowers = ""
    private var isProfileOpen = false
    private var isEditProfilePic = false
    private var permissionsUtil: PermissionsUtil? = null
    private lateinit var profileViewPager: ProfileViewPager
    private lateinit var list: ArrayList<GetFlickResponse.Result>

    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var profilickadapter : ProfileFlickAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            loginId = it.getString(ARG_PARAM1).toString()
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_flick, container, false)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(ProfileGetViewModel::class.java)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionsUtil = PermissionsUtil(requireActivity())
        list  = ArrayList()


        pref = SharedPrefrencesManager.getInstance(requireContext())
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")
        userId = pref.getString(PrefConstant.USER_ID, "")!!

        binding.rvFeed.addItemDecoration(GridSpacingItemDecoration(3, 15, true, 0))
        gridLayoutManager = GridLayoutManager(requireContext(),3)
        binding.rvFeed.layoutManager = gridLayoutManager
        profilickadapter  = ProfileFlickAdapter(requireActivity(), list, loginId)
        binding.rvFeed.adapter = profilickadapter


        if (isNetworkAvailable(requireContext())) {
            getProfileData()

        } else {

            viewModel.getLcoalData(requireContext())
        }

        observeProfile()



    }


    private fun getProfileData() {
        if (isNetworkAvailable(requireActivity())) {
            //   binding.progressBar.visibility = View.VISIBLE
            var userId = pref.getString(PrefConstant.USER_ID, "")
            if (loginId.trim().isEmpty()) {
                viewModel.getUserProfileData(token!!, userId!!, userId)
            } else {
                viewModel.getUserProfileData(token!!, loginId, userId!!)
            }
        } else {
            showSnackBar(
                binding.root,
                requireActivity().resources.getString(R.string.please_check_internet)
            )
        }
    }



    private fun observeProfile() {
        viewModel.response.observe(viewLifecycleOwner, Observer {
            //   binding.progressBar.visibility = View.GONE
            globalProfileResponse = it
            isFollowers = it.result.isFollowers


            // check and view with login id , this id is come from caller page

            setFlicks(globalProfileResponse?.result?.flickArr)
          //  setPostFeeds(it.result.postArr)

            })

        viewModel.errString.observe(viewLifecycleOwner, Observer { err: String ->
            ///    binding.progressBar.visibility = View.GONE
            //   if (!err.isNullOrEmpty())
            //      showSnackBar(binding.tvFollowing, err)
        })

        viewModel.errRes.observe(viewLifecycleOwner, Observer{ err: Int ->
            //    binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })

        viewModel.responseEditBio.observe(viewLifecycleOwner,Observer {
            if (it != null) {
                showSnackBar(binding.root, it.aPICODERESULT)
                isEditProfilePic = false
            }
        })
    }

    private fun setPostFeeds(list: List<ProfileGetResponse.ResultProfile.PostArr>?) {
        if (!list.isNullOrEmpty()) {
//            binding.rvFeed.visibility = View.VISIBLE
//            profiliefeedadapter2  = ProfileFeedAdapter2(requireActivity(), list, loginId, "personal")
//            binding.rvFeed.adapter = profiliefeedadapter2;
//            profiliefeedadapter2.notifyDataSetChanged()

        } else {
            //   binding.rvFeed.visibility = View.GONE
            // binding.tvNoPost.visibility = View.VISIBLE
        }
    }


    private fun setFlicks(list: List<GetFlickResponse.Result>?) {
        if (!list.isNullOrEmpty()) {
            binding.rvFeed.visibility = View.VISIBLE
            profilickadapter  = ProfileFlickAdapter(requireActivity(), list, loginId)
            binding.rvFeed.adapter = profilickadapter;
            profilickadapter.notifyDataSetChanged()
        } else {
            binding.rvFeed.visibility = View.GONE
            // binding.tvNoPost.visibility = View.VISIBLE
        }
    }










    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfilePersonalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFlickFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}