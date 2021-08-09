package com.app.spark.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import androidx.lifecycle.Observer
import com.app.spark.models.CommonResponse
import com.app.spark.viewmodel.ChangeEmailMobileViewModel
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.utils.*
import kotlinx.android.synthetic.main.activity_change_email_mobile.*

class ChangeEmailMobileActivity : BaseActivity(), View.OnClickListener {

    var type = ""
    private lateinit var viewModel: ChangeEmailMobileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email_mobile)
        type = intent.getStringExtra(IntentConstant.SIGNUP_TYPE).toString()
        if (type.equals("email")) {
            tvEmailPhone.text = "Change Email Address"
            tvMsg.text="You can change your email here and you will receive the OTP on this email."
            showView(edtEmail)
            hideView(edtPhone)
        } else {
            tvEmailPhone.text = "Change Mobile Number"
            tvMsg.text="You can change your number here and you will receive the OTP on this number."
            hideView(edtEmail)
            showView(edtPhone)
        }
        initlizeViewModel()
        applyClickListener()
    }

    private fun applyClickListener() {
        btnSubmit.setOnClickListener(this)
        tvBack.setOnClickListener(this)
    }

    private fun initlizeViewModel() {

        viewModel =
            ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(application)).get(ChangeEmailMobileViewModel::class.java)

        viewModel.response.observe(this, { res: CommonResponse ->
            hideView(progressBar)
            val intent = Intent(this, OtpActivity::class.java)
            intent.putExtra(IntentConstant.SIGNUP_TYPE, type)
            if (type.equals("email")) {
                intent.putExtra(IntentConstant.EMAIL_PHONE, edtEmail.text.toString())
            } else {
                intent.putExtra(IntentConstant.EMAIL_PHONE, edtPhone.text.toString())
            }
            startActivity(intent)

        })


        viewModel.errString.observe(this, { err: String ->
            hideView(progressBar)
            showToastShort(this, err)
        })
        viewModel.errRes.observe(this, { err: Int ->
            hideView(progressBar)
            if (err != null)
                showToastShort(this, getString(err))
        })
    }


    override fun onClick(v: View?) {
        when (v) {
            tvBack ->
                finish()
            btnSubmit -> {
                if (isValidate()) {
                    if (isNetworkAvailable(this)) {
                        showView(progressBar)
                        if (type.equals("email")) {
                            viewModel.changeSignUpAPI(
                                SharedPrefrencesManager.getInstance(this)
                                    .getString(PrefConstant.USER_ID, "").toString(),
                                type,
                                edtEmail.text.toString()
                            )
                        } else {
                            viewModel.changeSignUpAPI(
                                SharedPrefrencesManager.getInstance(this)
                                    .getString(PrefConstant.USER_ID, "").toString(),
                                type,
                                edtPhone.text.toString()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun isValidate(): Boolean {
        if (type.equals("email") && !AppValidator.isValidEmail(
                this,
                edtEmail,
                getString(R.string.enter_email_address)
            )
        ) {
            return false
        } else if (type.equals("mobile") && !AppValidator.isValidMobile(
                this,
                edtPhone,
                getString(R.string.enter_phone_number)
            )
        ) {
            return false
        } else {
            return true
        }
    }
}