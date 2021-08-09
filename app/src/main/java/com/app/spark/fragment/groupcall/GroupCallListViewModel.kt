package com.app.spark.fragment.groupcall

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.app.spark.R
import com.app.spark.activity.reciver.ADD_USER
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

class GroupCallListViewModel(application: Application) : AndroidViewModel(application),SocketManager.SocketListener,
    CoroutineScope {
    var response = MutableLiveData<GetRoomResponse>()
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    fun getRoomListAPI(userID: String,offset:String) {
        try {
            /*map["offset"] = offset
            map["limit"] = "10"*/
            val map = hashMapOf<String, Any?>(
                "user_id" to userID
            )
            Coroutines.main {
                val result = ApiInterface(getApplication())?.getRoomListAPI(map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        response.postValue(result.body())
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

    fun onDeleteRoom(userId: Int?, roomId: Int) {
        try {
            val map = hashMapOf<String, Any?>(
                "user_id" to userId,
                "room_id" to roomId
            )
            Coroutines.main {
                val result = ApiInterface(getApplication())?.onDeleteRoom(map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        errRes.postValue(result.body()?.statusCode)
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


    var successAddToUser = MutableLiveData<Int>()
    fun addUserToRoom(userId:Int,roomId:Int,userArray:ArrayList<Int>) {
        this.userId = userId.toString()
        this.roomId = roomId.toString()
        if (isNetworkAvailable(getApplication())) {
            val map = hashMapOf<String, Any?>(
                "user_id" to userId,
                "room_id" to roomId,
                "users" to userArray
            )
            Coroutines.main {
                try {
                    val result = ApiInterface(getApplication()).addUserToRoom(map)
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            successAddToUser.postValue(result.body()?.statusCode)
                            mInitializeSocket()
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
        initialize(userId.toString(), roomId!!.toInt())
    }

    fun initialize(userId:String,roomId:Int) {
        val sendMessageJson = JSONObject()
        try {
            sendMessageJson.put(ServerConstant.RECEIVER_ID, roomId)
            sendMessageJson.put(ServerConstant.SENDER_ID, userId!!.toInt())
            sendMessageJson.put(ServerConstant.CHAT_TYPE, ROOM_CHAT_TYPE)
            sendMessageJson.put(
                ServerConstant.MESSAGE,
                ""
            )
            sendMessageJson.put(ServerConstant.MESSAGE_TYPE, ADD_USER)
            sendMessageJson.put(ServerConstant.MEDIA_TYPE, "")
            sendMessageJson.put(ServerConstant.THUMBNAIL_URL, "")
            sendMessageJson.put(ServerConstant.MEDIA_URL, "")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.e("jsonObject", ":- $sendMessageJson")
        socketManager?.sendMsg(ServerConstant.EVENT_SEND_MESSAGE, sendMessageJson)
    }
}