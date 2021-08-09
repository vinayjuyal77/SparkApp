package com.app.spark.activity.explore

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.ExploreBottomSheetFragmentBinding
import com.app.spark.dialogs.AppReportDiloag
import com.app.spark.dialogs.ChangeConnectionTypeDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ExploreBottomSheetFragment: BottomSheetDialogFragment(), View.OnClickListener{
    lateinit var binding: ExploreBottomSheetFragmentBinding
    var bundle: Bundle? = null
    private var behavior: BottomSheetBehavior<View>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.explore_bottom_sheet_fragment, container, false)
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
    }
    private fun setClickListener() {
        binding.llSendMessage.setOnClickListener(this)
        binding.llReport.setOnClickListener(this)
        binding.llChangeConnection.setOnClickListener(this)
        binding.llCopyURL.setOnClickListener(this)
        binding.llBlockUser.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.llSendMessage -> {
                Toast.makeText(requireContext(),"Send Message",Toast.LENGTH_SHORT).show()
                dismiss()
            }
            binding.llReport -> {
                dismiss()
                AppReportDiloag.Builder(requireContext())
                    .setTitle("Report","Do you want to report this user?")
                    .setAction("Cancel","Report")
                    .setOkFunction {}
                    .build()
            }
            binding.llChangeConnection -> {
                dismiss()
                ChangeConnectionTypeDialog.Builder(requireContext())
                    .setOkFunction {}
                    .build()
            }
            binding.llCopyURL -> {
                dismiss()
                Toast(requireContext()).showCustomToast ("Link copied")
            }
            binding.llBlockUser -> {
                dismiss()
                AppReportDiloag.Builder(requireContext())
                    .setTitle("Block","Do you want to block this user?")
                    .setAction("Cancel","Block")
                    .setOkFunction {}
                    .build()
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


}