package com.app.spark.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.main.MainActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityOtpBinding
import com.app.spark.models.ImportantDataResponse
import com.app.spark.models.ImportantDataResult
import com.app.spark.models.OtpResponse
import com.app.spark.utils.BaseActivity
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.showSnackBar
import com.app.spark.utils.showToastShort
import com.app.spark.viewmodel.OtpViewModel
import com.google.gson.Gson


class OtpActivity : BaseActivity(), View.OnClickListener {

    lateinit var binding: ActivityOtpBinding

    var userOtp = ""
    var previousLength = 0
    private lateinit var viewModel: OtpViewModel
    var type = ""
    var emailPhone = ""
    lateinit var pref: SharedPrefrencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_otp)
        pref = SharedPrefrencesManager.getInstance(this)

        if (intent.extras != null) {
            type = intent.getStringExtra(IntentConstant.SIGNUP_TYPE).toString()
            emailPhone = intent.getStringExtra(IntentConstant.EMAIL_PHONE).toString()
        }


        if(intent.getStringExtra(IntentConstant.EMAIL_PHONE).toString()!=null)
        {

            binding.otpTxt.setText("Kindly enter the 4 digit code sent to " + emailPhone)

        }

//        if (type.equals("email")) {
//            binding.tvVerify.text = getString(R.string.verify_email_address)
//            binding.tvChangeNumber.text = getString(R.string.change_email)
//            binding.tvOtpMsg.text =
//                getString(R.string.otp_has_been_sent_to_your_email_please_enter_it_below)
//        } else {
//            binding.tvVerify.text = getString(R.string.verify_mobile_number)
//            binding.tvChangeNumber.text = getString(R.string.change_number)
//            binding.tvOtpMsg.text =
//                getString(R.string.otp_has_been_sent_to_your_mobile_number_on_your_phone_please_enter_it_below)
//
//        }
        initlizeViewModel()
        applyClickListener()
        moveCursor()
        startTimer()
        setCountdownTimer(120000)

    }

    private fun applyClickListener() {
        // binding.tvBack.setOnClickListener(this)
        binding.tvChangeNumber.setOnClickListener(this)
        binding.tvResend.setOnClickListener(this)
    }


    private fun initlizeViewModel() {

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                OtpViewModel::class.java
            )
       viewModel.sendOtpAPI(emailPhone)

        viewModel.response.observe(this, { res: ImportantDataResponse ->
            hideView(binding.progressBar)
            showToastShort(this, res.APICODERESULT)
            pref.setString(PrefConstant.ACCESS_TOKEN, res.result.accesstoken)
            pref.setString(PrefConstant.USER_ID, res.result.user_id)
            pref.setString(PrefConstant.USERNAME, res.result.username)
            pref.setString(PrefConstant.NAME, res.result.name)
            pref.setString(PrefConstant.GENDER, res.result.gender)
            pref.setString(PrefConstant.BIO, res.result.user_bio)
            pref.setString(PrefConstant.PROFILE_PIC, res.result.profile_pic)
            pref.setString(PrefConstant.DOB, res.result.dob)
            pref.setString(PrefConstant.MOBILE, res.result.mobile)
            pref.setString(PrefConstant.LOGIN_RESPONSE, Gson().toJson(res.result))
            pref.setBoolean(PrefConstant.IS_LOGIN, true)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

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


    private fun startTimer() {
        object : CountDownTimer(30000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                binding.tvResend.isEnabled = false
                var time = (millisUntilFinished / 1000)
                if (time >= 10) {
                    binding.tvResend.text =
                        getString(R.string.resend_in) + " " + (millisUntilFinished / 1000).toString()
                } else {
                    if (time.toInt() == 1) {
                        time = 0
                        binding.tvResend.isEnabled = true

                    }
                    binding.tvResend.text =
                        getString(R.string.resend_in) + " 0" + time.toString()
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                binding.tvResend.text = getString(R.string.resend)
                binding.tvResend.isEnabled = true
            }
        }.start()
    }


    /*
       Mehtod to automatically move cursor to next editText
    */
    private fun moveCursor() {
        //GenericKeyEvent here works for deleting the element and to switch back to previous EditText
        //first parameter is the current EditText and second parameter is previous EditText
        binding.etOtpOne.setOnKeyListener(GenericKeyEvent(binding.etOtpOne, null))
        binding.etOtpTwo.setOnKeyListener(GenericKeyEvent(binding.etOtpTwo, binding.etOtpOne))
        binding.etOtpThree.setOnKeyListener(GenericKeyEvent(binding.etOtpThree, binding.etOtpTwo))
        binding.etOtpFour.setOnKeyListener(GenericKeyEvent(binding.etOtpFour, binding.etOtpThree))

        binding.etOtpOne.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                previousLength = s.length
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 1) {
                    binding.etOtpTwo.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {

            }

        })
        binding.etOtpTwo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                previousLength = s.length
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 1) {
                    binding.etOtpThree.requestFocus()
                } else if (s.length == 0) {
                    binding.etOtpOne.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        binding.etOtpThree.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                previousLength = s.length
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 1) {
                    binding.etOtpFour.requestFocus()
                } else if (s.length == 0) {
                    binding.etOtpTwo.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        binding.etOtpFour.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                previousLength = s.length
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {

                binding.etOtpFour.setSelection(binding.etOtpFour.text!!.length)
                if (s.length == 1) {
                    binding.etOtpFour.requestFocus()
                    hideKeyboard(this@OtpActivity)

                    userOtp = (binding.etOtpOne.text.toString().trim { it <= ' ' }
                            + binding.etOtpTwo.text.toString().trim { it <= ' ' }
                            + binding.etOtpThree.text.toString().trim { it <= ' ' }
                            + binding.etOtpFour.text.toString().trim { it <= ' ' }
                            )
                    viewModel.otpAPI(userOtp, type, emailPhone)

                } else if (s.length == 0) {
                    binding.etOtpThree.requestFocus()
                }

            }
        })
    }

    override fun onClick(v: View?) {
        when (v) {
//            binding.tvBack -> {
//                finish()
//            }
            binding.tvResend -> {
                viewModel.sendOtpAPI(emailPhone)
            }
            binding.tvChangeNumber -> {
                val intent = Intent(this, ChangeEmailMobileActivity::class.java)
                intent.putExtra(IntentConstant.SIGNUP_TYPE, type)
                startActivity(intent)
            }
        }
    }

    private fun setCountdownTimer(time: Long) {
        //    binding.tvTimeOut.visibility = View.VISIBLE
        object : CountDownTimer(time, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val seconds = totalSeconds % 60
                val minutes = totalSeconds / 60 % 60
                if (minutes > 0) {
                    if (seconds > 9) {
                        binding.tvTimeOut.text = "$minutes:$seconds"
                    } else
                        binding.tvTimeOut.text = "$minutes:0$seconds"
                } else if (seconds > 9) {
                    binding.tvTimeOut.text = "0:$seconds"
                } else {
                    binding.tvTimeOut.text = "00:0$seconds"
                }  //here you can have your logic to set text to edittext
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                binding.tvTimeOut.text = "00:00"
            }
        }.start()
    }

    // text watcher
    class GenericKeyEvent internal constructor(
        private val currentView: EditText,
        private val previousView: EditText?
    ) : View.OnKeyListener {
        override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
            if (event!!.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.id != R.id.etOtpOne && currentView.text.isEmpty()) {
                //If current is empty then previous EditText's number will also be deleted
                previousView!!.text = null
                previousView.requestFocus()
                return true
            }
            return false
        }


    }

    class GenericTextWatcher internal constructor(
        private val currentView: View,
        private val nextView: View?
    ) : TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            // TODO Auto-generated method stub
            val text = editable.toString()
            when (currentView.id) {
                R.id.etOtpOne -> if (text.length == 1) nextView!!.requestFocus()
                R.id.etOtpTwo -> if (text.length == 1) nextView!!.requestFocus()
                R.id.etOtpThree -> if (text.length == 1) nextView!!.requestFocus()
                //You can use EditText4 same as above to hide the keyboard
            }
        }

        override fun beforeTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) { // TODO Auto-generated method stub
        }

        override fun onTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) { // TODO Auto-generated method stub

        }

    }

}