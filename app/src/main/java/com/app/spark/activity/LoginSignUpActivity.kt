package com.app.spark.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.main.MainActivity
import com.app.spark.databinding.ActivityLoginSignUpBinding
import com.app.spark.models.ImportantDataResponse
import com.app.spark.models.OtpResponse
import com.app.spark.utils.*
import com.app.spark.utils.AppValidator.isEmail
import com.app.spark.utils.AppValidator.isOnlyDigit
import com.app.spark.utils.AppValidator.isValidEmail
import com.app.spark.utils.AppValidator.isValidMobile
import com.app.spark.utils.AppValidator.isValidPassword
import com.app.spark.utils.AppValidator.isValidUserName
import com.app.spark.viewmodel.LoginSignUpViewModel
import com.app.spark.utils.SharedPrefrencesManager
import com.google.gson.Gson
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.models.ImportantDataResult


class LoginSignUpActivity : BaseActivity(), View.OnClickListener, View.OnFocusChangeListener {


    lateinit var binding: ActivityLoginSignUpBinding
    var type = "login"
    var signUpType = "mobile"
    private lateinit var viewModel: LoginSignUpViewModel
    lateinit var pref: SharedPrefrencesManager
    var loginType = ""
    var isShow = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_sign_up)
        pref = SharedPrefrencesManager.getInstance(this)
        applyClickListener()
        initlizeViewModel()
        applyFocusListener()
        binding.cardLogin.clipToOutline = true
        binding.cardSignUp.clipToOutline = true
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
            intent.putExtra(IntentConstant.EMAIL_PHONE, binding.edtEmailPhone.text.toString())
            startActivity(intent)

        })

        viewModel.loginResponse.observe(this, { res: ImportantDataResponse ->
            hideView(binding.progressBar)
            if (res.result.verified == "true") {
                pref.setString(PrefConstant.ACCESS_TOKEN, res.result.accesstoken)
                pref.setString(PrefConstant.USER_ID, res.result.user_id)
             //   if (res.result.completed == "Yes") {
                    pref.setBoolean(PrefConstant.IS_LOGIN, true)
                    pref.setString(PrefConstant.LOGIN_RESPONSE, Gson().toJson(res.result))
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
               /* } else {
                    val intent = Intent(this, ImportantDataActivity::class.java)
                    startActivity(intent)
                }*/
                finish()
            } /*else {
                var emailPhone = ""
                if (!res.result.email.isEmpty()) {
                    signUpType = "email"
                    emailPhone = res.result.email
                } else {
                    signUpType = "mobile"
                    emailPhone = res.result.mobile
                }
                val intent = Intent(this, OtpActivity::class.java)
                intent.putExtra(IntentConstant.SIGNUP_TYPE, signUpType)
                intent.putExtra(IntentConstant.EMAIL_PHONE, emailPhone)
                startActivity(intent)
                finish()
            }*/
        })

        viewModel.errString.observe(this, { err: String ->
            hideView(binding.progressBar)
            showToastShort(this, err)
        })

        viewModel.userNameAvailable.observe(this, {
            if (it) {
                binding.imgUserNameVerify.visibility = View.VISIBLE
            } else {
                binding.imgUserNameVerify.visibility = View.GONE
            }
        })

        viewModel.errRes.observe(this, { err: Int ->
            binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }

    private fun applyClickListener() {
        binding.txtLogin.setOnClickListener(this)
        binding.tvSignUp.setOnClickListener(this)
        binding.txtSignup.setOnClickListener(this)
        // binding.sparkEmailSignUp.setOnClickListener(this)
        binding.tvForgotPassword.setOnClickListener(this)
        binding.btnSignUp.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.imgEyePassword.setOnClickListener(this)
        binding.imgEyeCpassword.setOnClickListener(this)
        binding.imgEyeLoginpassword.setOnClickListener(this)
        userNameTextChange()
    }

    private fun applyFocusListener() {
        binding.edtPhoneNo.setOnFocusChangeListener(this)
        binding.etUserName.setOnFocusChangeListener(this)
        binding.etConfirmPassword.setOnFocusChangeListener(this)
        binding.etPassword.setOnFocusChangeListener(this)
        binding.etLoginPassword.setOnFocusChangeListener(this)
        binding.edtEmailPhone.setOnFocusChangeListener(this)
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
            binding.txtSignup ->
                setSignUp()
            binding.tvSignUp ->
                setSignUp()
            binding.txtLogin ->
                setLogin()
            binding.tvForgotPassword -> {
                startActivity(Intent(this, ForgotPasswordActivity::class.java))
            }
            binding.imgEyePassword -> {
                if (isShow) {
                    isShow = false
                    binding.imgEyePassword.setImageDrawable(resources.getDrawable(R.drawable.ic_visibility))
                    binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    binding.etPassword.setSelection(binding.etPassword.length())
                } else {
                    isShow = true
                    binding.imgEyePassword.setImageDrawable(resources.getDrawable(R.drawable.ic_close_eye))
                    binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                    binding.etPassword.setSelection(binding.etPassword.length())

                }
            }
            binding.imgEyeCpassword -> {
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
            binding.imgEyeLoginpassword -> {
                if (isShow) {
                    isShow = false
                    binding.imgEyeLoginpassword.setImageDrawable(resources.getDrawable(R.drawable.ic_visibility))
                    binding.etLoginPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance()
                    )
                    binding.etLoginPassword.setSelection(binding.etLoginPassword.length())
                } else {
                    isShow = true
                    binding.imgEyeLoginpassword.setImageDrawable(resources.getDrawable(R.drawable.ic_close_eye))
                    binding.etLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                    binding.etLoginPassword.setSelection(binding.etLoginPassword.length())
                }
            }
            binding.sparkEmailSignUp -> {
                binding.edtEmailPhone.clearFocus()
                if (signUpType.equals("email")) {
                    signUpType = "mobile"
                    setNumberType()

                } else {
                    signUpType = "email"
                    setEmailType()
                }
            }
            binding.btnSignUp -> {
                if (isValidSignUp()) {
                    if (isNetworkAvailable(this)) {
                        showView(binding.progressBar)
                        viewModel.signUpAPI(
                            binding.etUserName.text.toString(),
                            binding.etConfirmPassword.text.toString(),
                            signUpType,
                            binding.edtEmailPhone.text.toString()
                        )
                    }
                }
            }
            binding.btnLogin -> {
                if (isValidLogin()) {
                    if (isNetworkAvailable(this)) {
                        showView(binding.progressBar)
                        if (isOnlyDigit(binding.edtPhoneNo)) {
                            loginType = "mobile"
                        } else if (isEmail(binding.edtPhoneNo)) {
                            loginType = "email"
                        } else {
                            loginType = "username"
                        }
                        viewModel.loginAPI(
                            loginType,
                            binding.etLoginPassword.text.toString(),
                            binding.edtPhoneNo.text.toString()
                        )
                    }
                }
            }
        }
    }

    private fun setEmailType() {
        binding.edtEmailPhone.setText("")
        binding.edtEmailPhone.hint = getString(R.string.email_address)
        binding.imgEmailPhone.setImageResource(R.drawable.ic_call)
        binding.edtEmailPhone.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_email_inactive
            ), null, null, null
        )
        setEditTextMaxLength(binding.edtEmailPhone, 40)
        binding.edtEmailPhone.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

    }

    private fun setNumberType() {
        binding.edtEmailPhone.setText("")
        binding.edtEmailPhone.hint = getString(R.string.phone_number)
        binding.imgEmailPhone.setImageResource(R.drawable.ic_email)
        setEditTextMaxLength(binding.edtEmailPhone, 10)
        binding.edtEmailPhone.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_call_inactive
            ), null, null, null
        )
        binding.edtEmailPhone.inputType = InputType.TYPE_CLASS_NUMBER

    }

    private fun setSignUp() {
        type = "signup"
        binding.clLeft.elevation = 0F
        binding.clRight.elevation = resources.getDimensionPixelSize(R.dimen._6sdp).toFloat()
        binding.cardSignUp.elevation = resources.getDimensionPixelSize(R.dimen._7sdp).toFloat()
        binding.txtHeader.text = getString(R.string.welcome)
        binding.groupSignUp.visibility = View.VISIBLE
        binding.groupLogin.visibility = View.GONE
        binding.txtSpark.visibility = View.VISIBLE
        binding.txtAccount.visibility = View.GONE
        binding.txtSignup.visibility = View.GONE
        binding.sparkGoogleSignUp.visibility = View.VISIBLE
        //  binding.sparkEmailSignUp.visibility = View.VISIBLE
        binding.txtLogin.setTextColor(ContextCompat.getColor(this, R.color.text_hint))
        binding.tvSignUp.setTextColor(ContextCompat.getColor(this, R.color.theme_color))
        showView(binding.btnSignUp)
        hideView(binding.btnLogin)

    }

    private fun setLogin() {
        type = "login"
        binding.clLeft.elevation = resources.getDimensionPixelSize(R.dimen._6sdp).toFloat()
        binding.clRight.elevation = 0F
        binding.txtHeader.text = getString(R.string.hey_wassup)
        binding.txtSpark.visibility = View.GONE
        binding.groupSignUp.visibility = View.GONE
        binding.groupLogin.visibility = View.VISIBLE
        binding.txtAccount.visibility = View.VISIBLE
        binding.txtSignup.visibility = View.VISIBLE
        binding.sparkGoogleSignUp.visibility = View.GONE
        binding.sparkEmailSignUp.visibility = View.GONE
        binding.txtLogin.setTextColor(ContextCompat.getColor(this, R.color.theme_color))
        binding.tvSignUp.setTextColor(ContextCompat.getColor(this, R.color.text_hint))
        hideView(binding.btnSignUp)
        showView(binding.btnLogin)
    }

    private fun isValidSignUp(): Boolean {
        if (binding.etUserName.text.toString().isEmpty()) {
            showToastShort(this, getString(R.string.enter_user_name))
            return false
        } else if (binding.etUserName.text.toString().length < 4) {
            showToastShort(this, getString(R.string.username_length_validation))
            return false
        } else if (signUpType.equals("email") && !isValidEmail(
                this,
                binding.edtEmailPhone,
                getString(R.string.enter_email_address)
            )
        ) {
            return false
        } else if (signUpType.equals("mobile") && !isValidMobile(
                this,
                binding.edtEmailPhone,
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

    private fun isValidLogin(): Boolean {
        return if (!isValidUserName(this, binding.edtPhoneNo)) {
            false
        } else isValidPassword(
            this,
            binding.etLoginPassword,
            getString(R.string.please_enter_password)
        )
    }

    override fun onFocusChange(p0: View?, p1: Boolean) {
        when (p0) {
            binding.etUserName, binding.edtPhoneNo -> {
                val editText = p0 as EditText
                if (p1)
                    editText.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_person_active
                        ), null, null, null
                    )
                else if (!p1 && editText.text.toString().isNullOrEmpty()) {
                    editText.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_person
                        ), null, null, null
                    )
                }
            }

            binding.etPassword, binding.etLoginPassword, binding.etConfirmPassword -> {
                val editText = p0 as EditText
                if (p1)
                    editText.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_lock_active
                        ), null, null, null
                    )
                else if (!p1 && editText.text.toString().isNullOrEmpty()) {
                    editText.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_lock
                        ), null, null, null
                    )
                }
            }

            binding.edtEmailPhone -> {
                val editText = p0 as EditText
                if (signUpType.equals("email")) {
                    if (p1)
                        editText.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.ic_email_active
                            ), null, null, null
                        ) else if (!p1 && editText.text.toString().isNullOrEmpty()) {
                        editText.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.ic_email_inactive
                            ), null, null, null
                        )
                    }
                } else {
                    if (p1)
                        editText.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.ic_call_active
                            ), null, null, null
                        )
                    else if (!p1 && editText.text.toString().isNullOrEmpty()) {
                        editText.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.ic_call_inactive
                            ), null, null, null
                        )
                    }
                }
            }
        }
    }

}