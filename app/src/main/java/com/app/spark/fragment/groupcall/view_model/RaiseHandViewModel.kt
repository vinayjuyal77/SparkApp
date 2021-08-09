package com.app.spark.fragment.groupcall.view_model

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.app.spark.R
import com.app.spark.activity.reciver.REQUEST_ACCEPTED
import com.app.spark.constants.ServerConstant
import com.app.spark.constants.ServerConstant.ROOM_CHAT_TYPE
import com.app.spark.models.*
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines
import com.app.spark.utils.SocketManager
import com.app.spark.utils.isNetworkAvailable
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

class RaiseHandViewModel (application: Application) : AndroidViewModel(application),SocketManager.SocketListener,
    CoroutineScope {
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var tagList = MutableLiveData<String>()
    var activeSuccess = MutableLiveData<ArrayList<RaiseHandListResponse.ActiveUser>>()
    fun getRequestList(roomId:Int,userId:Int) {
        if (isNetworkAvailable(getApplication())) {
            val map = hashMapOf<String, Any?>(
                "room_id" to roomId.toString(),
                "user_id" to userId.toString()
            )
            Coroutines.main {
                try {
                    val result = ApiInterface(getApplication()).getRequestListRoom(map)
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            activeSuccess.postValue(result.body()!!.response)
                        } else {
                            errString.postValue(result.body()?.APICODERESULT)
                        }
                    } else {
                        errRes.postValue(R.string.something_went_wrong)
                    }
                } catch (ex: Exception) {
                    errRes.postValue(R.string.something_went_wrong)
                }
            }
        }
    }

    fun allowRoomRequest(id: Int, roomId: Int, useridd: Int) {
        if (isNetworkAvailable(getApplication())) {
            val map = hashMapOf<String, Any?>(
                "room_id" to roomId.toString(),
                "user_id" to useridd.toString(),
                "id" to id.toString()
            )
            Coroutines.main {
                try {
                    val result = ApiInterface(getApplication()).setAllowRoomRequest(map)
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            errRes.postValue(result.body()?.statusCode)
                            initialize()
                        } else {
                            errString.postValue(result.body()?.APICODERESULT)
                        }
                    } else {
                        errRes.postValue(R.string.something_went_wrong)
                    }
                } catch (ex: Exception) {
                    errRes.postValue(R.string.something_went_wrong)
                }
            }
        }
    }

    /* Socket apply for callback method in room data*/
    private var roomId: String? = null
    private var userId: String? = null
    var messageReceived = MutableLiveData<ChatDetailResponse.Result.Data>()
    private var socketManager: SocketManager? = null
    private val job = Job()
    private var isConnected = true

    fun setInitialData(userId: String,roomId: Int) {
        this.userId = userId
        this.roomId = roomId.toString()
        mInitializeSocket()
    }
    private fun mInitializeSocket() {
        if (isNetworkAvailable(getApplication())) {
            socketManager = SocketManager.getInstance(getApplication())
            socketManager!!.initialize(this)
            socketManager?.addListener(Socket.EVENT_CONNECT_ERROR, onConnectError)
            socketManager?.addListener(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
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
            Handler(Looper.getMainLooper()).postDelayed({
                initializeChat()
            }, 1000)
        }
    }

    override fun onDisConnected() {
        isConnected = false
    }
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
    private val onConnectError = Emitter.Listener {
        Log.e("onConnectError", "Error connecting")
    }
    private fun initializeChat() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put(ServerConstant.RECEIVER_ID, roomId!!.toInt())
            jsonObject.put(ServerConstant.SENDER_ID, userId?.toInt())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_INITIATE_CHAT, jsonObject)
    }

    fun initialize() {
        val sendMessageJson = JSONObject()
        try {
            sendMessageJson.put(ServerConstant.RECEIVER_ID, roomId)
            sendMessageJson.put(ServerConstant.SENDER_ID, userId!!.toInt())
            sendMessageJson.put(ServerConstant.CHAT_TYPE, ROOM_CHAT_TYPE)
            sendMessageJson.put(
                ServerConstant.MESSAGE,
                ""
            )
            sendMessageJson.put(ServerConstant.MESSAGE_TYPE, REQUEST_ACCEPTED)
            sendMessageJson.put(ServerConstant.MEDIA_TYPE, "mediaType")
            sendMessageJson.put(ServerConstant.THUMBNAIL_URL, "")
            sendMessageJson.put(ServerConstant.MEDIA_URL, "")

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.e("jsonObject", ":- $sendMessageJson")
        socketManager?.sendMsg(ServerConstant.EVENT_SEND_MESSAGE, sendMessageJson)
    }


}