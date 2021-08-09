package com.app.spark.dialogs

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.DialogAppReportBinding
import com.app.spark.databinding.DialogRiseHandBinding

class AppReportDiloag private constructor(
    title: String?,
    description:String?,
    cancel:String?,
    done:String?,
    okFunc: () -> Unit = {},
    context: Context
) {
    private val binding: DialogAppReportBinding
    init {
        val dialog = Dialog(context, R.style.DialogSlideAnim)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window!!.attributes.gravity = Gravity.CENTER }

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_app_report, null, false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        if (dialog.window != null)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        if(!title.isNullOrEmpty()) binding.tvTitle.text=title
        if(!description.isNullOrEmpty()) binding.tvdesc.text=description
        if(!cancel.isNullOrEmpty()) binding.tvCancel.text=cancel
        if(!done.isNullOrEmpty()) binding.tvDone.text=done
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
        private var title: String? = null
        private var description: String? = null
        private var cancel: String? = null
        private var done: String? = null
        fun setTitle(title: String,description: String) = apply { this.title = title
            this.description = description}
        fun setAction(cancel: String,done: String) = apply { this.cancel = cancel
            this.done = done}
        fun setOkFunction(func: () -> Unit) = apply { this.okFunc = func }
        fun build() = AppReportDiloag(title,description,cancel,done,okFunc,context!!)
    }
}