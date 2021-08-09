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
import com.app.spark.activity.change_password.ChangePasswordActivity
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityOtpBinding
import com.app.spark.models.OtpResponse
import com.app.spark.utils.BaseActivity
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.showSnackBar
import com.app.spark.utils.showToastShort
import com.app.spark.viewmodel.ForgotPasswordOtpViewModel
import com.app.spark.viewmodel.OtpViewModel
import com.google.gson.Gson

class ForgotPasswordOtpActivity : BaseActivity(), View.OnClickListener {

    lateinit var binding: ActivityOtpBinding

    var userOtp = ""
    var previousLength = 0
    private lateinit var viewModel: ForgotPasswordOtpViewModel
    var emailPhone = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_otp)
        if (intent.extras != null) {
            emailPhone = intent.getStringExtra(IntentConstant.EMAIL_PHONE).toString()
        }


        if(intent.getStringExtra(IntentConstant.EMAIL_PHONE).toString()!=null)
        {

            binding.otpTxt.setText("Kindly enter the 4 digit code sent to \n" + emailPhone)

        }
        initlizeViewModel()
        applyClickListener()
        moveCursor()
        startTimer()
        setCountdownTimer(120000)

    }

    private fun applyClickListener() {
      //  binding.tvBack.setOnClickListener(this)
        binding.tvResend.setOnClickListener(this)
        binding.tvChangeNumber.visibility = View.GONE
    }


    private fun initlizeViewModel() {

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                ForgotPasswordOtpViewModel::class.java
            )
        viewModel.sendOtpAPI(emailPhone)

        viewModel.response.observe(this, { err: String ->
            hideView(binding.progressBar)
            if (!err.isNullOrEmpty()) {
                startActivity(
                    Intent(this, ChangePasswordActivity::class.java)
                        .putExtra(IntentConstant.EMAIL_PHONE, emailPhone)
                )
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
                binding.etOtpFour.setSelection(binding.etOtpFour.text!!.length)
                if (s.length == 1) {
                    binding.etOtpFour.requestFocus()
                    hideKeyboard(this@ForgotPasswordOtpActivity)

                    userOtp = (binding.etOtpOne.text.toString().trim { it <= ' ' }
                            + binding.etOtpTwo.text.toString().trim { it <= ' ' }
                            + binding.etOtpThree.text.toString().trim { it <= ' ' }
                            + binding.etOtpFour.text.toString().trim { it <= ' ' }
                            )
                    viewModel.otpAPI(userOtp, emailPhone)

                } else if (s.length == 0) {
                    binding.etOtpThree.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {

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
        }
    }

    private fun setCountdownTimer(time: Long) {
       // binding.tvTimeOut.visibility = View.VISIBLE
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