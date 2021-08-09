package com.app.spark.dialogs

import android.app.Dialog
import android.content.Context
import android.view.*
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.DialogYourAgeBinding


class YourAgeDiloag private constructor(
    isSingleCheck : Boolean,
    okCallback: (selectedList : String) -> Unit = {},
    context: Context
) {
    private lateinit var binding : DialogYourAgeBinding
    init {
        val dialog = Dialog(context,R.style.DialogSlideAnim)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window!!.attributes.gravity = Gravity.BOTTOM
        }
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_your_age, null, false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)

        binding.npAge.value=14
        binding.npAge.apply { minValue=14
            maxValue=60
        }
        binding.ivClose.setOnClickListener { dialog.dismiss() }
        binding.done.setOnClickListener {
            dialog.dismiss()
            okCallback(binding.npAge.value.toString())
        }
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }
    class Builder(con: Context) {
        private var context = con
        private var isSingleCheck : Boolean = false
        private var okCallback: (selectedList : String) -> Unit = {}

        fun setSingleCheck(b : Boolean) = apply { this.isSingleCheck = b }
        fun setCallback(func: (selectedItemList : String) -> Unit) = apply { this.okCallback = func }
        fun build() = YourAgeDiloag(isSingleCheck,okCallback,context)
    }
}