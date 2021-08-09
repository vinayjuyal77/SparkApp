package com.app.spark.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
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
import com.app.spark.interfaces.OnConnectionTypeSelected
import kotlinx.android.synthetic.main.dialog_gender.*

import java.util.*


class CreateConnectionTypeDialog(
    private val selectionInt: Int,
    private val ctx: Context,
    private val onConnectionTypeSelected: OnConnectionTypeSelected,
    private val str: String
) : Dialog(ctx), DialogInterface.OnCancelListener {

    private lateinit var binding: DialogSelectConnectionTypeBinding
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var selectedPostType = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_select_connection_type, null, false)
        setContentView(binding.root)
        window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setOnCancelListener(this)
        when (selectionInt) {
            1 -> {
                binding.rbPublic.isChecked = true
            }
            2 -> {
                binding.rbProfessional.isChecked = true
            }
            3 -> {
                binding.rbPersonal.isChecked = true
            }
            4 -> {
                var v = str.split(",")
                for (i in v.indices) {
                    when {
                        v[i].equals("Public", true) -> {
                            binding.rbPublic.isChecked = true
                        }
                        v[i].equals("Professional", true) -> {
                            binding.rbProfessional.isChecked = true
                        }
                        v[i].equals("Personal", true) -> {
                            binding.rbPersonal.isChecked = true
                        }
                    }
                }

            }
        }

    }

    private fun setSelectedPostType() {
        if (binding.rbPersonal.isChecked) {
            selectedPostType = binding.rbPersonal.text.toString().toLowerCase(Locale.getDefault())
        }
        if (binding.rbProfessional.isChecked) {
            selectedPostType = if (selectedPostType.isNullOrEmpty())
                binding.rbProfessional.text.toString().toLowerCase(Locale.getDefault())
            else "$selectedPostType,${binding.rbProfessional.text.toString()
                .toLowerCase(Locale.getDefault())}"
        }
        if (binding.rbPublic.isChecked) {
            selectedPostType = if (selectedPostType.isNullOrEmpty())
                binding.rbPublic.text.toString().toLowerCase(Locale.getDefault())
            else "$selectedPostType,${binding.rbPublic.text.toString()
                .toLowerCase(Locale.getDefault())}"
        }
    }


    override fun onCancel(p0: DialogInterface?) {
        setSelectedPostType()
        onConnectionTypeSelected.onSelectedConnection(selectedPostType)
    }
}
