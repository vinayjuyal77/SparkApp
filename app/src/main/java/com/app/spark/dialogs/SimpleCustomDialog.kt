package com.app.spark.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.CustomSimpleDialogBinding
import com.app.spark.interfaces.SimpleDialogListner


class SimpleCustomDialog(
    private val ctx: Context,
    private val title:String,
    private val desc:String,
    private val positiveBtnName:String,
    private val onConnectionTypeSelected: SimpleDialogListner
) : Dialog(ctx) {

    private lateinit var binding: CustomSimpleDialogBinding
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.custom_simple_dialog, null, false)
        setContentView(binding.root)
        binding.dialog=this
        window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)
        setCanceledOnTouchOutside(false)
        binding.tvTitle.text=title
        binding.tvDesc.text=desc
        binding.tvReport.text=positiveBtnName
    }

    fun dialogCancel(){
        dismiss()
    }

    fun dialogSubmit(){
        onConnectionTypeSelected.submitSelected()
        dismiss()
    }

}
