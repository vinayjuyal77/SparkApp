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
import com.app.spark.databinding.*
import com.app.spark.interfaces.ChatProfileDialogListner
import com.bumptech.glide.Glide


class ChatProfileDialog(
    private val ctx: Context,
    private val url: String,
    private val name: String,
    private val chatProfileDialogListner: ChatProfileDialogListner
) : Dialog(ctx) {

    private lateinit var binding: DialogChatProfileBinding
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var selectedTime: String = "0"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_chat_profile, null, false)
        setContentView(binding.root)
        window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)
        setCanceledOnTouchOutside(false)
        setClickListener()
        binding.tvName.text = name
        Glide.with(context).load(url).placeholder(R.drawable.ic_profile).into(binding.imgProfilePic)
    }

    private fun setClickListener() {
        binding.imgChatMessage.setOnClickListener {
            chatProfileDialogListner.onChatMessage()
            dismiss()
        }
        binding.imgInfo.setOnClickListener {
            chatProfileDialogListner.onInfo()
            dismiss()
        }

    }
}
