package com.app.spark.fragment.groupcall

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.explore.ExploreViewModel
import com.app.spark.activity.gallery.GalleryActivity
import com.app.spark.constants.AppConstants
import com.app.spark.constants.AppConstants.BundleConstants.CREATE_ROOM
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.DialogCreateGroupVoiceBinding
import com.app.spark.fragment.groupcall.agora.AgoraMainActivity
import com.app.spark.fragment.groupcall.model.ConstantApp
import com.app.spark.models.CreateRoomResponse
import com.app.spark.photoeditor.Utils.Constant
import com.app.spark.utils.BaseFragment
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.showSnackBar
import com.app.spark.utils.showToastShort
import com.google.android.gms.common.internal.Constants


class CreateGroupCallBottomFragment : BottomSheetDialogFragment(), View.OnClickListener {
    lateinit var binding: DialogCreateGroupVoiceBinding
    var bundle: Bundle? = null
    private var behavior: BottomSheetBehavior<View>? = null
    private lateinit var viewModel: CreateGroupCallViewModel
    lateinit var pref: SharedPrefrencesManager
    var userId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // for testing scenario fail we added
        bundle = arguments
    }
    var room_type:Int=0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_create_group_voice, container, false)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(CreateGroupCallViewModel::class.java)
        pref = SharedPrefrencesManager.getInstance(requireContext())
        userId = pref.getString(PrefConstant.USER_ID, "")
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
        setClickListener()
        viewModel.errString.observe(requireActivity(),  { err: String ->
            binding.progressBar.visibility=View.GONE
            try {
                showToastShort(requireContext(), err)
            }catch (e:Exception){e.printStackTrace()}
        })
        viewModel.errRes.observe(requireActivity(), { err: Int ->
            binding.progressBar.visibility=View.GONE
            try{
            if (err != null) showToastShort(requireContext(), getString(err))
            }catch (e:Exception){e.printStackTrace()}
        })
        viewModel.roomSuccess.observe(this, { response: CreateRoomResponse ->
            if (response.statusCode == 300) {
                binding.progressBar.visibility=View.GONE
                dismiss()
                startActivity(Intent(requireContext(), AgoraMainActivity::class.java).apply {
                    putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME,response.result.toString())
                })
            }
        })

        viewModel.roomAddToGuest.observe(this, { response: CreateRoomResponse ->
            if (response.statusCode == 300) {
                binding.progressBar.visibility=View.GONE
                dismiss()
                startActivity(Intent(requireContext(), AddUserActivity::class.java).apply {
                    putExtra(AppConstants.BundleConstants.ROOM_ID,response.result.toString())
                })
                //  action=CREATE_ROOM
            }
        })

    }

    private fun setClickListener() {
        binding.addGuest.setOnClickListener(this)
        binding.letsgo.setOnClickListener(this)
        binding.llSocial.setOnClickListener(this)
        binding.llPrivate.setOnClickListener(this)
    }
    override fun onClick(p0: View?) {
        when (p0) {
            binding.addGuest -> {
                if(validateFileds()) {
                    /*dismiss()
                    startActivity(Intent(requireContext(), AddUserActivity::class.java).apply {
                        action=CREATE_ROOM
                        putExtra(AppConstants.BundleConstants.ROOM_TITLE,binding.etRoomTitle.text.toString().trim())
                        putExtra(AppConstants.BundleConstants.ROOM_DESC,binding.etRoomDescription.text.toString().trim())
                        putExtra(AppConstants.BundleConstants.ROOM_TYPE,room_type)
                    })*/
                    binding.progressBar.visibility=View.VISIBLE
                    viewModel.setCreateRoom(
                        userId!!.toInt(), binding.etRoomTitle.text.toString().trim(),
                        binding.etRoomDescription.text.toString().trim(), room_type,true
                    )

                }
            }
            binding.letsgo -> {
                if(validateFileds()) {
                    binding.progressBar.visibility=View.VISIBLE
                    viewModel.setCreateRoom(
                        userId!!.toInt(), binding.etRoomTitle.text.toString().trim(),
                        binding.etRoomDescription.text.toString().trim(), room_type,false
                    )
                }
            }
            binding.llSocial -> {
                room_type=0
                binding.llSocial.setBackgroundResource(R.drawable.bg_green_storke_black_solid)
                binding.llPrivate.setBackgroundResource(R.drawable.bg_theme_gray_solid)
                binding.ivNormal.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_social_group__green))
                binding.ivAdult.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_private_room_white))
            }
            binding.llPrivate -> {
                room_type=1
                binding.llSocial.setBackgroundResource(R.drawable.bg_theme_gray_solid)
                binding.llPrivate.setBackgroundResource(R.drawable.bg_green_storke_black_solid)
                binding.ivNormal.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_social_group_white))
                binding.ivAdult.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_private_room_green))
            }
        }
    }

    fun validateFileds():Boolean{
        if(binding.etRoomTitle.text.toString().trim().isNullOrEmpty()){
            Toast.makeText(requireContext(),"Please enter Disscussion about game.",Toast.LENGTH_LONG).show()
            return  false
        }else if(binding.etRoomDescription.text.toString().trim().isNullOrEmpty()){
            Toast.makeText(requireContext(),"Please enter Add a Description.",Toast.LENGTH_LONG).show()
            return  false
        }else{
            return true
        }
        return false
    }

    private fun openGalleryIntent(postType: String) {
        val intent = Intent(context, GalleryActivity::class.java)
        intent.putExtra(IntentConstant.POST_TYPE, postType)
        startActivity(intent)
        //startActivity(Intent(context, CameraXActivity::class.java))
        //startActivity(Intent(context, CameraCustom::class.java))

    }

}