package com.app.spark.dialogs

import android.app.Dialog
import android.content.Context
import android.view.*
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.constants.AppConstants
import com.app.spark.constants.AppConstants.Initilize.MAKE_LISTENERS_TYPE
import com.app.spark.constants.AppConstants.Initilize.MAKE_SPEAKERS_TYPE
import com.app.spark.constants.AppConstants.Initilize.MESSAGE_TYPE
import com.app.spark.constants.AppConstants.Initilize.REMOVE_TYPE
import com.app.spark.constants.AppConstants.Initilize.REPORT_TYPE
import com.app.spark.databinding.DialogChatReportBinding
import com.app.spark.databinding.DiloagSpeakersBinding

class SpeakersDiloag private constructor(
    type: Int?,
    okFunc: (type: String) -> Unit = {},
    context: Context
) {
    private val binding:DiloagSpeakersBinding
    init {
        val dialog = Dialog(context, R.style.DialogSlideAnim)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window!!.attributes.gravity = Gravity.BOTTOM }
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.diloag_speakers, null, false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        if (dialog.window != null)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        if(type==1) binding.tvMakeListeners.visibility=View.VISIBLE
        else if(type==2) binding.tvMakeSpeakers.visibility=View.VISIBLE
        if(type==3){
            binding.llSpeaker.visibility=View.GONE
            binding.llListener.visibility=View.VISIBLE
        }
        binding.tvMessage.setOnClickListener {
            dialog.dismiss()
            okFunc(MESSAGE_TYPE)
        }
        binding.tvRemove.setOnClickListener {
            dialog.dismiss()
            okFunc(REMOVE_TYPE)
        }
        binding.tvReport.setOnClickListener {
            dialog.dismiss()
            okFunc(REPORT_TYPE)
        }
        binding.tvMakeListeners.setOnClickListener {
            dialog.dismiss()
            okFunc(MAKE_LISTENERS_TYPE)
        }
        binding.tvMakeSpeakers.setOnClickListener {
            dialog.dismiss()
            okFunc(MAKE_SPEAKERS_TYPE)
        }
        binding.tvMessageList.setOnClickListener {
            dialog.dismiss()
            okFunc(MESSAGE_TYPE)
        }
        binding.tvReportList.setOnClickListener {
            dialog.dismiss()
            okFunc(REPORT_TYPE)
        }

        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }
    class Builder(con: Context?) {
        private var context = con
        private var okFunc :(type: String) -> Unit = {}
        private var type: Int? = null
        fun setType(type: Int) = apply { this.type = type }
        fun setOkFunction(func: (type: String) -> Unit) = apply { this.okFunc = func }
        fun build() = SpeakersDiloag(type,okFunc,context!!)
    }
}