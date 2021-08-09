package com.app.spark.activity.reciver

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.json.JSONObject

const val ACTION_ROOM = "com.app.spark.activity.reciver.action.room_api"
const val EXTRA_ROOM_TYPE = "com.app.spark.activity.reciver.extra.ROOM1"
const val ADD_REQUEST="addRequest"
const val ACCEPTED_REQUEST="acceptRequest"
const val ADD_TO_ROOM="addToRoom"
const val REMOVE_FROM_ROOM="removeFromRoom"
// for socket extension
const val ADD_USER="addUser"
const val RAISE_HAND="raiseHand"
const val REMOVE_USER="removeUser"
const val END_ROOM="endRoom"
const val REQUEST_ACCEPTED="requestAccepted"
const val RAISE_HOST_TYPE="raiseHandHost"

class AgoraReciverService : IntentService("AgoraReciverService") {
    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_ROOM -> {
                val param1 = intent.getStringExtra(EXTRA_ROOM_TYPE)
                handleAction(param1)
            }
        }
    }
    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleAction(param1: String?) {
        var resultIntent= Intent(ACTION_ROOM).putExtras(
            Bundle().apply {
                putString(EXTRA_ROOM_TYPE,param1)
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
        fun startAction(context: Context, extras: Bundle) {
            val intent = Intent(context, AgoraReciverService::class.java).apply {
                action = ACTION_ROOM
                var json  = JSONObject(extras.getString("noti_data",""))
                putExtra(EXTRA_ROOM_TYPE, json.getString("type"))
            }
            context.startService(intent)
        }
    }
}