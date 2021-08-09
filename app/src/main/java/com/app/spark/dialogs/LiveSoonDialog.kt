package com.app.spark.dialogs

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.DialogUnableMatchBinding

class LiveSoonDialog private constructor(
    textTitle: String?,
    textBody:String?,
    okFunc: () -> Unit = {},
    context: Context
) {
    private val binding: DialogUnableMatchBinding
    init {
        val dialog = Dialog(context, R.style.DialogSlideAnim)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window!!.attributes.gravity = Gravity.CENTER }
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_unable_match, null, false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        if (dialog.window != null)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        if(!textTitle.isNullOrEmpty()) binding.tvTitle.text=textTitle.toString()
        if(!textBody.isNullOrEmpty()) binding.tvBody.text=textBody.toString()
        binding.tvOk.setOnClickListener {
            okFunc()
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
    class Builder(con: Context?) {
        private var context = con
        private var okFunc = {}
        private var textTitle: String? = null
        private var textBody: String? = null
        fun setTextTitle(textTitle: String) = apply { this.textTitle = textTitle }
        fun setTextBody(textBody: String) = apply { this.textBody = textBody }
        fun setOkFunction(func: () -> Unit) = apply { this.okFunc = func }
        fun build() = LiveSoonDialog(textTitle,textBody,okFunc,context!!)
    }
}