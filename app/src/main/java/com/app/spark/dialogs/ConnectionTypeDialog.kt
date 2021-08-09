package com.app.spark.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import com.app.spark.R
import com.app.spark.databinding.DialogSelectConnectionTypeBinding
import com.app.spark.databinding.DialogSelectUserConnectionTypeBinding
import com.app.spark.interfaces.OnConnectionTypeSelected
import kotlinx.android.synthetic.main.dialog_gender.*

import java.util.*


class ConnectionTypeDialog(
    private val isPublicSelected:Boolean,
    private val ctx: Context,
    private val onConnectionTypeSelected: OnConnectionTypeSelected
) : Dialog(ctx) {

    private lateinit var binding: DialogSelectUserConnectionTypeBinding
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_select_user_connection_type, null, false)
        setContentView(binding.root)
        window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        onRadioListener()
        binding.rbPublic.isChecked = isPublicSelected
    }

    private fun onRadioListener() {
        binding.radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val radioButton = radioGroup.findViewById<RadioButton>(i)
            onConnectionTypeSelected.onSelectedConnection(radioButton.text.toString())
            dismiss()
        }
    }

}
