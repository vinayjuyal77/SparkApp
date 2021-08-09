package com.app.spark.activity

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityFixYourProfileBinding
import com.app.spark.models.ImportantDataResult
import com.app.spark.utils.*
import com.app.spark.viewmodel.ProfileGetViewModel
import com.bumptech.glide.Glide
import com.cancan.Utility.PermissionsUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_upload_profile.*
import kotlinx.android.synthetic.main.dialog_gender.*
import kotlinx.android.synthetic.main.dialog_location_permission.*
import java.io.IOException
import java.util.*



class FixYourProfileActivity : BaseActivity(), View.OnClickListener {
    lateinit var binding: ActivityFixYourProfileBinding
    private lateinit var viewModel: ProfileGetViewModel
    private lateinit var permissionsUtil: PermissionsUtil
    private var token: String? = ""
    private val REQUEST_CHECK_SETTINGS = 0x1
    internal var lat: Double = 0.toDouble()
    internal var long: Double = 0.toDouble()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var location = ""
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null
    var filePath = ""
    private var loginDetail: ImportantDataResult? = null
    lateinit var pref: SharedPrefrencesManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fix_your_profile)
        pref = SharedPrefrencesManager.getInstance(this)
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")
        loginDetail = Gson().fromJson(
            pref.getString(PrefConstant.LOGIN_RESPONSE, ""),
            ImportantDataResult::class.java
        )
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                ProfileGetViewModel::class.java
            )
        viewModel.getUserProfileData(token!!, loginDetail!!.user_id, loginDetail!!.user_id)
        setClickListener(this)
        textChangeListener()
        permissionsUtil = PermissionsUtil(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (doesUserHavePermission() == 0) {
            askPermission()
        } else {
            locationPermissionDialog()
        }
        obServerBio()
        setLoginDetails()
    }

    private fun obServerBio() {
        viewModel.responseEditBio.observe(this, {
            binding.progressBar.visibility = View.GONE
            setClickListener(this)
            if (it != null) {

                if(it.result?.profilePic!="") {
                    pref.setString(PrefConstant.PROFILE_PIC, it.result?.profilePic)

                }

                pref.setString(PrefConstant.DOB, it.result?.dob)
                pref.setString(PrefConstant.GENDER, it.result?.gender)
                pref.setString(PrefConstant.NAME, it.result?.name)

                showSnackBar(binding.root, it.aPICODERESULT)
                setResult(RESULT_OK)
                finish()
            }
        })

        viewModel.errString.observe(this, { err: String ->
            binding.progressBar.visibility = View.GONE
            setClickListener(this)
            if (!err.isNullOrEmpty())
                showSnackBar(binding.root, err)
        })

        viewModel.errRes.observe(this, { err: Int ->
            binding.progressBar.visibility = View.GONE
            setClickListener(this)
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }


    private fun setClickListener(onClickListener: View.OnClickListener? = null) {
        binding.etGender.setOnClickListener(onClickListener)
        binding.etDob.setOnClickListener(onClickListener)
        binding.tvFinish.setOnClickListener(onClickListener)
        binding.tvBack.setOnClickListener(onClickListener)
        binding.imgBack.setOnClickListener(onClickListener)
        binding.tvChangeDp.setOnClickListener(onClickListener)
    }

    private fun setLoginDetails() {
        viewModel.response.observe(this, {
            if (it != null) {
                binding.etName.setText(it.result.name)
                binding.etBio.setText(it.result.userBio)
                binding.etGender.text = it.result.gender


                binding.etDob.text = it.result?.dob?.split("T")?.get(0) ?: ""
                location = it.result?.location ?: ""

                if(it.result.dob != "")
                {
                    binding.etDob.isEnabled = false
                    pref.setString(PrefConstant.DOB, it.result?.dob)

                }

                if(it.result.lat == "")
                {
                    lat = 0.0;
                    long = 0.0;
                }
                else {
                    lat = (it.result.lat ?: "0").toDouble()
                    long = (it.result.long ?: "0").toDouble()
                }

                    //Glide.with(this).load(it.result?.profilePic).into(binding.imgProfilePic)
                    Glide.with(this).load(it.result?.profilePic).into(binding.imgAdd)


                pref.setString(PrefConstant.PROFILE_PIC, it.result?.profilePic)
            pref.setString(PrefConstant.DOB, it.result?.dob)
            pref.setString(PrefConstant.GENDER, it.result?.gender)
            pref.setString(PrefConstant.NAME, it.result?.name)

            }
        })
    }

    private fun textChangeListener() {
        binding.etBio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                binding.tvBioCount.text = "${s.toString().length}/200"
            }
        })
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.tvChangeDp -> {
                askCameraPermission()
            }
            binding.imgAdd -> {
                askCameraPermission()
            }
            binding.imgBack -> {
                onBackPressed()
            }
            binding.etGender -> {
                selectGenderDialog(binding.etGender.text.toString())
            }
            binding.etDob -> {
                showDate()
            }
            binding.tvFinish -> {
                hideKeyboard(this)
                if (isValidate()) {
                    binding.progressBar.visibility = View.VISIBLE
                    setClickListener()
                    viewModel.editBioAdd(
                        token!!,
                        loginDetail?.user_id!!,
                        binding.etBio.text.toString(),
                        filePath,
                        binding.etName.text.toString(),
                        binding.etGender.text.toString(),
                        binding.etDob.text.toString(),
                        location,
                        lat.toString(),
                        long.toString()
                    )
                }
            }
            binding.tvBack -> {
                onBackPressed()
            }
        }
    }

    private fun locationPermissionDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_location_permission)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        dialog.tvAllow.setOnClickListener {
            askPermission()
            dialog.dismiss()

        }
        dialog.tvDecline.setOnClickListener {
            dialog.dismiss()
            //finish()
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
    }

    private fun selectGenderDialog(prevGender: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_gender)
//        dialog.window!!.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        when (prevGender.trim()) {
            "Male"// first rdb button
            -> dialog.rdMale.isChecked = true//getString(R.string.male)
            "Female" // second rdb button
            -> dialog.rdFemale.isChecked = true//getString(R.string.female)
            "Transgender" // third rdb button
            -> dialog.rdTransender.isChecked = true//getString(R.string.transgender)
            "Others"
            -> dialog.rdOthers.isChecked = true//getString(R.string.others)
        }
        dialog.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButtonID = dialog.radioGroup.checkedRadioButtonId
            val radioButton = dialog.radioGroup.findViewById<RadioButton>(radioButtonID)
            val index = dialog.radioGroup.indexOfChild(radioButton)
            // Logic
            when (index) {
                0 // first rdb button
                -> binding.etGender.text = getString(R.string.male)
                1 // second rdb button
                -> binding.etGender.text = getString(R.string.female)
                2 // third rdb button
                -> binding.etGender.text = getString(R.string.transgender)
                3
                -> binding.etGender.text = getString(R.string.others)

            }
            dialog.dismiss()
        }
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
    }

    private fun doesUserHavePermission(): Int {
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionCheck
    }

    private fun askPermission() {
        permissionsUtil.askPermissions(this,
            PermissionsUtil.LOCATION,
            PermissionsUtil.LOCATION_CORSE,
            object : PermissionsUtil.PermissionListener {
                override fun onPermissionResult(isGranted: Boolean) {
                    if (isGranted) {
                        requestLocation()
                    } else {
                        if (doesUserHavePermission() == 0)
                            askPermission()
                    }
                }

            })
    }

    private fun askCameraPermission() {
        permissionsUtil.askPermissions(this, PermissionsUtil.CAMERA, PermissionsUtil.STORAGE,
            object : PermissionsUtil.PermissionListener {
                override fun onPermissionResult(isGranted: Boolean) {
                    if (isGranted) {
                        ImagePickerUtil.selectImage(
                            this@FixYourProfileActivity,
                            { imageFile, _ ->
                                filePath = imageFile!!.absolutePath
                                if (imageFile.exists()) {
                                    val myBitmap =
                                        BitmapFactory.decodeFile(imageFile.getAbsolutePath())
                                    binding.imgAdd.setImageBitmap(myBitmap)
                                   // Glide.with(this@FixYourProfileActivity)
                                       // .load(filePath)
                                        // .apply(RequestOptions().centerCrop())
                                       // .into(binding.imgProfilePic)
//                                    Glide.with(this@FixYourProfileActivity)
//                                        .load(filePath)
//                                        // .apply(RequestOptions().centerCrop())
//                                        .into(binding.imgAdd)


                                }
                            },
                            "user",
                            true, false, false, true
                        )
                    }
                }

            })
    }

    private fun requestLocation() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1000
        mLocationRequest!!.fastestInterval = 1000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest!!)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())


        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.lastLocation != null) {
                    lat = locationResult.lastLocation.latitude
                    long = locationResult.lastLocation.longitude
                    findAddress(lat, long)
                    stopLocationUpdates()
                }
            }
        }
        task.addOnSuccessListener(this) { startLocationUpdates() }
        task.addOnFailureListener(this) { e ->
            when ((e as ApiException).statusCode) {
                CommonStatusCodes.RESOLUTION_REQUIRED ->
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        val resolvable = e as ResolvableApiException
                        resolvable.startResolutionForResult(
                            this,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                }
            }
        }
    }

    fun findAddress(latitude: Double?, longitude: Double?) {
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(this, Locale.getDefault())

        try {
            addresses = geocoder.getFromLocation(
                latitude!!,
                longitude!!,
                1
            ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            var address: String? = ""
            if (addresses.size > 0) {
                address =
                    addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                val city = addresses[0].locality
                val state = addresses[0].adminArea
                val getCityState = "$city,$state"
                // SharedPrefManager.getInstance(AddressActivity.this).saveString("saveaddress", getCityState);
            }
            if (address != null) {
                location = address
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        if (mLocationCallback != null) fusedLocationClient.removeLocationUpdates(
            mLocationCallback
        )
    }

    private fun showDate() {
        val cldr = Calendar.getInstance()
        cldr.get(Calendar.YEAR) - 13

        val day = cldr[Calendar.DAY_OF_MONTH]
        val month = cldr[Calendar.MONTH]
        val year = cldr[Calendar.YEAR]
        // date picker dialog
        val datepicker = DatePickerDialog(
            this,
            { view, year, monthOfYear, dayOfMonth ->
                if (dayOfMonth.toString().length < 2) {
                    binding.etDob.text =
                        year.toString() + "-" + (monthOfYear + 1) + "-0" + dayOfMonth.toString()
                } else {
                    binding.etDob.text =
                        year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString()
                }
            },
            year,
            month,
            day
        )
        cldr.add(Calendar.YEAR, -13)
        datepicker.datePicker.maxDate = cldr.timeInMillis

        datepicker.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                startLocationUpdates()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                finish()
                Toast.makeText(
                    this,
                    "Location access denied.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        ImagePickerUtil.onActivityResult(requestCode, resultCode, data)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        permissionsUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    private fun isValidate(): Boolean {

        if (binding.etName.text.toString().isEmpty()) {
            showToastLong(this, getString(R.string.please_enter_name))
            return false
        } else if (binding.etGender.text.toString().isEmpty()) {
            showToastLong(this, getString(R.string.please_select_gender))
            return false
        } else if (binding.etDob.text.toString().isEmpty()) {
            showToastLong(this, getString(R.string.please_enter_dob))
            return false
        } else if (location.isEmpty()) {
            askPermission()
            return false
        } else {
            return true
        }
    }
}