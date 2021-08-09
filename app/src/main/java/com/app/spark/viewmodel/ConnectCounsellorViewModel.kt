package com.app.spark.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.app.spark.R
import com.app.spark.activity.counsellor.CounsellorInformationActivity
import com.app.spark.dialogs.YourAgeDiloag
import com.app.spark.models.ConnectCounsellorRequest
import com.app.spark.models.UpdateFlied
import com.app.spark.utils.showToastShort


class ConnectCounsellorViewModel(application: Application) : AndroidViewModel(application) {
    var request = ConnectCounsellorRequest()
    var updateFlied= UpdateFlied()

    fun onProceed(view: View) {
        if(request.preferredName.isNullOrEmpty())
            showToastShort(view.context,view.context.getString(R.string.enter_preferred_name))
        else if(request.yourAge.isNullOrEmpty())
            showToastShort(view.context,view.context.getString(R.string.enter_your_age))
        else if(request.yourIssue.isNullOrEmpty())
            showToastShort(view.context,view.context.getString(R.string.enter_your_issue))
        else if(request.relationshipStatus.isNullOrEmpty()||
            request.relationshipStatus.equals(view.context.getString(R.string.select_status)))
            showToastShort(view.context,view.context.getString(R.string.select_relationship_status))
        /*else if(request.currentMood.isNullOrEmpty())
            showToastShort(view.context,view.context.getString(R.string.select_current_mood))
        else if(request.currentSituation.isNullOrEmpty())
            showToastShort(view.context,view.context.getString(R.string.select_current_mood))*/
        else {
           // Log.d("TAG", "onProceed: "+categoryIdItemPosition.value!!)
            view.context.startActivity(
                Intent(
                    view
                        .context, CounsellorInformationActivity::class.java
                )
            )
        }
    }
    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        updateFlied.charStatus= if (s.length<200) 200-s.length else 0
    }
    fun onYourAgeSelect(view: View){
        YourAgeDiloag.Builder(view.context)
            .setSingleCheck(false)
            .setCallback {
                setYourAge(it)
            }
            .build()
    }
    private fun setYourAge(it: String) {
        updateFlied.ageStatus=it
        request.yourAge=it
    }
}

