package com.app.spark.activity

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.adapter.ListAdapter
import com.app.spark.constants.IntentConstant
import com.app.spark.databinding.ActivityImportantDataBinding
import com.app.spark.models.ImportantDataModel
import com.app.spark.utils.showToastLong
import com.app.spark.viewmodel.ImportantDataViewModel
import com.cancan.Utility.PermissionsUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.dialog_gender.*
import kotlinx.android.synthetic.main.dialog_location_permission.*
import java.io.IOException
import java.util.*

class ImportantDataActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityImportantDataBinding
    private lateinit var viewModel: ImportantDataViewModel
    private lateinit var permissionsUtil: PermissionsUtil
    val REQUEST_CHECK_SETTINGS = 0x1
    internal var lat: Double = 0.toDouble()
    internal var long: Double = 0.toDouble()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var location = ""
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_important_data)
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                ImportantDataViewModel::class.java
            )
        viewModel.getCountryListAPI()
        permissionsUtil = PermissionsUtil(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        applyClickListener()
        if (doesUserHavePermission() == 0) {
            askPermission()
        } else {
            locationPermissionDialog()
        }

    }

    private fun applyClickListener() {
        binding.tvGender.setOnClickListener(this)
        binding.tvDob.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.tvCountry.setOnClickListener(this)
        binding.tvState.setOnClickListener(this)
        binding.tvLocation.setOnClickListener { askPermission() }

    }

    fun locationPermissionDialog() {
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

    fun selectGenderDialog(prevGender: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_gender)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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
                -> binding.tvGender.text = getString(R.string.male)
                1 // second rdb button
                -> binding.tvGender.text = getString(R.string.female)
                2 // third rdb button
                -> binding.tvGender.text = getString(R.string.transgender)
                3
                -> binding.tvGender.text = getString(R.string.others)

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
                    }else{
                        if(doesUserHavePermission()==0)
                        askPermission()
                    }
                }

            })
    }

    private fun setMapToLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {


                lat = location.latitude
                long = location.longitude
                binding.tvLocation.text = lat.toString() + "°N, " + long.toString() + "°E"
            }
        }
    }

    protected fun requestLocation() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1000
        mLocationRequest!!.fastestInterval = 1000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest!!)
        val client = LocationServices.getSettingsClient(this)
        val task =
            client.checkLocationSettings(builder.build())
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.lastLocation != null) {
                    lat = locationResult.lastLocation.latitude
                    long = locationResult.lastLocation.longitude
                    binding.tvLocation.text = lat.toString() + "°N, " + long.toString() + "°E"
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
                    binding.tvDob.text =
                        year.toString() + "-" + (monthOfYear + 1) + "-0" + dayOfMonth.toString()
                } else {
                    binding.tvDob.text =
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

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.tvGender ->
                selectGenderDialog(binding.tvGender.text.toString())
            binding.tvDob ->
                showDate()
            binding.tvCountry ->
                selectCountry()
            binding.tvState ->
                if (!binding.tvCountry.text.toString().isNullOrEmpty())
                    selectState()
                else {
                    showToastLong(this, getString(R.string.please_select_country))
                }
            binding.btnNext -> {
                if (isValidate()) {
                    if (location != null && location.trim().isNotEmpty()) {
                        val model = ImportantDataModel(
                            binding.edtName.text.toString(),
                            binding.tvGender.text.toString(),
                            binding.tvDob.text.toString(),
                            location,
                            binding.tvState.text.toString(),
                            binding.tvCountry.text.toString(),
                            lat.toString(),
                            long.toString()
                        )
                        val intent = Intent(this, UploadProfileActivity::class.java)
                        intent.putExtra(IntentConstant.IMPORTANT_DATA, model)
                        startActivity(intent)
                    } else {
                        showToastLong(this, "Please wait...")
                    }
                }
            }
        }

    }

    private fun isValidate(): Boolean {
        if (binding.edtName.text.toString().isEmpty()) {
            showToastLong(this, getString(R.string.please_enter_name))
            return false
        } else if (binding.tvGender.text.toString().isEmpty()) {
            showToastLong(this, getString(R.string.please_select_gender))
            return false
        } else if (binding.tvDob.text.toString().isEmpty()) {
            showToastLong(this, getString(R.string.please_enter_dob))
            return false
        } else if (binding.tvLocation.text.toString().isEmpty()) {
            askPermission()
            return false
        } else {
            return true
        }
    }


    private fun selectCountry() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_list)
        val list = viewModel.countryList.value
        val adapter = ListAdapter(
            applicationContext,
            android.R.layout.simple_dropdown_item_1line,
            list
        )

        val title: TextView = dialog.findViewById(R.id.tvTitle) as TextView
        title.text = getString(R.string.country)
        val listViewSort: ListView? = dialog.findViewById(R.id.lvListView) as ListView
        listViewSort?.adapter = adapter
        listViewSort?.setOnItemClickListener { parent, view, position, id ->
            viewModel.getStateListAPI(list!![position].id)
            binding.tvCountry.text = list[position].name
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun selectState() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_list)
        val list = viewModel.stateList.value
        if (!list.isNullOrEmpty()) {
            val adapter = ListAdapter(
                applicationContext,
                android.R.layout.simple_dropdown_item_1line,
                list
            )
            val title: TextView = dialog.findViewById(R.id.tvTitle) as TextView
            title.text = getString(R.string.state)
            val listViewSort: ListView? = dialog.findViewById(R.id.lvListView) as ListView
            listViewSort?.adapter = adapter
            listViewSort?.setOnItemClickListener { parent, view, position, id ->
                binding.tvState.text = list!![position].name
                dialog.dismiss()
            }
        }
        dialog.show()
    }
}