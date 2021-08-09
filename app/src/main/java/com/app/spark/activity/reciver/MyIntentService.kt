package com.app.spark.activity.reciver

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.json.JSONObject

// TODO: Rename actions, choose action names that describe tasks that this
// IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
const val ACTION_MATCH = "com.app.spark.activity.reciver.action.match_api"

// TODO: Rename parameters
const val EXTRA_MATCH_ID = "com.app.spark.activity.reciver.extra.PARAM1"
const val EXTRA_MATCH_NAME = "com.app.spark.activity.reciver.extra.PARAM2"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.

 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.

 */
class MyIntentService : IntentService("MyIntentService") {
    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_MATCH -> {
                val param1 = intent.getStringExtra(EXTRA_MATCH_ID)
                val param2 = intent.getStringExtra(EXTRA_MATCH_NAME)
                handleAction(param1, param2)
            }
        }
    }
    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleAction(param1: String?, param2: String?) {
        var resultIntent=Intent(ACTION_MATCH).putExtras(
            Bundle().apply {
                putString(EXTRA_MATCH_ID,param1)
                putString(EXTRA_MATCH_NAME,param2)
            }
        )
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent)
    }
    companion object {
        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        @JvmStatic
        fun startAction(context: Context, extras:Bundle) {
            val intent = Intent(context, MyIntentService::class.java).apply {
                action = ACTION_MATCH
                var json  = JSONObject(extras.getString("noti_data",""))
                putExtra(EXTRA_MATCH_ID, json.getString("from_user"))
                putExtra(EXTRA_MATCH_NAME, json.getString("name"))
            }
            context.startService(intent)
        }
    }
}