package com.app.spark.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.*
import com.app.spark.interfaces.OnConnectionTypeSelected
import com.app.spark.interfaces.ReportPostDialogListner
import com.app.spark.interfaces.SimpleDialogListner


class ReportChatDialog(
    private val ctx: Context,
    private val buttonDrawable: Int,
    private val onConnectionTypeSelected: OnConnectionTypeSelected
) : Dialog(ctx) {

    private lateinit var binding: DialogReportChatBinding
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var selectedTime: String = "0"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_report_chat, null, false)
        setContentView(binding.root)
        window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)
        setCanceledOnTouchOutside(false)
        binding.tvOk.setBackgroundResource(buttonDrawable)
        setClickListener()
    }

    private fun setClickListener(){
        binding.tvOk.setOnClickListener {
            onConnectionTypeSelected.onSelectedConnection(selectedTime)
            dismiss()
        }
        binding.tvCancel.setOnClickListener { dismiss() }
        binding.cbDeleteMedia.setOnCheckedChangeListener { _, b ->
            if (b){
                selectedTime="1"
            }else selectedTime="0"
        }

    }
}
