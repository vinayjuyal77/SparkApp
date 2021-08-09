package com.app.spark.activity.group_chat_room

import android.R.attr.mimeType
import android.app.Application
import android.content.ContentUris
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
import com.app.spark.models.CommonResponse
import com.app.spark.models.MediaModel
import com.app.spark.network.ApiInterface
import com.app.spark.utils.*
import com.google.gson.Gson
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext


class GroupChatRoomViewModel(application: Application) : AndroidViewModel(application),
    SocketManager.SocketListener, CoroutineScope {
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var chatList: LiveData<List<ChatDetailResponse.Result.Data>>? = null
    var messageReceived = MutableLiveData<ChatDetailResponse.Result.Data>()
    private var isConnected = true
    private var socketManager: SocketManager? = null
    private var otherUserId: String? = null
    private var userId: String? = null
    private var groupName: String? = null
    private var connectedAppDatabase: ConnectedAppDatabase? = null
    var imagesLiveData: MutableList<MediaModel> = mutableListOf()
    var videoLiveData: MutableList<MediaModel> = mutableListOf()
    var docLiveData: MutableList<MediaModel> = mutableListOf()
    var audioLiveData: MutableList<MediaModel> = mutableListOf()
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    fun setInitialData(userId: String?, otherUserId: String?, groupName: String?) {
        this.userId = userId
        this.otherUserId = otherUserId
        this.groupName = groupName
        connectedAppDatabase = ConnectedAppDatabase.getInstance(getApplication())
        mInitializeSocket()
        getChatList()
    }

    fun getChatList() {
        chatList = connectedAppDatabase?.appDao()?.getChatMessage(otherUserId)
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
            socketManager?.addListener(ServerConstant.EVENT_RECEIVE_MESSAGE, onMessageReceived)
            socketManager?.addListener(ServerConstant.EVENT_GET_CHAT_LIST, onReceiveChatList)
            socketManager?.addListener(ServerConstant.EVENT_DELETE_MSG, commonResponse)
            socketManager?.addListener(ServerConstant.EVENT_DELETE_MSG_EVERYONE, commonResponse)
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


    var onMessageReceived = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        try {
            val messageJson = data.getJSONObject("result")
            val newMessage: ChatDetailResponse.Result.Data = Gson().fromJson(
                messageJson.toString(),
                ChatDetailResponse.Result.Data::class.java
            )
            messageReceived.postValue(newMessage)
            newMessage.chatId = otherUserId
            connectedAppDatabase?.appDao()?.insertChatMessage(newMessage)
        } catch (e: JSONException) {
            Log.e(
                "CHAT_ERROR",
                e.message!!
            )
        }
    }


    var onReceiveChatList = Emitter.Listener { args ->
        val response: ChatDetailResponse = Gson().fromJson(
            args[0].toString(),
            ChatDetailResponse::class.java
        )
        for (item in response.result.data) {
            item.chatId = otherUserId
        }
        connectedAppDatabase?.appDao()
            ?.insertChatMessages(response.result.data.sortedBy { it.dateAdded })
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
        getChatListRequest()
    }

    /*
     * *************************************
     * Method Name:- getChatListRequest
     * Parameters :- No Arg
     * Purpose :- Method to hit event to get Chat list
     * *************************************
     * */

    private fun getChatListRequest() {
        val chatListRequestJson = JSONObject()
        try {
            chatListRequestJson.put(ServerConstant.RECEIVER_ID, otherUserId!!.toInt())
            chatListRequestJson.put(ServerConstant.SENDER_ID, userId!!.toInt())
            chatListRequestJson.put(ServerConstant.CHAT_TYPE, "group")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_GET_CHAT_LIST, chatListRequestJson)
    }


    /*
     * *************************************
     * Method Name:- sendMessage
     * Parameters :- String messageType, String mediaType, String uri, String thumbUri
     * Purpose :- Method to send new message
     * *************************************
     * */
    fun sendMessage(
        message: String? = null,
        mediaType: String? = null,
        uri: String? = null,
        thumbUri: String? = null
    ) {
        val sendMessageJson = JSONObject()
        try {
            sendMessageJson.put(ServerConstant.RECEIVER_ID, otherUserId!!.toInt())
            sendMessageJson.put(ServerConstant.SENDER_ID, userId!!.toInt())
            sendMessageJson.put(ServerConstant.CHAT_TYPE, "group")
            sendMessageJson.put(ServerConstant.GROUP_NAME, groupName)
            sendMessageJson.put(
                ServerConstant.MESSAGE,
                message
            )
            if (!message.isNullOrEmpty())
                sendMessageJson.put(ServerConstant.MESSAGE_TYPE, "text")
            else {
                sendMessageJson.put(ServerConstant.MESSAGE_TYPE, "media")
                sendMessageJson.put(ServerConstant.MEDIA_TYPE, mediaType)
            }
            if (mediaType != null && mediaType == "video") {
                sendMessageJson.put(ServerConstant.THUMBNAIL_URL, thumbUri)
            } else {
                sendMessageJson.put(ServerConstant.THUMBNAIL_URL, "")
            }
            if (uri != null) sendMessageJson.put(
                ServerConstant.MEDIA_URL,
                uri
            ) else sendMessageJson.put(ServerConstant.MEDIA_URL, "")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_SEND_MESSAGE, sendMessageJson)
        getChatListRequest()
    }

    private var commonResponse = Emitter.Listener { args ->
        try {
            val response: CommonResponse = Gson().fromJson(
                args[0].toString(),
                CommonResponse::class.java
            )
            if (response.statusCode === 200) {
                getChatListRequest()
            }
        } catch (e: JSONException) {
            Log.e(
                "CHAT_ERROR",
                e.message!!
            )
        }
    }


    fun deleteChatMessagesFromList(msg_id: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("s_id", userId)
            jsonObject.put("msg_id", msg_id)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_DELETE_MSG, jsonObject)
    }

    fun deleteChatMsgEveryone(msg_id: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("id", userId)
            jsonObject.put("msg_id", msg_id)
            jsonObject.put(ServerConstant.CHAT_TYPE, "group")
            jsonObject.put("group_name", groupName)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_DELETE_MSG_EVERYONE, jsonObject)
        Handler(Looper.getMainLooper()).postDelayed({
            getChatListRequest()
        }, 500)
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
    }

    fun clearChat(status: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("s_id", userId)
            jsonObject.put("id", otherUserId)
            jsonObject.put(ServerConstant.CHAT_TYPE, "group")
            jsonObject.put("status", status)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_CLEAR_CHAT, jsonObject)
        Handler(Looper.getMainLooper()).postDelayed({
            getChatListRequest()
        }, 500)
    }

    fun reportChat(status: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("s_id", userId)
            jsonObject.put("id", otherUserId)
            jsonObject.put(ServerConstant.CHAT_TYPE, "group")
            jsonObject.put("status", status)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socketManager?.sendMsg(ServerConstant.EVENT_REPORT_CHAT, jsonObject)
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

    /**
     * Getting All Images Path.
     *
     * Required Storage Permission
     *
     * @return ArrayList with images Path
     */
    internal suspend fun loadImagesfromSDCard(selection: String, list: MutableList<MediaModel>) {
        val columns = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Files.FileColumns.BUCKET_ID,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        val queryUri = MediaStore.Files.getContentUri("external")

        val imagecursor = if (selection == (MediaStore.Files.FileColumns.MIME_TYPE + "=?")) {
            val selectionArgsPdf =
                arrayOf<String?>(MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf"))
            getApplication<Application>().contentResolver.query(
                queryUri,
                columns,
                selection,
                selectionArgsPdf,  // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
            )
        } else {
            getApplication<Application>().contentResolver.query(
                queryUri,
                columns,
                selection,
                null,  // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
            )
        }
        var thumbNail: Bitmap? = null
        if (imagecursor != null) {
            while (imagecursor.moveToNext() && imagecursor.position < 11) {
                val imageId: Long =
                    imagecursor.getLong(imagecursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                val name: String =
                    imagecursor.getString(imagecursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME))
                        ?: "doc_${System.currentTimeMillis()}"
                var path: Uri? = null
                var filePath =
                    imagecursor.getString(imagecursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
                val date: String =
                    imagecursor.getString(imagecursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED))
                val bucketName: String =
                    imagecursor.getString(imagecursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME))?:""
                val mediaType: Int =
                    imagecursor.getInt(imagecursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE))
                val mimeType: String =
                    imagecursor.getString(imagecursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))

                if (mediaType == 3) {
                    path = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        imageId
                    )
                    thumbNail = retriveVideoFrameFromVideo(getApplication(), path)

                }
                val mediaModel = MediaModel(
                    date,
                    imageId,
                    mediaType,
                    mimeType,
                    name,
                    filePath,
                    bucketName,
                    thumbNail
                )
                list.add(mediaModel)
            }
            imagecursor.close()
        }
    }

    fun getAllImages() {
        launch(Dispatchers.IO) {
            loadImagesfromSDCard(
                (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE), imagesLiveData
            )
            loadImagesfromSDCard(
                (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO), videoLiveData
            )
            loadImagesfromSDCard(
                (MediaStore.Files.FileColumns.MIME_TYPE + "=?"), docLiveData
            )
            loadImagesfromSDCard(
                (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO), audioLiveData
            )
        }
    }

    fun uploadMediaAPI(
        header: String,
        userId: String,
        mediaType: String?,
        image: File?
    ) {
        try {
            val map = hashMapOf<String, RequestBody>(
                "user_id" to getRequestBody(userId)
            )

            Coroutines.main {
                val result = ApiInterface(getApplication()).uploadMediaAPI(
                    header, map,
                    if (image != null) {
                        getImageFilePart("chat_media", image)
                    } else {
                        null
                    }
                )
                if (result!!.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        sendMessage(null, mediaType, result.body()?.result, null)
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

    fun removeSocket() {
        if (socketManager?.isConnected!!) {
            socketManager?.disConnect()
        }
        socketManager?.removeListener(Socket.EVENT_CONNECT_ERROR, onConnectError)
        socketManager?.removeListener(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        socketManager?.removeListener(ServerConstant.EVENT_RECEIVE_MESSAGE, onMessageReceived)
        socketManager?.removeListener(ServerConstant.EVENT_GET_CHAT_LIST, onReceiveChatList)
        socketManager?.removeListener(ServerConstant.EVENT_DELETE_MSG, commonResponse)
        socketManager?.removeListener(ServerConstant.EVENT_DELETE_MSG_EVERYONE, commonResponse)

    }
}