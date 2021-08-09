package com.app.spark.fragment.groupcall

import android.app.Dialog
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.spark.R
import com.app.spark.activity.reciver.ACTION_ROOM
import com.app.spark.activity.reciver.ADD_REQUEST
import com.app.spark.activity.reciver.EXTRA_ROOM_TYPE
import com.app.spark.constants.AppConstants
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.RaiseHandBottomFragmentBinding
import com.app.spark.fragment.groupcall.adapter.RaiseHandListAdapter
import com.app.spark.fragment.groupcall.callback.RaiseHandCallbackListner
import com.app.spark.fragment.groupcall.view_model.RaiseHandViewModel
import com.app.spark.models.RaiseHandListResponse
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.showToastShort
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RaiseHandListBottomFragment(var roomId:Int) : BottomSheetDialogFragment(), View.OnClickListener{
    lateinit var binding: RaiseHandBottomFragmentBinding
    var bundle: Bundle? = null
    private var behavior: BottomSheetBehavior<View>? = null
    private lateinit var viewModel: RaiseHandViewModel
    lateinit var pref: SharedPrefrencesManager
    var userId: String? = null
    lateinit var listnerBackcall: RaiseHandCallbackListner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // for testing scenario fail we added
        bundle = arguments
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.raise_hand_bottom_fragment, container, false)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(RaiseHandViewModel::class.java)
        pref = SharedPrefrencesManager.getInstance(requireContext())
        userId = pref.getString(PrefConstant.USER_ID, "")
        setClickEvent()
        getApiRequestList()
        getObserver()
        viewModel.setInitialData(userId.toString(),roomId)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReciver,
            IntentFilter(ACTION_ROOM)
        )

        return binding.root
    }
    var isLoding=true
    private val broadcastReciver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent!!.getStringExtra(EXTRA_ROOM_TYPE)){
                ADD_REQUEST->{
                    if(isLoding) {
                        isLoding=false
                        getApiRequestList()
                    }
                }
            }
        }
    }

    private fun getObserver() {
        viewModel.activeSuccess.observe(requireActivity(), {
            try {
                if (listItem.isNotEmpty()) {
                    listItem.clear()
                }
                if (it != null) {
                    listItem.addAll(it)
                    raiseHandListAdapter.notifyDataSetChanged()
                }
                binding.tvEmpty.visibility = View.GONE
                binding.llProgressbar.visibility = View.GONE
                isLoding = true
            }catch (e:Exception){e.printStackTrace()}
        })
        viewModel.errString.observe(requireActivity(), Observer { err: String ->
            try {
                if (!err.isNullOrEmpty()) {
                    binding.rvRaiseHandList.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text = err
                }
                binding.llProgressbar.visibility = View.GONE
                isLoding = true
                //showToastShort(requireContext(), err)
            } catch (e:Exception){e.printStackTrace()}
        })
        viewModel.errRes.observe(requireActivity(), Observer { err: Int ->
            try {
                binding.tvEmpty.visibility=View.GONE
                if(err==200){
                    getApiRequestList()
                }else if (err != null) {
                    showToastShort(requireContext(), getString(err))
                }
                binding.llProgressbar.visibility=View.GONE
                isLoding=true
            }catch (e:Exception){e.printStackTrace()}
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        AppConstants.BundleConstants.isVisible=true
        listnerBackcall.onRaiseHandCallbackClick()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { setupBottomSheet(it) }
        //Toast.makeText(requireContext(),""+roomId+"-"+userIds,Toast.LENGTH_LONG).show()
        return dialog
    }

    private fun setClickEvent() {
        binding.llLeave.setOnClickListener {
            dismiss()
        }
    }

    private fun getApiRequestList() {
        viewModel.getRequestList(roomId,userId!!.toInt())
    }

    private fun setupBottomSheet(dialogInterface: DialogInterface) {
        val bottomSheetDialog = dialogInterface as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )
            ?: return
        bottomSheet.setBackgroundResource(R.drawable.bg_bottom_dialog)
        behavior = BottomSheetBehavior.from(bottomSheet)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intilize()
        listnerBackcall=requireActivity() as RaiseHandCallbackListner
    }
    private lateinit var raiseHandListAdapter: RaiseHandListAdapter
    private var listItem : ArrayList<RaiseHandListResponse.ActiveUser> = ArrayList()
    private fun intilize() {
        raiseHandListAdapter= RaiseHandListAdapter(requireContext(),listItem)
        binding.rvRaiseHandList.layoutManager = LinearLayoutManager(activity,
            LinearLayoutManager.VERTICAL, false).apply {
            scrollToPosition(0)
        }
        binding.rvRaiseHandList.adapter = raiseHandListAdapter
        raiseHandListAdapter.onItemAllowListener={ Pos,Id,RoomId,Useridd->
            binding.llProgressbar.visibility=View.VISIBLE
            viewModel.allowRoomRequest(Id,RoomId,Useridd)
        }
    }

    override fun onClick(p0: View?) {
        when (p0) {
            /*binding.tvGuideBook -> {
            }*/
        }
    }

}