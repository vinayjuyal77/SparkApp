package com.cancan.Utility


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Created by Naresh on 30/11/16.
 */
class PermissionsUtil(private var context: Activity?) {
    private var permissionListener: PermissionListener? = null
    // --Commented out by Inspection (9/17/2018 5:36 PM):String tag;
    private val requestCode = 1001
    private var permission: String? = null
    private var selpermission1: String? = null
    private var selpermission2: String? = null

    fun askPermission(
        context: Activity,
        selPermission: String,
        permissionListener: PermissionListener
    ) {
        this.context = context
        this.permissionListener = permissionListener
        permission = selPermission
        if (ContextCompat.checkSelfPermission(context, permission!!) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, arrayOf<String>(permission!!), requestCode)
        } else {
            permissionListener.onPermissionResult(true)
        }
    }

    fun askPermissions(
        context: Activity,
        permission1: String,
        permission2: String,
        permissionListener: PermissionListener
    ) {
        this.context = context
        this.permissionListener = permissionListener
        selpermission1 = permission1
        selpermission2 = permission2
        if (ContextCompat.checkSelfPermission(context, permission1) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(context, permission2) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(context, arrayOf(permission1, permission2), requestCode
            )
        } else {
            permissionListener.onPermissionResult(true)
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionListener!!.onPermissionResult(true)
        } else if (grantResults.size > 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                permissionListener!!.onPermissionResult(true)
            } else {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    showAgainPermissionDialog(selpermission1)
                else
                    showAgainPermissionDialog(selpermission2)
            }
        } else {
            showAgainPermissionDialog(permission)
        }
    }

    private fun showAgainPermissionDialog(permission: String?) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(context!!, permission!!)) {
            val alertBuilder = AlertDialog.Builder(context!!)
            alertBuilder.setCancelable(true)
            alertBuilder.setTitle("Permission Required")
            if (permission == STORAGE)
                alertBuilder.setMessage(storagePermInfoMsg)
            else if (permission == READ_SMS)
                alertBuilder.setMessage(smsPermInfoMsg)
            else if (permission == RECEIVE_SMS)
                alertBuilder.setMessage(smsPermInfoMsg)
            else if (permission == ACCOUNTS)
                alertBuilder.setMessage(accountsPermInfoMsg)
            else if (permission == READ_CALENDAR)
                alertBuilder.setMessage(calendarPermInfoMsg)
            else if (permission == WRITE_CALENDAR)
                alertBuilder.setMessage(calendarPermInfoMsg)
            else if (permission == CAMERA)
                alertBuilder.setMessage(cameraPermInfoMsg)
            else if (permission == LOCATION)
                alertBuilder.setMessage(locationPermInfoMsg)
            else if (permission == READ_CONTACTS)
                alertBuilder.setMessage(contactsPermInfoMsg)
            else if (permission == WRITE_CONTACTS)
                alertBuilder.setMessage(contactsPermInfoMsg)
            else if (permission == LOCATION_CORSE)
                alertBuilder.setMessage(locationPermInfoMsg1)

            alertBuilder.setPositiveButton("Grant") { dialog, which ->
                ActivityCompat.requestPermissions(
                    context!!,
                    arrayOf(permission),
                    requestCode
                )
            }
            alertBuilder.setNegativeButton("Deny") { dialog, which ->
                dialog.dismiss()
                permissionListener!!.onPermissionResult(false)
            }
            val alert = alertBuilder.create()
            alert.show()
        } else {
            if (permission == STORAGE)
                Toast.makeText(context, storagePermErrorMsg, Toast.LENGTH_LONG).show()
            else if (permission == READ_SMS)
                Toast.makeText(context, smsPermErrorMsg, Toast.LENGTH_LONG).show()
            else if (permission == RECEIVE_SMS)
                Toast.makeText(context, smsPermErrorMsg, Toast.LENGTH_LONG).show()
            else if (permission == ACCOUNTS)
                Toast.makeText(context, accountsPermErrorMsg, Toast.LENGTH_LONG).show()
            else if (permission == READ_CALENDAR)
                Toast.makeText(context, calendarPermErrorMsg, Toast.LENGTH_LONG).show()
            else if (permission == WRITE_CALENDAR)
                Toast.makeText(context, calendarPermErrorMsg, Toast.LENGTH_LONG).show()
            else if (permission == CAMERA)
                Toast.makeText(context, cameraPermErrorMsg, Toast.LENGTH_LONG).show()
            else if (permission == LOCATION)
                Toast.makeText(context, locationPermErrorMsg, Toast.LENGTH_LONG).show()
            else if (permission == READ_CONTACTS)
                Toast.makeText(context, contactsPermErrorMsg, Toast.LENGTH_LONG).show()
            else if (permission == WRITE_CONTACTS)
                Toast.makeText(context, contactsPermErrorMsg, Toast.LENGTH_LONG).show()
            else if (permission == LOCATION_CORSE)
                Toast.makeText(context, locationPermErrorMsg1, Toast.LENGTH_LONG).show()

            permissionListener!!.onPermissionResult(false)
        }
    }

    interface PermissionListener {
        fun onPermissionResult(isGranted: Boolean)
    }

    companion object {


        private val READ_CONTACTS = Manifest.permission.READ_CONTACTS
        private val WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS
        var STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
        var CAMERA = Manifest.permission.CAMERA
        private val READ_SMS = Manifest.permission.READ_SMS
        private val RECEIVE_SMS = Manifest.permission.RECEIVE_SMS
        var LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        var LOCATION_CORSE = Manifest.permission.ACCESS_COARSE_LOCATION
        private val READ_CALENDAR = Manifest.permission.READ_CALENDAR
        private val WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR
        private val ACCOUNTS = Manifest.permission.GET_ACCOUNTS
        var RECORD_AUDIO = Manifest.permission.RECORD_AUDIO


        private val storagePermInfoMsg =
            "App needs this permission to store files on phone's storage. Are you sure you want to deny this permission ?"
        private val storagePermErrorMsg = "Storage permission denied. You can enable permission from settings"

        private val contactsPermInfoMsg =
            "App needs this permission to access phone contacts. Are you sure you want to deny this permission ?"
        private val contactsPermErrorMsg = "Contacts permission denied. You can enable permission from settings"

        private val smsPermInfoMsg =
            "App needs this permission to receive/read SMS for auto detection of OTP. Are you sure you want to deny this permission ?"
        private val smsPermErrorMsg = "SMS permission denied. You can enable permission from settings"

        private val accountsPermInfoMsg =
            "App needs this permission to access Google Account on phone. Are you sure you want to deny this permission ?"
        private val accountsPermErrorMsg = "Contacts permission denied. You can enable permission from settings"

        private val cameraPermInfoMsg =
            "App needs this permission to capture photos using phone's camera. Are you sure you want to deny this permission ?"
        private val cameraPermErrorMsg = "Camera permission denied. You can enable permission from settings"

        private val calendarPermInfoMsg =
            "App needs this permission to access phone's calendar. Are you sure you want to deny this permission ?"
        private val calendarPermErrorMsg = "Calendar permission denied. You can enable permission from settings"

        private val locationPermInfoMsg = "App needs this permission to access your location. Are you sure you want to deny this permission ?"
        private val locationPermErrorMsg = "Location permission denied. You can enable permission from settings"

        private val locationPermInfoMsg1 = "App needs this permission to access your location. Are you sure you want to deny this permission ?"
        private val locationPermErrorMsg1 = "Location permission denied. You can enable permission from settings"
    }
}