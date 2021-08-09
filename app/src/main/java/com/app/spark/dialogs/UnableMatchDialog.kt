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

class UnableMatchDialog private constructor(
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

        fun setOkFunction(func: () -> Unit) = apply { this.okFunc = func }
        fun build() = UnableMatchDialog(okFunc,context!!)
    }
}