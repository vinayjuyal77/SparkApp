package com.app.spark.fragment.groupcall

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spark.R
import com.app.spark.databinding.FragmentGroupCallListBinding
import com.app.spark.databinding.FragmentSearchGroupCallListBinding
import com.app.spark.fragment.groupcall.adapter.GroupListAdapter
import com.app.spark.models.GetRoomResponse
import com.app.spark.models.ResultFollowing
import com.app.spark.utils.BaseFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupCallList.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupSearchCallList : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var  layoutManager: LinearLayoutManager
    lateinit var groupcallAdapter: GroupListAdapter
    lateinit var binding : FragmentSearchGroupCallListBinding
    lateinit var viewModel : GroupCallListViewModel
    private var listItem : ArrayList<GetRoomResponse.UserData> = ArrayList()

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_group_call_list, container, false)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(GroupCallListViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false )
        binding.recyclviewgroup.layoutManager = layoutManager
        groupcallAdapter  = GroupListAdapter(requireContext(),listItem)
        binding.recyclviewgroup.adapter = groupcallAdapter
        groupcallAdapter.notifyDataSetChanged()

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
            GroupSearchCallList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}