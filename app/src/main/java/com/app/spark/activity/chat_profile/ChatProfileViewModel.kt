package com.app.spark.activity.chat_profile

import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.database.MergeCursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.spark.R
import com.app.spark.constants.ServerConstant
import com.app.spark.database.ConnectedAppDatabase
import com.app.spark.models.ChatDetailResponse
import com.app.spark.models.ChatUserDetail
import com.app.spark.models.CommonResponse
import com.app.spark.models.MediaModel
import com.app.spark.network.ApiInterface
import com.app.spark.utils.*
import com.google.gson.Gson
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.parser.Parser.EVENT
import kotlinx.coroutines.*
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

class ChatProfileViewModel(application: Application) : AndroidViewModel(application),
    SocketManager.SocketListener, CoroutineScope {
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var userDetails = MutableLiveData<ChatUserDetail.Result>()
    private var isConnected = true
    private var socketManager: SocketManager? = null
    private var otherUserId: String? = null
    private var userId: String? = null
    private var isGroup: Boolean = false
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    fun setInitialData(userId: String?, otherUserId: String?, isGroup: Boolean) {
        this.userId = userId
        this.otherUserId = otherUserId
        this.isGroup = isGroup
        mInitializeSocket()
    }

    /*
     * *************************************
     * Method Name:- mInitializeSocket
     * Parameters :- No Arg
     * Purpose :- Method to Initialise Socket
     * *************************************
     * */
    private fun mInitializeSocket() {
        if (isNetworkAvailable(getApplication())) {
            socketManager = SocketManager.getInstance(getApplication())
            socketManager!!.initialize(this)
            socketManager?.addListener(Socket.EVENT_CONNECT_ERROR, onConnectError)
            socketManager?.addListener(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
            socketManager?.addListener(ServerConstant.EVENT_USER_DETAIL, commonResponse)
            socketManager?.addListener(ServerConstant.EVENT_GROUP_DETAIL, commonResponse)
            socketManager?.connect()
        }
    }

    override fun onConnected() {
        if (!isConnected) {
            //if (null != rideId)
            initializeChat()
            // Toast.makeText(HomeActivity.this, R.string.connect, Toast.LENGTH_LONG).show();
            isConnected = true
        } else {
            Handler(Looper.getMainLooper()).postDelayed({ initializeChat() }, 1000)
        }
    }

    override fun onDisConnected() {
        isConnected = false
    }

    private val onConnectError = Emitter.Listener {
        Log.e("CHAT_ROOM", "Error connecting")
        //Toast.makeText(PersonalChatActivity.this, R.string.error_connect, Toast.LENGTH_LONG).show();
    }

    /*
     * *************************************
     * Method Name:- initializeChat
     * Parameters :- No Arg
     * Purpose :- Method to initialise Chat
     * *************************************
     * */

    private fun initializeChat() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put(ServerConstant.RECEIVER_ID, otherUserId?.toInt())
            jsonObject.put(ServerConstant.SENDER_ID, userId?.toInt())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_INITIATE_CHAT, jsonObject)
        if (isGroup) {
            groupDetail()
        } else userDetail()
    }

    private fun userDetail() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("r_id", otherUserId?.toInt())
            jsonObject.put(ServerConstant.SENDER_ID, userId?.toInt())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_USER_DETAIL, jsonObject)
    }

    private fun groupDetail() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("group_id", otherUserId?.toInt())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_GROUP_DETAIL, jsonObject)
    }

    private var commonResponse = Emitter.Listener { args ->
        try {
            val response: ChatUserDetail = Gson().fromJson(
                args[0].toString(),
                ChatUserDetail::class.java
            )
            userDetails.postValue(response.result)
        } catch (e: JSONException) {
            Log.e(
                "CHAT_ERROR",
                e.message!!
            )
        }
    }

    fun reportChat(status: String, chatType: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("s_id", userId)
            jsonObject.put("id", otherUserId)
            jsonObject.put(ServerConstant.CHAT_TYPE, chatType)
            jsonObject.put("status", status)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_REPORT_CHAT, jsonObject)
    }

    fun blockChat(status: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("s_id", userId)
            jsonObject.put("r_id", otherUserId)
            jsonObject.put("status", status)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_BLOCK_CHAT, jsonObject)
    }

    fun exitGroupChat(status: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("user_id", userId)
            jsonObject.put("group_id", otherUserId)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_EXIT_GROUP, jsonObject)
    }

    fun removeSocket() {
        if (socketManager?.isConnected!!) {
            socketManager?.disConnect()
        }
        socketManager?.removeListener(Socket.EVENT_CONNECT_ERROR, onConnectError)
        socketManager?.removeListener(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        socketManager?.removeListener(ServerConstant.EVENT_USER_DETAIL, commonResponse)
        socketManager?.removeListener(ServerConstant.EVENT_GROUP_DETAIL, commonResponse)

    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        coroutineContext?.cancel()
    }
}