package com.app.spark.activity.change_password

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.NewLoginActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ActivityChangePasswordBinding
import com.app.spark.utils.*

class ChangePasswordActivity : BaseActivity() {

    lateinit var binding: ActivityChangePasswordBinding
    lateinit var viewModel: ChangePasswordViewModel
    var isShow = true
    var emailPhone = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password)
        if (intent.extras != null) {
            emailPhone = intent.getStringExtra(IntentConstant.EMAIL_PHONE).toString()
        }
        setClickListener()
        initlizeViewModel()
    }

    private fun initlizeViewModel() {

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                ChangePasswordViewModel::class.java
            )

        viewModel.response.observe(this, { err: String ->
            hideView(binding.progressBar)
            if (!err.isNullOrEmpty()) {
                clearAllgoToActivity(this, NewLoginActivity::class.java)
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

    private fun setClickListener() {
        binding.tvBack.setOnClickListener { finish() }
        binding.tvSubmit.setOnClickListener {
            if (isValidPassword()) {
                showView(binding.progressBar)
                viewModel.changePasswordAPI(emailPhone, binding.etPassword.text.toString().trim())
            }
        }
        binding.imgEyePassword.setOnClickListener {
            if (isShow) {
                isShow = false
                binding.imgEyePassword.setImageDrawable(resources.getDrawable(R.drawable.ic_visibility))
                binding.etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                binding.etPassword.setSelection(binding.etPassword.length())
            } else {
                isShow = true
                binding.imgEyePassword.setImageDrawable(resources.getDrawable(R.drawable.ic_close_eye))
                binding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                binding.etPassword.setSelection(binding.etPassword.length())

            }
        }
        binding.imgEyeCpassword.setOnClickListener {
            if (isShow) {
                isShow = false
                binding.imgEyeCpassword.setImageDrawable(resources.getDrawable(R.drawable.ic_visibility))
                binding.etConfirmPassword.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
                )
                binding.etConfirmPassword.setSelection(binding.etConfirmPassword.length())
            } else {
                isShow = true
                binding.imgEyeCpassword.setImageDrawable(resources.getDrawable(R.drawable.ic_close_eye))
                binding.etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                binding.etConfirmPassword.setSelection(binding.etConfirmPassword.length())
            }
        }
        binding.etPassword.setOnFocusChangeListener { view, b ->
            val editText = view as EditText
            if (b)
                editText.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_lock_active
                    ), null, null, null
                )
            else if (!b && editText.text.toString().isNullOrEmpty()) {
                editText.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_lock
                    ), null, null, null
                )
            }
        }
        binding.etConfirmPassword.setOnFocusChangeListener { view, b ->
            val editText = view as EditText
            if (b)
                editText.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_lock_active
                    ), null, null, null
                )
            else if (!b && editText.text.toString().isNullOrEmpty()) {
                editText.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_lock
                    ), null, null, null
                )
            }
        }
    }

    private fun isValidPassword(): Boolean {
        if (binding.etPassword.text.toString().isEmpty()) {
            showToastLong(this, getString(R.string.please_enter_password))
            return false
        } else if (binding.etPassword.text.toString().length < 8) {
            showToastLong(this, getString(R.string.minimum_length_password))
            return false
        }
//        else if (!AppValidator.validatePassword(binding.etPassword.text.toString())) {
//            showToastLong(this, getString(R.string.password_type_msg))
//            return false
//       }
       else if (binding.etConfirmPassword.text.toString().isEmpty()) {
            showToastLong(this, getString(R.string.please_enter_confirm_password))
            return false
        }
        else if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString()
        ) {
            showToastLong(this, getString(R.string.password_confirm_password_not_match))
            return false
        }
        return true

    }
}