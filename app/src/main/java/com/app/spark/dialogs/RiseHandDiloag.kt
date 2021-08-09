package com.app.spark.dialogs

import android.app.Dialog
import android.content.Context
import android.view.*
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.DialogRiseHandBinding
import com.app.spark.databinding.DiloagSpeakersBinding

class RiseHandDiloag private constructor(
    okFunc: () -> Unit = {},
    context: Context
) {
    private val binding: DialogRiseHandBinding
    init {
        val dialog = Dialog(context, R.style.DialogSlideAnim)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window!!.attributes.gravity = Gravity.CENTER }
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_rise_hand, null, false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        if (dialog.window != null)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        binding.tvCancel.setOnClickListener {
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
        fun build() = RiseHandDiloag(okFunc,context!!)
    }
}