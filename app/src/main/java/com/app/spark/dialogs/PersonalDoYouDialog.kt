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
import com.app.spark.databinding.DialogPersonalDoYouBinding
import com.app.spark.databinding.DialogReportPostBinding
import com.app.spark.interfaces.DoYouDialogListener
import com.app.spark.interfaces.ReportPostDialogListner
import com.app.spark.interfaces.SimpleDialogListner


class PersonalDoYouDialog(
    private val ctx: Context,
    private val onConnectionTypeSelected: DoYouDialogListener
) : Dialog(ctx) {

    private lateinit var binding: DialogPersonalDoYouBinding
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var selectedValue="no"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_personal_do_you, null, false)
        setContentView(binding.root)
        window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)
        setCanceledOnTouchOutside(false)
        onCheckedChangeListener()
        binding.tvAdd.setOnClickListener { dialogSubmit() }
    }

    fun dialogCancel() {
        dismiss()
    }

    fun dialogSubmit() {
        onConnectionTypeSelected.addDoYou(binding.etQuestion.text.toString(),selectedValue)
        dismiss()
    }

    private fun onCheckedChangeListener(){
        binding.rbYes.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                selectedValue= "yes"
            }
        }
        binding.rbNo.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                selectedValue= "no"
            }
        }
    }

}
