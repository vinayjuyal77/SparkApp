package com.app.spark.activity.personal_info

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityPersonalInformationBinding
import com.app.spark.dialogs.PersonalDoYouDialog
import com.app.spark.interfaces.DoYouDialogListener
import com.app.spark.utils.*
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class ActivityPersonalInformation : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityPersonalInformationBinding
    private lateinit var viewModel: PersonalInfoViewModel
    private var popupWindow: ListPopupWindow? = null
    private val questionList = mutableListOf("Have a Pet", "Drink", "Smoke")
    private val answerList = mutableListOf("no", "no", "no")
    private val hobbiesList = mutableListOf<String>()
    private val interestList = mutableListOf<String>()
    private val tagsList = mutableListOf<String>()
    lateinit var pref: SharedPrefrencesManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_personal_information)
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                PersonalInfoViewModel::class.java
            )
        pref = SharedPrefrencesManager.getInstance(this)
        viewModel.setUserDetail(
            pref.getString(PrefConstant.ACCESS_TOKEN, "")!!,
            pref.getString(PrefConstant.USER_ID, "")!!
        )
        setClickListeners()
        observeViewModel()
    }

    private fun setClickListeners() {
        binding.llCalendar.setOnClickListener(this)
        binding.llRelation.setOnClickListener(this)
        binding.llAddMore.setOnClickListener(this)
        binding.tvSave.setOnClickListener(this)
        binding.chipAdd.setOnClickListener(this)
        binding.chipAddInterest.setOnClickListener(this)
        binding.chipAddTags.setOnClickListener(this)
        setRadioButtonListener()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.llCalendar -> {
                pickDate(this, binding.txtBirthdayInput)
            }
            binding.llRelation -> {
                showRelationDropDown()
            }
            binding.chipAdd -> {
                addTextToChips(getString(R.string.hobbies), binding.chipHobbies, hobbiesList)
            }
            binding.chipAddInterest -> {
                addTextToChips(getString(R.string.interest), binding.chipInterest, interestList)
            }
            binding.chipAddTags -> {
                addTextToChips(getString(R.string.tags), binding.chipTags, tagsList)
            }
            binding.llAddMore -> {
                PersonalDoYouDialog(this, object : DoYouDialogListener {
                    override fun addDoYou(question: String, answer: String) {
                        questionList.add(question)
                        answerList.add(answer)
                    }

                }).show()
            }
            binding.tvSave -> {
                binding.progressBar.visibility = View.VISIBLE
                viewModel.updatePersonalInfoAPI(
                    binding.edtNickName.text.toString(),
                    binding.edtAbout.text.toString(),
                    binding.txtRelationShipStatus.text.toString(),
                    binding.txtBirthdayInput.text.toString(),
                    hobbiesList, interestList, tagsList, questionList, answerList
                )
            }
        }
    }

    private fun observeViewModel() {
        viewModel.response.observe(this, { res: String? ->
            hideView(binding.progressBar)
            if (!res.isNullOrEmpty()) {
                showToastShort(this, res)
            }
        })

        viewModel.errString.observe(this, { err: String? ->
            hideView(binding.progressBar)
            if (err != null)
                showToastShort(this, err)
        })

        viewModel.errRes.observe(this, { err: Int ->
            binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }

    private fun setRadioButtonListener() {
        binding.rbPetYes.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                answerList[0] = "yes"
            }
        }
        binding.rbPetNo.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                answerList[0] = "no"
            }
        }
        binding.rbDrinkYes.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                answerList[1] = "yes"
            }
        }
        binding.rbDrinkNo.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                answerList[1] = "no"
            }
        }
        binding.rbSmokeYes.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                answerList[2] = "yes"
            }
        }
        binding.rbSmokeNo.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                answerList[2] = "no"
            }
        }
    }

    private fun showRelationDropDown() {
        popupWindow = ListPopupWindow(this)
        val arrayList = listOf(
            "Single",
            "In a relationship",
            "Engaged",
            "Married",
            "Separated",
            "Divorced",
            "Widow",
            "Its Complicated"
        )
        val adapted = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            arrayList
        )
        popupWindow?.setAdapter(adapted)
        popupWindow?.setOnItemClickListener { parent, view, position, id ->
            binding.txtRelationShipStatus.text = arrayList[position]
            popupWindow?.dismiss()
        }
        popupWindow?.width = binding.llRelation.width
        popupWindow?.anchorView = binding.llRelation
        popupWindow?.setDropDownGravity(Gravity.BOTTOM)
        popupWindow?.show()
    }

    private fun addTextToChips(title: String, chipGroup: ChipGroup, list: MutableList<String>) {

        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        lp.setMargins(30,0,30,0)
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