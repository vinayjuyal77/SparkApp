package com.app.spark.activity.professional_info

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.app.spark.R
import com.app.spark.databinding.DialogEducationInfoBinding
import com.app.spark.databinding.DialogProfessionalExperienceBinding
import com.app.spark.interfaces.AddExperienceEducationListener
import com.app.spark.utils.pickCalDate
import com.app.spark.utils.pickDate
import com.app.spark.utils.showToastLong
import com.app.spark.utils.showToastShort
import kotlinx.android.synthetic.main.dialog_gender.*

import java.util.*


class AddEducationDialog(
    private val ctx: Context,
    private val professionalInfoViewModel: ProfessionalInfoViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val addExperienceEducationListener: AddExperienceEducationListener
) : Dialog(ctx) {

    private lateinit var binding: DialogEducationInfoBinding
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_education_info, null, false)
        setContentView(binding.root)
        window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setListener()
        addEducationObserver()
    }

    private fun setListener() {
        binding.tvAdd.setOnClickListener {
            if (validateData()) {
                addExperienceEducationListener.addExperienceEducation(
                    binding.etTitle.text.toString(), binding.etSchool.text.toString(),
                    binding.tvStartDate.text.toString(), binding.tvEndDate.text.toString()
                )
            }
        }

        binding.llCalendar.setOnClickListener {
            pickCalDate(context, binding.tvStartDate)
        }
        binding.llEndCalendar.setOnClickListener {
            pickCalDate(context, binding.tvEndDate)
        }
    }

    private fun addEducationObserver() {
        professionalInfoViewModel.educationResponse.observe(lifecycleOwner, {
            if (it != null) {
                showToastShort(context, context.getString(R.string.experience_updated))
                setListener()
                binding.etTitle.text.clear()
                binding.etSchool.text.clear()
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
            binding.etSchool.text.toString().isEmpty() -> {
                showToastLong(context, ctx.getString(R.string.error_add_school_college))
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
