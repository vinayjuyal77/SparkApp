package com.app.spark.dialogs

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.DialogRelationshipStatusBinding

class RelationshipStatusDiloag private constructor(
    isSingleCheck : Boolean,
    okCallback: (selectedList : String) -> Unit = {},
    context: Context
) {
    private lateinit var binding : DialogRelationshipStatusBinding
    init {
        val dialog = Dialog(context)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window!!.attributes.gravity = Gravity.BOTTOM }
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_relationship_status, null, false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        binding.tvSingle.setOnClickListener { dialog.dismiss()
            okCallback(context.resources.getString(R.string.single))}
        binding.tvMarried.setOnClickListener { dialog.dismiss()
            okCallback(context.resources.getString(R.string.married))}
        binding.tvCommited.setOnClickListener {dialog.dismiss()
            okCallback(context.resources.getString(R.string.commited))}
        binding.tvComplicated.setOnClickListener { dialog.dismiss()
            okCallback(context.resources.getString(R.string.complicated))}
        binding.tvSearching.setOnClickListener {dialog.dismiss()
            okCallback(context.resources.getString(R.string.searching))}
        binding.tvDivorced.setOnClickListener { dialog.dismiss()
            okCallback(context.resources.getString(R.string.divorced))}
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
        fun build() = RelationshipStatusDiloag(isSingleCheck,okCallback,context)
    }
}