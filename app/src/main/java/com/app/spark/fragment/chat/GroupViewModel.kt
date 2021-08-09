package com.app.spark.fragment.chat

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.app.spark.constants.ServerConstant
import com.app.spark.models.ConversationListResponse
import com.app.spark.utils.SocketManager
import com.app.spark.utils.isNetworkAvailable
import com.google.gson.Gson
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class GroupViewModel(application: Application) : AndroidViewModel(application),
    SocketManager.SocketListener {
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var chatList = MutableLiveData<List<ConversationListResponse.Result>>()
    private var isConnected = true
    private var socketManager: SocketManager? = null
    private var userId: String? = null
    private var group: Int? = null
    fun setInitialData(userId: String?, group: Int?) {
        this.userId = userId
        this.group = group
    }

    /*
     * *************************************
     * Method Name:- mInitializeSocket
     * Parameters :- No Arg
     * Purpose :- Method to Initialise Socket
     * *************************************
     * */
    fun mInitializeSocket() {
        if (isNetworkAvailable(getApplication())) {
            socketManager = SocketManager.getInstance(getApplication())
            socketManager!!.initialize(this)
            socketManager?.addListener(Socket.EVENT_CONNECT_ERROR, onConnectError)
            socketManager?.addListener(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
            socketManager?.addListener(ServerConstant.EVENT_CONVERSATION_LIST, onConversationList)
            socketManager?.connect()
        }
    }

    override fun onConnected() {
        if (!isConnected) {
            //if (null != rideId)
            initializeConversationList()
            // Toast.makeText(HomeActivity.this, R.string.connect, Toast.LENGTH_LONG).show();
            isConnected = true
        } else {
            Handler(Looper.getMainLooper()).postDelayed({ initializeConversationList() }, 1000)
        }
    }

    override fun onDisConnected() {
        isConnected = false
    }

    private val onConnectError = Emitter.Listener {
        Log.e("CHAT_ROOM", "Error connecting")
        //Toast.makeText(PersonalChatActivity.this, R.string.error_connect, Toast.LENGTH_LONG).show();
    }

    fun initializeConversationList() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("user_id", userId)
            jsonObject.put(ServerConstant.CHAT_TYPE, "group")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_CONVERSATION_LIST, jsonObject)
    }

    private var onConversationList = Emitter.Listener { args ->
        try {
            val response: ConversationListResponse = Gson().fromJson(
                args[0].toString(),
                ConversationListResponse::class.java
            )
            if (!response.result.isNullOrEmpty() && response.result[0].chatType.equals(
                    "group",
                    true
                )
            )
                chatList.postValue(response.result)
            else  chatList.postValue(null)
        } catch (e: JSONException) {
            Log.e(
                "CHAT_ERROR",
                e.message!!
            )
        }
    }

    fun muteChatFromList(otherUserId: String, status: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("s_id", userId)
            jsonObject.put("group_id", otherUserId)
            jsonObject.put(ServerConstant.CHAT_TYPE, "group")
            jsonObject.put("status", status)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_MUTE_NOTIFICATION, jsonObject)
        Handler(Looper.getMainLooper()).postDelayed({
            initializeConversationList()
        }, 500)
    }

    fun pinUsers(otherUserId: String, status: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("s_id", userId)
            jsonObject.put("group_id", otherUserId)
            jsonObject.put(ServerConstant.CHAT_TYPE, "group")
            jsonObject.put("status", status)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_PIN_USERS, jsonObject)
        Handler(Looper.getMainLooper()).postDelayed({
            initializeConversationList()
        }, 500)
    }

    fun deleteGroup(otherUserId: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("group_id", otherUserId)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_DELETE_GROUP, jsonObject)
        Handler(Looper.getMainLooper()).postDelayed({
            initializeConversationList()
        }, 500)
    }


    fun removeSocket() {
        if (socketManager?.isConnected!!) {
            socketManager?.disConnect()
        }
        socketManager?.removeListener(Socket.EVENT_CONNECT_ERROR, onConnectError)
        socketManager?.removeListener(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        socketManager?.removeListener(ServerConstant.EVENT_CONVERSATION_LIST, onConversationList)
    }
}