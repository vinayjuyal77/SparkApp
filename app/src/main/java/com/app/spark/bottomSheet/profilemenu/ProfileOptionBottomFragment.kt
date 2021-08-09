package com.app.spark.bottomSheet.profilemenu

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.UsersProfileActivity
import com.app.spark.activity.chatroom.ChatRoomActivity
import com.app.spark.activity.main.MainActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.DialogProfileMenuBinding
import com.app.spark.dialogs.ProfileConnectionTypeDialog
import com.app.spark.dialogs.SimpleCustomDialog
import com.app.spark.interfaces.ProfileOnConnectionTypeSelected
import com.app.spark.interfaces.SimpleDialogListner
import com.app.spark.utils.isNetworkAvailable
import com.app.spark.utils.showToastLong
import com.app.spark.utils.SharedPrefrencesManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_create_chat_group.*

class ProfileOptionBottomFragment : BottomSheetDialogFragment(), View.OnClickListener,
    ProfileOnConnectionTypeSelected {
    lateinit var binding: DialogProfileMenuBinding
    var bundle: Bundle? = null
    private var otherProfileId = ""
    private var otherProfileName = ""
    private var otherProfileImage = ""
    private var chatType = 2
    private var token = ""
    private var isFollowers = ""
    private var followerGroup = ""
    private var isConnectionFrom=""
    private var behavior: BottomSheetBehavior<View>? = null
    private lateinit var viewModel: ProfileOptionViewModel
    private lateinit var pref: SharedPrefrencesManager


    
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
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_profile_menu, container, false)
        viewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(ProfileOptionViewModel::class.java)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { setupBottomSheet(it) }
        return dialog
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
        if (bundle != null) {
            otherProfileId = bundle!!.getString(IntentConstant.PROFILE_ID, "")
            otherProfileName = bundle!!.getString(IntentConstant.CHAT_NAME, "")
            otherProfileImage = bundle!!.getString(IntentConstant.CHAT_IMG, "")
            chatType = bundle!!.getInt(IntentConstant.CHAT_TYPE, 2)
            isFollowers = bundle!!.getString(IntentConstant.IS_FOLLOWERS, "")
            followerGroup=bundle!!.getString(IntentConstant.FOLLOWING_GROUP,"")
            isConnectionFrom=bundle!!.getString(IntentConstant.ISPAGE_THROUGH,"")
        }
        pref = SharedPrefrencesManager.getInstance(requireContext())
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "").toString()
        setClickListener()
        observer()
    }

    private fun observer() {
        viewModel.responseReport.observe(viewLifecycleOwner, Observer {
            showToastLong(requireActivity(), it.APICODERESULT)
            dismiss()
        })
        viewModel.responseBlock.observe(viewLifecycleOwner, Observer {
            showToastLong(requireActivity(), it.APICODERESULT)
            dismiss()
        })

        viewModel.responseConnectionType.observe(viewLifecycleOwner, Observer {
            showToastLong(requireActivity(), it.APICODERESULT)
            dismiss()
        })

        viewModel.errString.observe(viewLifecycleOwner, Observer { err: String ->
            // binding.progressBar.visibility = View.GONE
            if (!err.isNullOrEmpty())
                showToastLong(requireContext(), err)
        })
    }

    private fun setClickListener() {
        binding.tvSendMessage.setOnClickListener(this)
        binding.tvReport.setOnClickListener(this)
        binding.tvChangeConnection.setOnClickListener(this)
        binding.tvBlockUser.setOnClickListener(this)
        binding.tvCopyUrl.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.tvSendMessage -> {
                    dismiss()
                    val intent = Intent(context, ChatRoomActivity::class.java)
                    intent.putExtra(IntentConstant.CHAT_ID,otherProfileId)
                    intent.putExtra(IntentConstant.CHAT_NAME,otherProfileName)
                    intent.putExtra(IntentConstant.CHAT_IMG, otherProfileImage)
                    intent.putExtra(IntentConstant.CHAT_TYPE, chatType)
                    startActivity(intent)
            }
            binding.tvReport -> {
                dismiss()
                var dialog = SimpleCustomDialog(
                    requireActivity(),
                    title = "Report",
                    desc = "Do you want to report this user?",
                    positiveBtnName = "Report",
                    onConnectionTypeSelected = object : SimpleDialogListner {
                        override fun submitSelected() {
                            if (isNetworkAvailable(requireActivity())) {
                                viewModel.reportUser(
                                    token,
                                    pref.getString(PrefConstant.USER_ID, toString())!!,
                                    otherProfileId
                                )

                            }
                        }
                    })
                dialog.show()
                dialog.setCancelable(true)
            }
            binding.tvChangeConnection -> {
                dismiss()
                if (isFollowers.trim().isNotEmpty() && isFollowers.equals("yes", true)) {
                    ProfileConnectionTypeDialog(followerGroup, requireActivity(), this).show()
                } else {
                    showToastLong(context, "This user is not follow you.")
                }
            }
            binding.tvBlockUser -> {
                dismiss()
                var dialog = SimpleCustomDialog(
                    requireActivity(),
                    title = "Blocked",
                    desc = "Do you want to block this user?",
                    positiveBtnName = "Block",
                    onConnectionTypeSelected = object : SimpleDialogListner {
                        override fun submitSelected() {
                            if (isNetworkAvailable(requireActivity())) {
                                viewModel.blockedUser(
                                    token,
                                    pref.getString(PrefConstant.USER_ID, toString())!!,
                                    otherProfileId,
                                    "block"
                                )
                            }
                        }
                    })
                dialog.show()
                dialog.setCancelable(true)
            }
            binding.tvCopyUrl -> {
                dismiss()
                Toast(requireContext()).showCustomToast ("Link copied")
            }
        }
    }

    fun Toast.showCustomToast(message: String)
    {
        val layout = requireActivity().layoutInflater.inflate (
            R.layout.custom_toast_layout,
            requireActivity().findViewById(R.id.toast_container)
        )
        // set the text of the TextView of the message
        val textView = layout.findViewById<TextView>(R.id.toast_text)
        textView.text = message
        // use the application extension function
        this.apply {
            setGravity(Gravity.CENTER, 0, 40)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
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
                    if (isAdded) {
                        viewModel.connectionTypeApi(
                            token = token,
                            follower_id = otherProfileId,
                            follow_group = myType.toLowerCase(),
                            user_id = pref.getString(PrefConstant.USER_ID, toString())!!
                        )
                    }
                    if(isConnectionFrom.trim().isNotEmpty()) {
                        val ins = Intent(requireActivity(), UsersProfileActivity::class.java)
                        ins.putExtra(IntentConstant.PROFILE_ID, otherProfileId)
                        startActivity(ins)
                        requireActivity().finish()
                    }
                }
            }
    }


}