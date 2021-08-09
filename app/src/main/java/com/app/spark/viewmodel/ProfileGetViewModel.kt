package com.app.spark.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import android.content.Context
import android.util.Log
import com.app.spark.R
import com.app.spark.constants.PrefConstant
import com.app.spark.database.ConnectedAppDatabase
import com.app.spark.models.EditBioResponse
import com.app.spark.models.ProfileGetResponse
import com.app.spark.models.ProfileGetResponse.ResultProfile.PostArr
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines
import com.app.spark.utils.SharedPrefrencesManager
import com.app.spark.utils.getImageFilePart
import com.app.spark.utils.getRequestBody
import okhttp3.RequestBody
import java.io.File

class ProfileGetViewModel(application: Application) : AndroidViewModel(application) {
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var response = MutableLiveData<ProfileGetResponse>()
    var responseEditBio = MutableLiveData<EditBioResponse>()

    private var connectedAppDatabase: ConnectedAppDatabase? = null
    private lateinit var pref: SharedPrefrencesManager

    var profilelistner : ProfileListerner? = null

    fun getUserProfileData(token: String, viewUserId: String, loginId: String) {
        try {
            val map = HashMap<String, String>()
            map["user_id"] = viewUserId
            map["login_id"] = loginId
            Coroutines.main {
                val result = ApiInterface(getApplication())?.getProfileApi(token, map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        response.postValue(result.body())


                       // profileGetResponse.


                    } else {
                        errString.postValue(result.body()?.aPICODERESULT)
                    }
                } else {
                    errRes.postValue(R.string.something_went_wrong)
                }
            }
        } catch (ex: Exception) {
            errRes.postValue(R.string.something_went_wrong)
        }
    }


    fun getLcoalMainData(context : Context, profileListerner: ProfileListerner)
    {



        connectedAppDatabase = ConnectedAppDatabase.getInstance(getApplication())


        pref = SharedPrefrencesManager.getInstance(getApplication())
    //    Log.e("+++", connectedAppDatabase?.appDao()?.getProfileData().toString())
        if(connectedAppDatabase?.appDao()?.getProfileData()!=null) {
            response.postValue(connectedAppDatabase?.appDao()?.getProfileData())
        }
        else
        {

            profileListerner?.listner()
        }
    }


    fun getLcoalData(context : Context)
    {



        connectedAppDatabase = ConnectedAppDatabase.getInstance(getApplication())


        pref = SharedPrefrencesManager.getInstance(getApplication())
        //    Log.e("+++", connectedAppDatabase?.appDao()?.getProfileData().toString())
        if(connectedAppDatabase?.appDao()?.getProfileData()!=null) {
            response.postValue(connectedAppDatabase?.appDao()?.getProfileData())
        }
        else
        {

            profilelistner?.listner()
        }
    }

    fun editBioAdd(
        header: String,
        userId: String,
        bio: String,
        image: String,
        name: String,
        gender: String,
        dob: String,
        location: String,
        latitude: String,
        longitude: String
    ) {
        try {
            val map = HashMap<String, RequestBody>()
            map["user_id"] = getRequestBody(userId)
            map["user_bio"] = getRequestBody(bio)
            map["name"] = getRequestBody(name)
            map["gender"] = getRequestBody(gender)
            map["dob"] = getRequestBody(dob)
            map["location"] = getRequestBody(location)
            map["latitude"] = getRequestBody(latitude)
            map["longitude"] = getRequestBody(longitude)


            Coroutines.main {
                val result = ApiInterface(getApplication())?.editBioAPI(
                    header, map,
                    if (image.trim().isNotEmpty()) {
                        getImageFilePart("profile_pic", File(image))
                    } else {
                        null
                    }
                )
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        responseEditBio.postValue(result.body())

                    } else {
                        errString.postValue(result.body()?.aPICODERESULT)
                    }
                } else {
                    errRes.postValue(R.string.something_went_wrong)
                }
            }
        } catch (ex: Exception) {
            errRes.postValue(R.string.something_went_wrong)
        }
    }

    fun followUnfollowApi(
        token: String,
        userId: String,
        otherUserId: String,
        type: String = "Follow"
    ) {
        try {
            val map = HashMap<String, String>()
            map["follow_by"] = userId
            map["follow_to"] = otherUserId
            map["follow_group"] = "public"
            map["type"] = type
            Coroutines.main {
                val result = ApiInterface(getApplication())?.followUnfollowAPI(token, map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        errString.postValue(result.body()?.APICODERESULT)
                        getUserProfileData(token, otherUserId, userId)
                    } else {
                        errString.postValue(result.body()?.APICODERESULT)
                    }
                } else {
                    errRes.postValue(R.string.something_went_wrong)
                }
            }
        } catch (ex: Exception) {
            errRes.postValue(R.string.something_went_wrong)
        }
    }


    public interface ProfileListerner
    {
        fun listner()
    }


    fun updateDeviceTokenApi(
        header: String?,
        userId: String?,
        deviceToken: String?
    ) {

        val map = hashMapOf<String, String?>(
            "user_id" to userId,
            "device_type" to "android",
            "device_token" to deviceToken
        )
        Coroutines.main {
            try {
                val result = ApiInterface(getApplication())?.updateDeviceTokenAPI(
                    header, map
                )
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode != 200) {
                        updateDeviceTokenApi(header, userId, deviceToken)
                    }
                } else {
                    updateDeviceTokenApi(header, userId, deviceToken)
                }
            } catch (ex: Exception) {

            }
        }

    }

}
