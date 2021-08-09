package com.app.spark.fragment.groupcall

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.spark.R
import com.app.spark.constants.AppConstants.BundleConstants.isResumeCallList
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.FragmentGroupCallListBinding
import com.app.spark.dialogs.AppReportDiloag
import com.app.spark.fragment.groupcall.adapter.GroupListAdapter
import com.app.spark.fragment.groupcall.agora.AgoraMainActivity
import com.app.spark.fragment.groupcall.model.ConstantApp
import com.app.spark.models.GetRoomResponse
import com.app.spark.utils.BaseFragment
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.isNetworkAvailable
import com.app.spark.utils.showToastShort

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupCallList.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupCallList : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var  layoutManager: LinearLayoutManager
    lateinit var groupcallAdapter: GroupListAdapter
    lateinit var binding : FragmentGroupCallListBinding
    lateinit var viewModel : GroupCallListViewModel
    lateinit var pref: SharedPrefrencesManager
    var userId: String? = null
    private var loading = true
    private var pastVisiblesItems = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0
    private var listItem : ArrayList<GetRoomResponse.UserData> = ArrayList()
    private var offSet: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_group_call_list, container, false)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(GroupCallListViewModel::class.java)
        pref = SharedPrefrencesManager.getInstance(requireContext())
        userId = pref.getString(PrefConstant.USER_ID, "")
        return binding.root
    }
    private var selectedList : ArrayList<Int> = ArrayList()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listItem= ArrayList()
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false )
        binding.recyclviewgroup.layoutManager = layoutManager
        groupcallAdapter  = GroupListAdapter(requireActivity(),listItem)
        binding.recyclviewgroup.adapter = groupcallAdapter
        groupcallAdapter.notifyDataSetChanged()
        groupcallAdapter.onItemListener = {Pos,RoomId->
            roomId = RoomId
            if(listItem[Pos].user_ids.contains(userId!!.toInt())){
                isResumeCallList=true
                startActivity(Intent(requireContext(), AgoraMainActivity::class.java).apply {
                    putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, RoomId.toString())
                })
            }else{
                selectedList.clear()
                selectedList.add(userId!!.toInt())
                viewModel.addUserToRoom(userId!!.toInt(), RoomId, selectedList)
            }
        }
//        groupcallAdapter.onLongPress = {Pos,RoomId,CreatedBy->
//            if(CreatedBy==userId!!.toInt()) {
//                AppReportDiloag.Builder(requireContext())
//                    .setTitle(getString(R.string.delete), getString(R.string.Delete_this_room))
//                    .setAction(getString(R.string.Cancel), getString(R.string.Delete))
//                    .setOkFunction {
//                        postion = Pos
//                        viewModel.onDeleteRoom(userId!!.toInt(), RoomId)
//                    }
//                    .build()
//            }
//        }

        binding.searchGroup.setOnClickListener{
            isResumeCallList=true
            startActivity(Intent(requireContext(), GroupCallUserListActivity::class.java))
        }
        binding.createGroup.setOnClickListener{
            //mainActivityInterface?.onCreateGroup()
            mainActivityInterface?.onAddPost()
        }
        getRoomListAPI()
        //recycleViewScrollListner()
        observer()
        binding.apply {
            swapRefresh.setOnRefreshListener {
                swapRefresh.isRefreshing=true
                getRoomListAPI()
            }
        }
    }
    var postion=-1
    var roomId=-1
    private fun observer() {
        viewModel.response.observe(requireActivity(), Observer {
            if (listItem.isNotEmpty()) {
                listItem.clear()
            }
            /*if(it!=null) {
             try {
                 listItem.addAll(it.result)
                 groupcallAdapter.notifyDataSetChanged()
             }catch (e:Exception){e.printStackTrace()}
                *//*for (i in it.result.indices) {
                    listItem.add(it.result[i])
                }*//*
            }*/
            try {
                groupcallAdapter.updateList(it)
            }catch (e:Exception){e.printStackTrace()}
            binding.swapRefresh.isRefreshing=false
            //  groupcallAdapter.notifyDataSetChanged()
            /*offSet = if (it.result.isNotEmpty()) {
                +10
            } else {
                offSet
            }*/
            binding.llText.visibility=View.GONE
            binding.recyclviewgroup.visibility=View.VISIBLE
        })
        viewModel.errString.observe(requireActivity(), Observer { err: String ->
            try {
                showToastShort(requireContext(), err)
                binding.swapRefresh.isRefreshing=false
                binding.llText.visibility=View.VISIBLE
                binding.recyclviewgroup.visibility=View.GONE
            }catch (e:Exception){e.printStackTrace()}
        })
        viewModel.errRes.observe(requireActivity(), Observer { err: Int ->
            try{
                if(err==200){
                    groupcallAdapter.onDeleteItem(postion)
                }else {
                    if (err != null) showToastShort(requireContext(), getString(err))
                }
                binding.swapRefresh.isRefreshing=false
                binding.llText.visibility=View.VISIBLE
                binding.recyclviewgroup.visibility=View.GONE
            }catch (e:Exception){e.printStackTrace()}
        })
        viewModel.successAddToUser.observe(requireActivity(), Observer { err: Int ->
            try {
                if (err != null && roomId != -1) {
                    isResumeCallList=true
                    startActivity(Intent(requireContext(), AgoraMainActivity::class.java).apply {
                        putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, roomId.toString())
                    })
                }
            }catch (e:Exception){e.printStackTrace()}
        })
    }

    override fun onResume() {
        super.onResume()
        if(isResumeCallList) {
            isResumeCallList=false
            getRoomListAPI()
        }
    }
    private fun getRoomListAPI() {
        if (isNetworkAvailable(requireContext())) {
            userId?.let { it ->
                viewModel.getRoomListAPI(userID = it,offSet.toString())
            }
        }
    }
    private fun recycleViewScrollListner() {
        binding.recyclviewgroup.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            getRoomListAPI()
                            loading = true
                        }
                    }
                }
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GroupCallList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GroupCallList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}