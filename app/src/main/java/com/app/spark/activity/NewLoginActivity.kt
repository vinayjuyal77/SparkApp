package com.app.spark.activity

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.main.MainActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityLoginNewBinding
import com.app.spark.models.ImportantDataResponse
import com.app.spark.models.ImportantDataResult
import com.app.spark.utils.*
import com.app.spark.viewmodel.LoginSignUpViewModel
import com.cancan.Utility.PermissionsUtil
import com.google.gson.Gson

class NewLoginActivity : BaseActivity(), View.OnClickListener {
    lateinit var binding: ActivityLoginNewBinding
    var type = "login"
    private lateinit var viewModel: LoginSignUpViewModel
    lateinit var pref: SharedPrefrencesManager
    var loginType = ""
    var isShow = true
    private var permissionsUtil: PermissionsUtil? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_new)
        pref = SharedPrefrencesManager.getInstance(this)
        permissionsUtil = PermissionsUtil(this)

     //   askPermission()

        if (SharedPrefrencesManager.getInstance(this)
                .getBoolean(PrefConstant.IS_LOGIN, false)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
//            val intent = Intent(this, NewLoginActivity::class.java)
//            startActivity(intent)
//            finish()
        }
        applyClickListener()
        initlizeViewModel()

    }



    private fun initlizeViewModel() {

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                LoginSignUpViewModel::class.java
            )


        viewModel.loginResponse.observe(this, Observer { res: ImportantDataResponse ->
            hideView(binding.progressBar)
            if (res.result.verified == "true") {
                pref.setString(PrefConstant.ACCESS_TOKEN, res.result.accesstoken)
                pref.setString(PrefConstant.USER_ID, res.result.user_id)
                //   if (res.result.completed == "Yes") {
                pref.setBoolean(PrefConstant.IS_LOGIN, true)
                pref.setString(PrefConstant.USERNAME, res.result.username)
                pref.setString(PrefConstant.NAME, res.result.name)
                pref.setString(PrefConstant.GENDER, res.result.gender)
                pref.setString(PrefConstant.BIO, res.result.user_bio)
                pref.setString(PrefConstant.PROFILE_PIC, res.result.profile_pic)
                pref.setString(PrefConstant.DOB, res.result.dob)
                pref.setString(PrefConstant.MOBILE, res.result.mobile)
                pref.setString(PrefConstant.LOGIN_RESPONSE, Gson().toJson(res.result))
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("splash",true)
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

        viewModel.errString.observe(this, Observer{ err: String ->
            hideView(binding.progressBar)
            showToastShort(this, err)
        })

        viewModel.errString.observe(this, Observer {

        })

        viewModel.errRes.observe(this, Observer { err: Int ->
            binding.progressBar.visibility = View.GONE
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }

    private fun askPermission() {
        permissionsUtil?.askPermissions(this, PermissionsUtil.CAMERA,
            PermissionsUtil.STORAGE, object : PermissionsUtil.PermissionListener {
                override fun onPermissionResult(isGranted: Boolean) {
                    if (isGranted) {
                        // getAllAudio()
                    } else {
                        askPermission()
                    }
                }

            })
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsUtil?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun applyClickListener() {
        binding.btnLogin.setOnClickListener(this)
        binding.tvSignUp.setOnClickListener(this)
        binding.tvClickHere.setOnClickListener(this)
        binding.imgEyeLoginpassword.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.tvClickHere -> {
                startActivity(Intent(this, ForgotPasswordActivity::class.java))
            }
            binding.tvSignUp -> {
                startActivity(Intent(this, NewSignupActivity::class.java))
            }
            binding.imgEyeLoginpassword -> {
                if (isShow) {
                    isShow = false
                    binding.imgEyeLoginpassword.setImageDrawable(resources.getDrawable(R.drawable.ic_visibility))
                    binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    binding.etPassword.setSelection(binding.etPassword.length())
                } else {
                    isShow = true
                    binding.imgEyeLoginpassword.setImageDrawable(resources.getDrawable(R.drawable.ic_close_eye))
                    binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                    binding.etPassword.setSelection(binding.etPassword.length())
                }
            }
            binding.btnLogin -> {
                if (isValidLogin()) {
                    if (isNetworkAvailable(this)) {
                        showView(binding.progressBar)
                        loginType = if (AppValidator.isOnlyDigit(binding.etUserName)) {
                            "mobile"
                        } else {
                            "username"
                        }
                        viewModel.loginAPI(
                            loginType,
                            binding.etPassword.text.toString(),
                            binding.etUserName.text.toString()
                        )
                    }
                }
            }
        }
    }


    private fun isValidLogin(): Boolean {
        return if (!AppValidator.isValidUserName(this, binding.etUserName)) {
            false
        } else AppValidator.isValidPassword(
            this,
            binding.etPassword,
            getString(R.string.please_enter_password)
        )
    }

}
