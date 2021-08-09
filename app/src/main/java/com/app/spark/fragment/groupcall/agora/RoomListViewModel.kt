package com.app.spark.fragment.groupcall.agora

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.app.spark.R
import com.app.spark.activity.reciver.*
import com.app.spark.constants.ServerConstant
import com.app.spark.constants.ServerConstant.ROOM_CHAT_TYPE
import com.app.spark.constants.ServerConstant.SELECT_TYPE
import com.app.spark.models.ConvertMessageReceiver
import com.app.spark.models.GetListResponse
import com.app.spark.models.RoomData
import com.app.spark.models.USER_INFO
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines
import com.app.spark.utils.SocketManager
import com.app.spark.utils.isNetworkAvailable
import com.google.gson.Gson
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

class RoomListViewModel (application: Application) : AndroidViewModel(application),SocketManager.SocketListener,
    CoroutineScope {
    var response = MutableLiveData<GetListResponse>()
    var listener = MutableLiveData<ArrayList<USER_INFO>>()
    var speaker = MutableLiveData<ArrayList<USER_INFO>>()
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var sucessString = MutableLiveData<String>()
    var successResult = MutableLiveData<Int>()

    var listItemLitnere: ArrayList<USER_INFO> = ArrayList()
    var listItemSpeaker: ArrayList<USER_INFO> = ArrayList()
    var model= MutableLiveData<RoomData>()
    var isSpeakerList: ArrayList<Int> = ArrayList()

    var countText = MutableLiveData<Int>()
    fun getListAPI(roomId: String) {
  //      Log.d("TAG", "getListAPI: "+roomId)
        try {
            val map = hashMapOf<String, Any?>(
                "room_id" to roomId
            )
            Coroutines.main {
                val result = ApiInterface(getApplication())?.getListAPI(map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        try {
                            listItemLitnere.clear()
                            listItemSpeaker.clear()
                            for (i in result.body()?.result!!.users.indices) {
                                if (result.body()?.result!!.users[i].type.equals("listener")) listItemLitnere.add(
                                    result.body()?.result!!.users[i]
                                )
                                else {
                                    listItemSpeaker.add(result.body()?.result!!.users[i])
                                    isSpeakerList.add(result.body()?.result!!.users[i].user_id)
                                }
                            }
                            model.postValue(result.body()?.result!!.room)
                            listener.postValue(listItemLitnere)
                            speaker.postValue(listItemSpeaker)
                            countText.postValue(result.body()?.result!!.requests)
                        }catch (e:Exception)
                        {e.printStackTrace()}
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
    fun setRaiseHandAPI(roomId: Int,hostId:Int) {
        SELECT_TYPE=RAISE_HAND
        try {
            val map = hashMapOf<String, Any?>(
                "room_id" to roomId,
                "user_id" to hostId
            )
            Coroutines.main {
                val result = ApiInterface(getApplication())?.setRaiseHandAPI(map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        Log.d("", "setRaiseHandAPI: ")
                        initialize(1)
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
    fun onRemoveFromRoomAPI(roomId: Int,userId:Int) {
        SELECT_TYPE=REMOVE_USER
        try {
            initialize(userId)
            val map = hashMapOf<String, Any?>(
                "room_id" to roomId,
                "user_id" to userId
            )
            Coroutines.main {
                val result = ApiInterface(getApplication())?.onRemoveFromRoomAPI(map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        sucessString.postValue(REMOVE_USER)
                        initialize(userId)
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
        SELECT_TYPE=END_ROOM
        try {
            initialize(0)
            val map = hashMapOf<String, Any?>(
                "user_id" to userId,
                "room_id" to roomId
            )
            Coroutines.main {
                val result = ApiInterface(getApplication())?.onDeleteRoom(map)
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        Log.d("", "setRaiseHandAPI: ")
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

    fun reportPost(token: String, userIdss: String, postId: String) {
        if (isNetworkAvailable(getApplication())) {
            try {
                val map = HashMap<String, String>()
                map["reported_by"] = userIdss
                map["reported_to"] = postId
                Coroutines.main {
                    val result = ApiInterface(getApplication()).reportUserApi(token, map)
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            errString.postValue(result.body()?.APICODERESULT)
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
    }

    fun onSendListneresType(roomId: String, type: String, selectUserId: String) {
        SELECT_TYPE= ADD_USER
        if (isNetworkAvailable(getApplication())) {
            try {
                initialize(0)
                val map = hashMapOf<String, Any?>(
                    "type" to type,
                    "user_id" to selectUserId,
                    "room_id" to roomId
                )
                Coroutines.main {
                    val result = ApiInterface(getApplication()).onChangeRoomTypeAPI(map)
                    if (result.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            successResult.postValue(result.body()?.statusCode)
                            initialize(0)
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
    }


    /* Socket apply for callback method in room data*/
    private var roomId: String? = null
    private var userId: String? = null
    var messageReceived = MutableLiveData<ConvertMessageReceiver.ResultData>()
    private var socketManager: SocketManager? = null
    private val job = Job()
    fun setInitialData(userId: String,roomId: Int) {
        this.userId = userId
        this.roomId = roomId.toString()
        mInitializeSocket()
    }
    private var isConnected = true
    private fun mInitializeSocket() {
        if (isNetworkAvailable(getApplication())) {
            socketManager = SocketManager.getInstance(getApplication())
            socketManager!!.initialize(this)
            socketManager?.addListener(Socket.EVENT_CONNECT_ERROR, onConnectError)
            socketManager?.addListener(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
            socketManager?.addListener(ServerConstant.RECEIVE_ROOM_MSG, onMessageReceived)
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


    fun initialize(value:Int) {
        val sendMessageJson = JSONObject()
        try {
            sendMessageJson.put(ServerConstant.RECEIVER_ID, roomId)
            sendMessageJson.put(ServerConstant.SENDER_ID, userId!!.toInt())
            sendMessageJson.put(ServerConstant.CHAT_TYPE, ROOM_CHAT_TYPE)
            if(value==1) sendMessageJson.put(
                ServerConstant.MESSAGE,
                RAISE_HOST_TYPE
            ) else sendMessageJson.put(
                ServerConstant.MESSAGE,
                value.toString()
            )
            sendMessageJson.put(ServerConstant.MESSAGE_TYPE, SELECT_TYPE)
            sendMessageJson.put(ServerConstant.MEDIA_TYPE, "mediaType")
            sendMessageJson.put(ServerConstant.THUMBNAIL_URL, "")
            sendMessageJson.put(ServerConstant.MEDIA_URL, "")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Log.e("jsonObject", ":- $sendMessageJson")
        socketManager?.sendMsg(ServerConstant.EVENT_SEND_MESSAGE, sendMessageJson)
    }
    var onMessageReceived = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        Log.e("onMessageReceived", ":- $data")
        try {
            val messageJson = data.getJSONObject("result")
            val newMessage: ConvertMessageReceiver.ResultData = Gson().fromJson(
                messageJson.toString(),
                ConvertMessageReceiver.ResultData ::class.java
            )
            messageReceived.postValue(newMessage)
        } catch (e: JSONException) {
            Log.e(
                "ROOM_listiner_ERROR",
                e.message!!
            )
        }
    }


}
