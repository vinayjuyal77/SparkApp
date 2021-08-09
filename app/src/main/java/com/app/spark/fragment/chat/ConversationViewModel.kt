package com.app.spark.fragment.chat

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.app.spark.constants.ServerConstant
import com.app.spark.models.CommonResponse
import com.app.spark.models.ConversationListResponse
import com.app.spark.utils.SocketManager
import com.app.spark.utils.isNetworkAvailable
import com.google.gson.Gson
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ConversationViewModel(application: Application) : AndroidViewModel(application),
    SocketManager.SocketListener {
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var chatList = MutableLiveData<List<ConversationListResponse.Result>>()
    private var isConnected = true
    private var socketManager: SocketManager? = null
    private var userId: String? = null
    private var group: Int? = null
    private var isChatDeleted: Boolean = false

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
            socketManager?.addListener(ServerConstant.EVENT_DELETE_NORMAL, commonResponse)
            socketManager?.connect()
        }
    }

    override fun onConnected() {
        if (isNetworkAvailable(getApplication())) {
            if (!isConnected) {

                //if (null != rideId)
                initializeConversationList()

                // Toast.makeText(HomeActivity.this, R.string.connect, Toast.LENGTH_LONG).show();
                isConnected = true
            } else {
                Handler(Looper.getMainLooper()).postDelayed({ initializeConversationList() }, 1000)
            }
        }
    }

    override fun onDisConnected() {
        isConnected = false
    }

    private val onConnectError = Emitter.Listener {
        Log.e("CHAT_ROOM", "Error connecting")
        //Toast.makeText(PersonalChatActivity.this, R.string.error_connect, Toast.LENGTH_LONG).show();
    }

    private fun initializeConversationList() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("user_id", userId)
            jsonObject.put(ServerConstant.CHAT_TYPE, "normal")
            jsonObject.put(
                "group",
                if (group == 0) "personal" else if (group == 1) "professional" else "public"
            )
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
            if (!response.result.isNullOrEmpty() && response.result[0].groupType.equals(
                    "personal",
                    true
                )
            )
                chatList.postValue(response.result)
            else if (isChatDeleted) {
                isChatDeleted = false
                chatList.postValue(response.result)
            }else chatList.postValue(null)
        } catch (e: JSONException) {
            Log.e(
                "CHAT_ERROR",
                e.message!!
            )
        }
    }

    private var commonResponse = Emitter.Listener { args ->
        try {
            val response: CommonResponse = Gson().fromJson(
                args[0].toString(),
                CommonResponse::class.java
            )
            if (response.statusCode === 200) {
                initializeConversationList()
            }
        } catch (e: JSONException) {
            Log.e(
                "CHAT_ERROR",
                e.message!!
            )
        }
    }

    fun deleteChatFromList(otherUserId: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("id", userId)
            jsonObject.put("users_ids", otherUserId)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_DELETE_NORMAL, jsonObject)
        isChatDeleted = true
        Handler(Looper.getMainLooper()).postDelayed({
            initializeConversationList()
        }, 500)
    }

    fun muteChatFromList(otherUserId: String, status: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("s_id", userId)
            jsonObject.put("r_id", otherUserId)
            jsonObject.put(ServerConstant.CHAT_TYPE, "normal")
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
            jsonObject.put("r_id", otherUserId)
            jsonObject.put(ServerConstant.CHAT_TYPE, "normal")
            jsonObject.put("status", status)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_PIN_USERS, jsonObject)
        Handler(Looper.getMainLooper()).postDelayed({
            initializeConversationList()
        }, 500)
    }

    fun removeSocket() {
        if (isNetworkAvailable(getApplication())) {
            if (socketManager?.isConnected!!) {
                socketManager?.disConnect()
            }
            socketManager?.removeListener(Socket.EVENT_CONNECT_ERROR, onConnectError)
            socketManager?.removeListener(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
            socketManager?.removeListener(
                ServerConstant.EVENT_CONVERSATION_LIST,
                onConversationList
            )
            socketManager?.removeListener(ServerConstant.EVENT_DELETE_NORMAL, commonResponse)
        }
    }
}