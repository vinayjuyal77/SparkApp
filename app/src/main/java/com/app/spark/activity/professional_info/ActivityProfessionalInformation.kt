package com.app.spark.activity.professional_info

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityProfessionalInformationBinding
import com.app.spark.interfaces.AddExperienceEducationListener
import com.app.spark.models.AddEducationResponse
import com.app.spark.models.AddWorkExpResponse
import com.app.spark.utils.BaseActivity
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.showSnackBar
import com.app.spark.utils.showToastShort
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ActivityProfessionalInformation : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityProfessionalInformationBinding
    lateinit var viewModel: ProfessionalInfoViewModel
    lateinit var pref: SharedPrefrencesManager
    private val skillsList = mutableListOf<String>()
    private val interestList = mutableListOf<String>()
    private val tagsList = mutableListOf<String>()
    private val experienceList = mutableListOf<AddWorkExpResponse.Result>()
    private val educationList = mutableListOf<AddEducationResponse.Result>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_professional_information)
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                ProfessionalInfoViewModel::class.java
            )
        pref = SharedPrefrencesManager.getInstance(this)
        viewModel.setUserDetail(
            pref.getString(PrefConstant.ACCESS_TOKEN, "")!!,
            pref.getString(PrefConstant.USER_ID, "")!!
        )
        setClickListeners()
        observeResponse()
    }

    private fun setClickListeners() {
        binding.txtEducationInput.setOnClickListener(this)
        binding.txtExperienceInput.setOnClickListener(this)
        binding.chipAdd.setOnClickListener(this)
        binding.chipAddInterest.setOnClickListener(this)
        binding.chipAddTags.setOnClickListener(this)
        binding.tvSave.setOnClickListener(this)
    }

    private fun observeResponse() {
        viewModel.response.observe(this, { res: String? ->
            hideView(binding.progressBar)
            if (!res.isNullOrEmpty()) {
                showToastShort(this, res)
            }
        })
        viewModel.workExpResponse.observe(this, {
            hideView(binding.progressBar)
            if (it != null) {
                experienceList.add(it)
            }
        })
        viewModel.educationResponse.observe(this, {
            hideView(binding.progressBar)
            if (it != null) {
                educationList.add(it)
            }
        })
        viewModel.errString.observe(this, { err: String ->
            hideView(binding.progressBar)
            showToastShort(this, err)
        })

        viewModel.errRes.observe(this, { err: Int ->
            binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.txtEducationInput -> {
                AddEducationDialog(this, viewModel, this, object : AddExperienceEducationListener {
                    override fun addExperienceEducation(
                        title: String,
                        place: String,
                        startDate: String,
                        endDate: String
                    ) {
                        viewModel.addEducation(title, place, startDate, endDate)
                    }

                }).show()

            }
            binding.txtExperienceInput -> {
                AddExperienceDialog(this, viewModel, this, object : AddExperienceEducationListener {
                    override fun addExperienceEducation(
                        title: String,
                        place: String,
                        startDate: String,
                        endDate: String
                    ) {
                        viewModel.addWorkExperience(title, place, startDate, endDate)
                    }

                }).show()

            }
            binding.chipAdd -> {
                addTextToChips(getString(R.string.hobbies), binding.chipSkills, skillsList)
            }
            binding.chipAddInterest -> {
                addTextToChips(getString(R.string.interest), binding.chipInterest, interestList)
            }
            binding.chipAddTags -> {
                addTextToChips(getString(R.string.tags), binding.chipTags, tagsList)
            }

            binding.tvSave -> {
                binding.progressBar.visibility = View.VISIBLE
                viewModel.updateProfessionalInfoAPI(
                    binding.edtNickName.text.trim().toString(),
                    binding.edtAbout.text.trim().toString(),
                    binding.edtContact.text.trim().toString(),
                    interestList,
                    skillsList,
                    tagsList,
                    experienceList,
                    educationList
                )
            }
        }
    }

    private fun addTextToChips(title: String, chipGroup: ChipGroup, list: MutableList<String>) {

        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        lp.setMargins(30, 0, 30, 0)
        input.layoutParams = lp
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this).setTitle(title)
            .setView(input)
            .setPositiveButton(getString(R.string.add)) { dialogInterface: DialogInterface, i: Int ->
                val chip = Chip(this)
                chip.text = input.text.toString()
                chip.closeIcon = ContextCompat.getDrawable(this, R.drawable.ic_remove_cross)
                chip.isCloseIconVisible = true
                chip.setTextAppearanceResource(R.style.ChipTextAppearance)
                chipGroup.addView(chip, chipGroup.size - 1)
                list.add(chip.text.toString())
                chip.setOnCloseIconClickListener {
                    chipGroup.removeView(chip)
                    list.remove(chip.text.toString())
                }
            }
        alertDialog.create()
        alertDialog.show()
    }

}