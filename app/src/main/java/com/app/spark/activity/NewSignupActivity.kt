package com.app.spark.activity

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.main.MainActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityLoginNewBinding
import com.app.spark.databinding.ActivitySignupNewBinding
import com.app.spark.models.ImportantDataResponse
import com.app.spark.models.ImportantDataResult
import com.app.spark.utils.*
import com.app.spark.viewmodel.LoginSignUpViewModel
import com.google.gson.Gson

class NewSignupActivity : BaseActivity(), View.OnClickListener {
    lateinit var binding: ActivitySignupNewBinding
    var type = "login"
    var signUpType = "mobile"
    private lateinit var viewModel: LoginSignUpViewModel
    lateinit var pref: SharedPrefrencesManager
    var loginType = ""
    var isShow = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup_new)
        pref = SharedPrefrencesManager.getInstance(this)
        applyClickListener()
        initlizeViewModel()


        if (SharedPrefrencesManager.getInstance(this)
                .getBoolean(PrefConstant.IS_LOGIN, false)){
            val intent = Intent(this@NewSignupActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
//            val intent = Intent(this, NewLoginActivity::class.java)
//            startActivity(intent)
//            finish()
        }

    }

    private fun initlizeViewModel() {

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                LoginSignUpViewModel::class.java
            )
        viewModel.response.observe(this, { res: ImportantDataResult ->
            hideView(binding.progressBar)
            pref.setString(PrefConstant.USER_ID, res.user_id)
            val intent = Intent(this, OtpActivity::class.java)
            intent.putExtra(IntentConstant.SIGNUP_TYPE, signUpType)
            intent.putExtra(IntentConstant.EMAIL_PHONE, binding.etPhoneNo.text.toString())
            startActivity(intent)

        })
        viewModel.userNameAvailable.observe(this, {
            if (it) {
                binding.imgUserNameVerify.visibility = View.VISIBLE
            } else {
                binding.imgUserNameVerify.visibility = View.GONE
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

    private fun applyClickListener() {
        binding.btnLogin.setOnClickListener(this)
        binding.tvLogin.setOnClickListener(this)
        binding.imgEyeLoginpassword.setOnClickListener(this)
        binding.imgEyeConfirmPassword.setOnClickListener(this)
        userNameTextChange()
    }

    private fun userNameTextChange() {
        binding.etUserName.doAfterTextChanged {
            if (!it.isNullOrEmpty() && it.length >= 4) {
                viewModel.verifyUserNameAPI(it.toString())
            } else {
                binding.imgUserNameVerify.visibility = View.GONE
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.tvLogin -> {
                startActivity(Intent(this@NewSignupActivity, NewLoginActivity::class.java))
            }
            binding.imgEyeLoginpassword -> {
                if (isShow) {
                    isShow = false
                    binding.imgEyeLoginpassword.setImageDrawable(resources.getDrawable(R.drawable.ic_visibility))
                    binding.etPassword.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                    binding.etPassword.setSelection(binding.etPassword.length())
                } else {
                    isShow = true
                    binding.imgEyeLoginpassword.setImageDrawable(resources.getDrawable(R.drawable.ic_close_eye))
                    binding.etPassword.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                    binding.etPassword.setSelection(binding.etPassword.length())
                }
            }
            binding.imgEyeConfirmPassword -> {
                if (isShow) {
                    isShow = false
                    binding.imgEyeConfirmPassword.setImageDrawable(resources.getDrawable(R.drawable.ic_visibility))
                    binding.etConfirmPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance()
                    )
                    binding.etConfirmPassword.setSelection(binding.etConfirmPassword.length())
                } else {
                    isShow = true
                    binding.imgEyeConfirmPassword.setImageDrawable(resources.getDrawable(R.drawable.ic_close_eye))
                    binding.etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                    binding.etConfirmPassword.setSelection(binding.etConfirmPassword.length())
                }
            }
            binding.btnLogin -> {
                if (isValidSignUp()) {
                    if (isNetworkAvailable(this)) {
                        showView(binding.progressBar)
                        viewModel.signUpAPI(
                            binding.etUserName.text.toString(),
                            binding.etConfirmPassword.text.toString(),
                            signUpType,
                            binding.etPhoneNo.text.toString()
                        )
                    }
                }
            }
        }
    }

    private fun isValidSignUp(): Boolean {
        if (binding.etUserName.text.toString().isEmpty()) {
            showToastShort(this, getString(R.string.enter_user_name))
            return false
        } else if (binding.etUserName.text.toString().length < 4) {
            showToastShort(this, getString(R.string.username_length_validation))
            return false
        } else if (signUpType.equals("mobile") && !AppValidator.isValidMobile(
                this,
                binding.etPhoneNo,
                getString(R.string.enter_phone_number)
            )
        ) {
            return false
        } else if (binding.etPassword.text.toString().equals("")) {
            showToastLong(this, getString(R.string.please_enter_password))
            return false
        } else if (binding.etPassword.text.toString().length < 8) {
            showToastLong(this, getString(R.string.minimum_length_password))
            return false
        } /*else if (!AppValidator.validatePassword(binding.etPassword.text.toString())) {
            showToastLong(this, getString(R.string.password_type_msg))
            return false
        } */else if (binding.etConfirmPassword.text.toString().equals("")) {
            showToastLong(this, getString(R.string.please_enter_confirm_password))
            return false
        } else if (!binding.etPassword.text.toString()
                .equals(binding.etConfirmPassword.text.toString())
        ) {
            showToastLong(this, getString(R.string.password_confirm_password_not_match))
            return false
        } else {
            return true
        }
    }


}