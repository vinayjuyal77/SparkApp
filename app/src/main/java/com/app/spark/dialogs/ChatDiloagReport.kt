package com.app.spark.dialogs

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.DialogChatReportBinding

class ChatDiloagReport private constructor(
    textColor: Int?,
    okFunc: () -> Unit = {},
    context: Context) {
    private val binding: DialogChatReportBinding
    init {
        val dialog = Dialog(context,R.style.DialogSlideAnim)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setLayout(MATCH_PARENT, WRAP_CONTENT)
            dialog.window!!.attributes.gravity = Gravity.BOTTOM }
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_chat_report, null, false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        if (dialog.window != null)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        binding.reportAbusive.setTextColor(textColor!!)
        binding.tvCancel.setOnClickListener {
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
        private var textColor: Int? = null
        fun setColorText(textColor: Int) = apply { this.textColor = textColor }
        fun setOkFunction(func: () -> Unit) = apply { this.okFunc = func }
        fun build() = ChatDiloagReport(textColor,okFunc,context!!)
    }
}