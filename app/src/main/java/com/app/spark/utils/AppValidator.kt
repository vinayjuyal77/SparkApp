package com.app.spark.utils


import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.app.spark.R
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern


object AppValidator {

    val EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
    val NAME_REGEX1 = "^[_A-Za-z0-9-\\+]"
    val NAME_REGEX = "^[A-Za-z0-9\\s]{1,}[\\.]{0,1}[A-Za-z0-9\\s]{0,}$"
    val CHAR_REGEX = ".*[a-zA-Z]+.*"
    val ONLY_CHAR_REGEX = "^[a-zA-Z ]*$"
    val MOBILE_REGEX = "\\d{10}"
    val MOBILE_REGEX_TEST = "\\d{10}|.{11}"
    val YEAR_REGEX = "\\d{4}"
    val ONLY_DIGIT_REGEX = "[0-9]+"

    val PINCODE_REGEX = "^([1-9])([0-9]){5}$"
    val VEHICLE_REGEX = "^[A-Z]{2} [0-9]{2} [A-Z]{2} [0-9]{4}$"
    val PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=_])(?=\\S+$).{6,}$"//.{8,}
    val IMAGE_EXTENSIONS = arrayOf("jpg", "jpeg", "png")


    private var pattern: Pattern? = null
    private lateinit var matcher: Matcher
    //private val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\\\S+\$).{6,}\$"

    private val PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})"


    fun isValidEmail(context: Context, editText: EditText, msg: String): Boolean {
        if (editText.text.toString().trim { it <= ' ' } == "") {
            showToastLong(context, msg)
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        } else if (editText.text.toString().matches(EMAIL_REGEX.toRegex())) {
            return true
        } else {
            showToastLong(context, context.resources.getString(R.string.please_enter_valid_email))
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        }
    }

    fun isEmail(editText: EditText): Boolean {
        return editText.text.toString().matches(EMAIL_REGEX.toRegex())
    }


    fun isValidPassword(context: Context, editText: EditText, msg: String): Boolean {
        if (editText.text.toString().trim { it <= ' ' } == "") {
            showToastLong(context, msg)
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        } else if (editText.text.toString().matches(EMAIL_REGEX.toRegex())) {
            showToastLong(context, context.resources.getString(R.string.please_enter_valid_email))
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        } else if (editText.text.toString().length >= 8)
            return true
        else {
            showToastLong(context, context.resources.getString(R.string.error_invalid_password))
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        }
    }

    fun isValidFullName(context: Context, editText: EditText, msg: String): Boolean {
        if (editText.text.toString().trim { it <= ' ' } == "") {
            showToastLong(context, msg)
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        } else if (editText.text.toString().matches(ONLY_CHAR_REGEX.toRegex()))
            return true
        else {
            showToastLong(context, context.resources.getString(R.string.error_invalid_fullname))
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        }
    }


    fun isValidMobile(context: Context, editText: EditText, msg: String): Boolean {

        if (editText.text.toString().trim { it <= ' ' } == "") {
            showToastLong(context, msg)
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        } else if (editText.text.toString().length >= 9)
            return true
        else {
            showToastLong(context, context.resources.getString(R.string.error_invalid_mobile))
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        }

    }

    fun isOnlyDigit(editText: EditText): Boolean {
        return if (editText.text.toString().matches(ONLY_DIGIT_REGEX.toRegex()))
            true
        else {

            false
        }
    }

    fun isValidUserName(context: Context, editText: EditText): Boolean {

        if (editText.text.length == 0) {
            showToastLong(context, "Enter Username or Phone Number or Email")
            return false
        } else if (editText.text.matches(ONLY_DIGIT_REGEX.toRegex())) {
            if (editText.text.length < 6) {
                showToastLong(context, "Please enter valid mobile no")
                return false
            }
        }

        return true

    }

    fun validatePassword(password: String): Boolean {
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern!!.matcher(password)
        return matcher.matches()
    }

    fun isValidUsername(name: String?): Boolean {

        // Regex to check valid username.
        val regex = "^[A-Za-z]\\w{5,29}$"

        // Compile the ReGex
        val p: Pattern = Pattern.compile(regex)

        // If the username is empty
        // return false
        if (name == null) {
            return false
        }

        // Pattern class contains matcher() method
        // to find matching between given username
        // and regular expression.
        val m: Matcher = p.matcher(name)

        // Return if the username
        // matched the ReGex
        return m.matches()
    }


    fun isValidCardNumber(context: Context, editText: EditText, msg: String): Boolean {
        if (editText.text.toString().trim { it <= ' ' } == "") {
            showToastLong(context, msg)
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        } else if (editText.text.toString().trim { it <= ' ' }.length == 19)
            return true
        else {
            showToastLong(context, "Invalid card number.")
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        }
    }


    fun isValidPincode(editText: EditText, msg: String): Boolean {
        if (editText.text.toString().trim { it <= ' ' } == "") {

            editText.error = msg
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        } else if (editText.text.toString().matches(PINCODE_REGEX.toRegex()))
            return true
        else {
            editText.error = "invalid pincode"
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        }
    }


    fun isValidAddress(editText: EditText, msg: String): Boolean {
        if (editText.text.toString().trim { it <= ' ' } == "") {

            editText.error = msg
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        } else if (editText.text.toString().matches(NAME_REGEX.toRegex()))
            return true
        else {
            editText.error = "invalid address"
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        }
    }

    fun isOnlyChars(context: Context, editText: EditText, msg: String): Boolean {
        if (editText.text.toString().trim { it <= ' ' } == "") {
            showToastLong(context, msg)
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        } else if (editText.text.toString().matches(ONLY_CHAR_REGEX.toRegex()))
            return true
        else {
            showToastLong(context, "invalid name")
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        }

    }

    fun isValidVehicleNo(editText: EditText, msg: String): Boolean {
        if (editText.text.toString().trim { it <= ' ' } == "") {

            editText.error = msg
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        } else if (editText.text.toString().matches(VEHICLE_REGEX.toRegex()))
            return true
        else {
            editText.error = "invalid vehicle no."
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        }
    }


    fun isValidImage(file: File): Boolean {
        for (extensions in IMAGE_EXTENSIONS) {
            if (file.name.toLowerCase().endsWith(extensions))
                return true
        }
        return false
    }

    fun isValidYear(data: String): Boolean {
        return data.matches(YEAR_REGEX.toRegex())
    }


    fun isValid(context: Context, editText: EditText, msg: String): Boolean {
        if (editText.text.toString().trim { it <= ' ' } == "") {
            showToastLong(context, msg)
            editText.addTextChangedListener(RemoveErrorEditText(editText))
            editText.requestFocus()
            return false
        }
        return true
    }


    class RemoveErrorEditText(private val editText: EditText) : TextWatcher {

        override fun afterTextChanged(s: Editable) {

            editText.error = null
        }

        override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
        ) {
        }

        override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
        ) {

        }

    }


}
