package com.app.spark.activity.professional_info

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.app.spark.R
import com.app.spark.databinding.DialogProfessionalExperienceBinding
import com.app.spark.interfaces.AddExperienceEducationListener
import com.app.spark.utils.*
import kotlinx.android.synthetic.main.dialog_gender.*

import java.util.*


class AddExperienceDialog(
    private val ctx: Activity,
    private val professionalInfoViewModel: ProfessionalInfoViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val addExperienceEducationListener: AddExperienceEducationListener
) : Dialog(ctx) {

    private lateinit var binding: DialogProfessionalExperienceBinding
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_professional_experience, null, false)
        setContentView(binding.root)
        window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setListener()
        addWorkExpObserver()
    }

    private fun setListener() {
        binding.tvAdd.setOnClickListener {
            if (validateData()) {
                binding.tvAdd.setOnClickListener(null)
                binding.llCalendar.setOnClickListener(null)
                binding.llEndCalendar.setOnClickListener(null)
                addExperienceEducationListener.addExperienceEducation(
                    binding.etTitle.text.toString(), binding.etPlace.text.toString(),
                    binding.tvStartDate.text.toString(), binding.tvEndDate.text.toString()
                )
            }
        }
        binding.llCalendar.setOnClickListener {
            BaseActivity.hideKeyboard(ctx)
            pickCalDate(context, binding.tvStartDate)
        }
        binding.llEndCalendar.setOnClickListener {
            BaseActivity.hideKeyboard(ctx)
            pickCalDate(context, binding.tvEndDate)
        }
    }

    private fun addWorkExpObserver() {
        professionalInfoViewModel.workExpResponse.observe(lifecycleOwner, {
            if (it != null) {
                showToastShort(context,context.getString(R.string.experience_updated))
                setListener()
                binding.etTitle.text.clear()
                binding.etPlace.text.clear()
                binding.tvStartDate.text = ""
                binding.tvEndDate.text = ""
            }
        })
    }

    private fun validateData(): Boolean {
        when {
            binding.etTitle.text.toString().isEmpty() -> {
                showToastLong(context, ctx.getString(R.string.error_add_title))
                return false
            }
            binding.etPlace.text.toString().isEmpty() -> {
                showToastLong(context, ctx.getString(R.string.error_add_exp_place))
                return false
            }
            binding.tvStartDate.text.toString().isEmpty() -> {
                showToastLong(context, ctx.getString(R.string.error_add_exp_start_date))
                return false
            }
            binding.tvEndDate.text.toString().isEmpty() -> {
                showToastLong(context, ctx.getString(R.string.error_add_exp_end_date))
                return false
            }

            else -> return true
        }
    }

}
