package com.app.spark.activity.group_chat_room

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.spark.R
import com.app.spark.activity.chat_profile.ChatProfileActivity
import com.app.spark.activity.chatroom.AudioAdapter
import com.app.spark.activity.chatroom.DocAdapter
import com.app.spark.activity.chatroom.PhotoAdapter
import com.app.spark.activity.chatroom.VideoAdapter
import com.app.spark.constants.IntentConstant
import com.app.spark.constants.PrefConstant
import com.app.spark.databinding.ActivityChatRoomBinding
import com.app.spark.dialogs.BlockChatDialog
import com.app.spark.dialogs.ClearChatDialog
import com.app.spark.dialogs.MuteNotificationDialog
import com.app.spark.dialogs.ReportChatDialog
import com.app.spark.interfaces.AudioPlayerListener
import com.app.spark.interfaces.ChatMessageSelectedListener
import com.app.spark.interfaces.OnConnectionTypeSelected
import com.app.spark.interfaces.OnItemSelectedInterface
import com.app.spark.models.ChatDetailResponse
import com.app.spark.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cancan.Utility.PermissionsUtil
import com.google.android.material.tabs.TabLayout
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.dialog_delete_chat_msg.*
import java.io.File
import java.io.IOException

class GroupChatRoomActivity : BaseActivity(), ChatMessageSelectedListener, AudioPlayerListener,
    OnItemSelectedInterface {
    private lateinit var binding: ActivityChatRoomBinding
    lateinit var viewModel: GroupChatRoomViewModel
    private var chatID = ""
    private var name = ""
    private var chatImg = ""
    private var userId = ""
    lateinit var pref: SharedPrefrencesManager
    private var chatAdapter: GroupChatAdapter? = null
    private var chatList: MutableList<ChatDetailResponse.Result.Data> = mutableListOf()
    private var mPlayer: MediaPlayer? = null
    private var popup: EmojiPopup? = null
    private var permissionsUtil: PermissionsUtil? = null
    private var filePath: File? = null
    private var token = ""
    private var mRecorder: MediaRecorder? = null
    private var audioMessageFile: String? = null
    private var textOptionsLayout: LinearLayout? = null
    private var chatMediaDownloader: ChatMediaDownloader? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_room)
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                GroupChatRoomViewModel::class.java
            )
        popup = EmojiPopup.Builder
            .fromRootView(binding.root).build(binding.edtChatMessage);
        permissionsUtil = PermissionsUtil(this)
        permissionsUtil?.askPermission(
            this,
            PermissionsUtil.STORAGE,
            object : PermissionsUtil.PermissionListener {
                override fun onPermissionResult(isGranted: Boolean) {
                    if (isGranted) {
                        viewModel.getAllImages()
                    }
                }

            })
        chatMediaDownloader = ChatMediaDownloader(this)
        pref = SharedPrefrencesManager.getInstance(this)
        userId = pref.getString(PrefConstant.USER_ID, "")!!
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "")!!
        intentValueGet()
      //  if (isNetworkAvailable(this@GroupChatRoomActivity))
            viewModel.setInitialData(userId, chatID, name)
        chatAdapter = GroupChatAdapter(this, chatList, userId, this, this)
        binding.rvChatList.adapter = chatAdapter
        sendTextMessage()
        onAttachmentsTabSelection()

    }

    override fun onResume() {
        super.onResume()
        initlizeViewModelObserver()
    }

    private fun intentValueGet() {
        chatID = intent.getStringExtra(IntentConstant.CHAT_ID).toString()
        name = intent.getStringExtra(IntentConstant.CHAT_NAME).toString()
        chatImg = intent.getStringExtra(IntentConstant.CHAT_IMG).toString()
        setChatUi()
        binding.llSelRoomColor.setBackgroundResource(R.drawable.bg_chat_yellow_card)
        binding.imgMic.setBackgroundResource(R.drawable.bg_circle_yellow)
        binding.llAttachments.setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.yellow_text_color
            )
        )
    }

    private fun setChatUi() {
        binding.tvName.text = name
        Glide.with(this)
            .load(chatImg)
            .apply(RequestOptions().centerCrop())
            .into(binding.imgChat)
        binding.tvChatType.text = getString(R.string.group)
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.imgChat.setOnClickListener {
            val intent = Intent(this, ChatProfileActivity::class.java)
            intent.putExtra(IntentConstant.CHAT_ID, chatID)
            intent.putExtra(IntentConstant.CHAT_NAME, name)
            intent.putExtra(IntentConstant.CHAT_IMG, chatImg)
            intent.putExtra(IntentConstant.CHAT_TYPE, 3)
            intent.putExtra(IntentConstant.FROM_CHAT, true)
            startActivity(intent)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun sendTextMessage() {
        /* binding.imgMic.setOnClickListener {
             if (binding.edtChatMessage.text.toString().isNotEmpty()) {
                 viewModel.sendMessage(message = binding.edtChatMessage.text.toString())
                 binding.edtChatMessage.text?.clear()
             } else audioRecordPermission()
         }*/
        binding.imgMic.setOnTouchListener(View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (binding.edtChatMessage.text.toString().isNotEmpty()) {
                        binding.edtChatMessage.text?.clear()
                    } else {
                        stopRecording(false)
                    }
                    // Do what you want
                    true
                }
                MotionEvent.ACTION_DOWN -> {
                    if (binding.edtChatMessage.text.toString().isNotEmpty()) {
                        viewModel.sendMessage(message = binding.edtChatMessage.text.toString())
                    } else {
                        audioRecordPermission()
                    }
                    // Do what you want
                    true
                }
                else -> false
            }
        })
        binding.edtChatMessage.addTextChangedListener {
            if (!it.isNullOrEmpty()) {
                binding.imgMic.setImageResource(R.drawable.ic_comment_send)
            } else {
                binding.imgMic.setImageResource(R.drawable.ic_chat_room_voice)
            }
        }
        binding.imgEmojy.setOnClickListener {
            /* binding.edtChatMessage.requestFocus()
             binding.edtChatMessage.isFocusableInTouchMode = true
             binding.edtChatMessage.text!!.append("\ud83d\ude01");
             val imm =
                 getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
             imm.showSoftInput(binding.edtChatMessage, InputMethodManager.SHOW_FORCED)*/
            popup?.toggle()
        }
        binding.imgAttach.setOnClickListener {
            hideKeyboard(this)
            binding.llAttachments.visibility = View.VISIBLE
            binding.rvAttachments.adapter = PhotoAdapter(
                this@GroupChatRoomActivity,
                viewModel.imagesLiveData,
                this@GroupChatRoomActivity
            )
        }

        binding.imgStopRecording.setOnClickListener {
            stopRecording(false)
        }
        binding.txtCancelRecording.setOnClickListener {
            stopRecording(true)
        }

        binding.imgCammera.setOnClickListener {
            permissionsUtil?.askPermissions(
                this,
                PermissionsUtil.STORAGE,
                PermissionsUtil.CAMERA,
                object : PermissionsUtil.PermissionListener {
                    override fun onPermissionResult(isGranted: Boolean) {
                        if (isGranted) {
                            ImagePickerUtil.selectImage(
                                this@GroupChatRoomActivity,
                                { imageFile, tag ->
                                    filePath = imageFile
                                    viewModel.uploadMediaAPI(token, userId, "photo", filePath)
                                    hideView(binding.llAttachments)
                                },
                                "photo", true, false, false,false
                            )
                        }
                    }

                })
        }

        binding.imgChatOptionMenu.setOnClickListener {
            showPopUpWindow()
        }
    }

    private fun audioRecordPermission() {
        permissionsUtil?.askPermissions(
            this,
            PermissionsUtil.STORAGE,
            PermissionsUtil.RECORD_AUDIO,
            object : PermissionsUtil.PermissionListener {
                override fun onPermissionResult(isGranted: Boolean) {
                    if (isGranted) {
                        startRecording()
                    }
                }

            })
    }

    private fun onAttachmentsTabSelection() {
        binding.rvAttachments.addItemDecoration(GridSpacingItemDecoration(4, 20, false, 0))
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.rvAttachments.adapter = PhotoAdapter(
                            this@GroupChatRoomActivity,
                            viewModel.imagesLiveData,
                            this@GroupChatRoomActivity
                        )
                    }
                    1 -> {
                        binding.rvAttachments.adapter = VideoAdapter(
                            this@GroupChatRoomActivity,
                            viewModel.videoLiveData,
                            this@GroupChatRoomActivity
                        )
                    }
                    2 -> {
                        binding.rvAttachments.adapter = DocAdapter(
                            this@GroupChatRoomActivity,
                            viewModel.docLiveData,
                            this@GroupChatRoomActivity
                        )
                    }
                    3 -> {
                        binding.rvAttachments.adapter = AudioAdapter(
                            this@GroupChatRoomActivity,
                            viewModel.audioLiveData,
                            this@GroupChatRoomActivity
                        )
                    }
                }
            }

        })
    }


    private fun initlizeViewModelObserver() {

        viewModel.chatList?.observe(this, {
            if (!it.isNullOrEmpty()) {
                chatList.clear()
                chatList.addAll(it)
                chatAdapter?.notifyDataSetChanged()
                scrollToBottom()
            } else {
                chatList.clear()
                chatAdapter?.notifyDataSetChanged()
            }
        })
        viewModel.messageReceived.observe(this, {
            if (it != null) {
                chatList.add(it)
                chatAdapter?.notifyDataSetChanged()
                scrollToBottom()
            }
        })

        viewModel.errString.observe(this, { err: String? ->
            if (err != null)
                showToastShort(this, err)
        })

        viewModel.errRes.observe(this, { err: Int ->
            if (err != null)
                showSnackBar(binding.root, getString(err))
        })
    }

    /*
     * *************************************
     * Method Name:- scrollToBottom
     * Parameters :- No Arg
     * Purpose :- Method to scroll to bottom of the list
     * *************************************
     * */
    private fun scrollToBottom() {
        chatAdapter?.itemCount?.minus(1)?.let { binding.rvChatList.scrollToPosition(it) }
    }


    override fun onBackPressed() {
        when {
            binding.llAttachments.isVisible -> {
                binding.llAttachments.visibility = View.GONE
            }
            textOptionsLayout?.isVisible == true -> {
                textOptionsLayout?.visibility = View.GONE
            }
            else -> super.onBackPressed()
        }
    }

    override fun chatMessageSelected(chatItem: ChatDetailResponse.Result.Data?, position: Int) {

    }

    override fun deleteMessage(chatItem: ChatDetailResponse.Result.Data?, position: Int) {
        textOptionsLayout?.visibility = View.GONE
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_delete_chat_msg)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.tvDelete.setBackgroundResource(R.drawable.bg_chat_delete_yellow)
        dialog.show()
        dialog.tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.tvDelete.setOnClickListener {
            dialog.dismiss()
            viewModel.deleteChatMsgEveryone(chatItem?.id.toString())
        }
    }

    override fun copyMessage(chatItem: ChatDetailResponse.Result.Data?, position: Int) {
        textOptionsLayout?.visibility = View.GONE
        copyText(this, chatItem?.msg.toString())
        showToastLong(this, getString(R.string.message_copied))
    }

    override fun textMessageClick(
        chatItem: ChatDetailResponse.Result.Data?,
        position: Int,
        view: LinearLayout
    ) {
        textOptionsLayout?.visibility = View.GONE
        textOptionsLayout = view
        textOptionsLayout?.visibility = View.VISIBLE
    }

    override fun downloadMedia(downloadUrl: String) {
        chatMediaDownloader?.setupAppResources(downloadUrl)
    }

    override fun playAudio(dataItem: ChatDetailResponse.Result.Data?, position: Int) {
        dataItem?.url?.let { startPlaying(it) }
    }

    private fun startPlaying(urlAudioFile: String) {
        mPlayer = MediaPlayer()
        try {
            mPlayer!!.setDataSource(urlAudioFile)
            mPlayer!!.prepare()
            mPlayer!!.start()
        } catch (e: IOException) {
            Log.e("LOG_TAG", "prepare() failed")
        }
    }

    override fun onStop() {
        super.onStop()
        stopPlaying()
    }

    override fun onDestroy() {
        viewModel.removeSocket()
        super.onDestroy()
    }

    override fun onItemSelected(position: Int, totalSize: Int?) {
        if (position === (totalSize!! - 1)) {
            askPermission()
        } else {
            selectedTabItem(position)
        }
    }

    private fun askPermission() {
        permissionsUtil?.askPermission(this, PermissionsUtil.STORAGE,
            object : PermissionsUtil.PermissionListener {
                override fun onPermissionResult(isGranted: Boolean) {
                    if (isGranted) {
                        selectedTabLibrary()
                    }
                }

            })
    }

    private fun selectedTabLibrary() {
        when (binding.tabLayout.selectedTabPosition) {
            0 -> {
                ImagePickerUtil.pickFromGalleryWithCrop(
                    this@GroupChatRoomActivity,
                    { imageFile, tag ->
                        filePath = imageFile
                        hideView(binding.llAttachments)
                        viewModel.uploadMediaAPI(token, userId, "photo", filePath)

                    },
                    "photo"
                )
            }
            1 -> {
                ImagePickerUtil.pickVideo(
                    this@GroupChatRoomActivity,
                    { imageFile, tag ->
                        filePath = imageFile
                        viewModel.uploadMediaAPI(token, userId, "video", filePath)
                        hideView(binding.llAttachments)
                    },
                    "video"
                )
            }
            2 -> {
                ImagePickerUtil.pickFile(
                    this@GroupChatRoomActivity,
                    { imageFile, tag ->
                        filePath = imageFile
                        viewModel.uploadMediaAPI(token, userId, "doc", filePath)
                        hideView(binding.llAttachments)
                    },
                    "doc"
                )

            }
            3 -> {
                ImagePickerUtil.pickAudio(
                    this@GroupChatRoomActivity,
                    { imageFile, tag ->
                        filePath = imageFile
                        viewModel.uploadMediaAPI(token, userId, "audio", filePath)
                        hideView(binding.llAttachments)
                    },
                    "audio"
                )

            }
        }
    }

    private fun selectedTabItem(position: Int) {
        when (binding.tabLayout.selectedTabPosition) {
            0 -> {
                filePath = File(viewModel.imagesLiveData[position].path)
                viewModel.uploadMediaAPI(token, userId, "photo", filePath)
            }
            1 -> {
                filePath = File(viewModel.videoLiveData[position].path)
                viewModel.uploadMediaAPI(token, userId, "video", filePath)
            }
            2 -> {
                filePath = File(viewModel.docLiveData[position].path)
                viewModel.uploadMediaAPI(token, userId, "doc", filePath)
            }
            3 -> {
                filePath = File(viewModel.audioLiveData[position].path)
                viewModel.uploadMediaAPI(token, userId, "audio", filePath)

            }
        }
        hideView(binding.llAttachments)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImagePickerUtil.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsUtil?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startRecording() {
        binding.llMessage.visibility = View.GONE
        binding.rlSendAudio.visibility = View.VISIBLE
        //we use the MediaRecorder class to record
        mRecorder = MediaRecorder()
        mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        /**In the lines below, we create a directory VoiceRecorderSimplifiedCoding/Audios in the phone storage
         * and the audios are being stored in the Audios folder  */
        val root = Environment.getExternalStorageDirectory()
        val file = File(root.absolutePath + "/${getString(R.string.app_name)}/Audios")
        if (!file.exists()) {
            file.mkdirs()
        }
        audioMessageFile =
            root.absolutePath + "/${getString(R.string.app_name)}/Audios/" + (System.currentTimeMillis()
                .toString() + ".mp3")
        Log.d("filename", audioMessageFile!!)
        mRecorder?.setOutputFile(audioMessageFile)
        mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        try {
            mRecorder?.prepare()
            mRecorder?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        stopPlaying()

        //starting the chronometer
        binding.chronometerTimer.base = SystemClock.elapsedRealtime()
        binding.chronometerTimer.start()
    }

    private fun stopPlaying() {
        try {
            mPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mPlayer = null
        //showing the play button
        //imageViewPlay.setImageResource(R.drawable.ic_play);
        binding.chronometerTimer.stop()

    }

    private fun stopRecording(isCancelled: Boolean) {
        try {
            mRecorder!!.stop()
            mRecorder!!.release()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        mRecorder = null
        binding.chronometerTimer.stop()
        binding.chronometerTimer.base = SystemClock.elapsedRealtime()
        if (!isCancelled&& !audioMessageFile.isNullOrEmpty()) {
            //Hit API to send audio file
            val audioFile = File(audioMessageFile!!)
            viewModel.uploadMediaAPI(token, userId, "audio", audioFile)
        }
        //starting the chronometer

        binding.llMessage.visibility = View.VISIBLE
        binding.rlSendAudio.visibility = View.GONE
    }

    private fun showPopUpWindow() {
        // inflate the layout of the popup window
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.item_chat_option_menu, null);

        // create the popup window
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true; // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupView, width, height, focusable);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 5f
        }
        val tvViewGroup: TextView = popupView.findViewById(R.id.tvViewGroup) as TextView
        val tvSharedMedia: TextView = popupView.findViewById(R.id.tvSharedMedia) as TextView
        val tvMuteNotification: TextView =
            popupView.findViewById(R.id.tvMuteNotification) as TextView
        val tvClearChat: TextView = popupView.findViewById(R.id.tvClearChat) as TextView
        val tvSearch: TextView = popupView.findViewById(R.id.tvSearch) as TextView
        val tvReport: TextView = popupView.findViewById(R.id.tvReport) as TextView
        val tvBlock: TextView = popupView.findViewById(R.id.tvBlock) as TextView
        tvBlock.setText(R.string.exit_group)
        tvMuteNotification.setOnClickListener {
            MuteNotificationDialog(
                this,
                R.drawable.bg_chat_delete_yellow,
                object : OnConnectionTypeSelected {
                    override fun onSelectedConnection(type: String) {
                        viewModel.muteChatFromList(chatID, type)
                    }

                }).show()
            popupWindow.dismiss()
        }
        tvClearChat.setOnClickListener {
            ClearChatDialog(
                this,
                R.drawable.bg_chat_delete_yellow,
                object : OnConnectionTypeSelected {
                    override fun onSelectedConnection(type: String) {
                        viewModel.clearChat(type)
                    }

                }).show()
            popupWindow.dismiss()
        }
        tvBlock.setOnClickListener {
            BlockChatDialog(
                this,
                R.drawable.bg_chat_delete_yellow,
                object : OnConnectionTypeSelected {
                    override fun onSelectedConnection(type: String) {
                        viewModel.exitGroupChat(type)
                    }

                }).show()
            popupWindow.dismiss()
        }
        tvReport.setOnClickListener {
            ReportChatDialog(
                this,
                R.drawable.bg_chat_delete_yellow,
                object : OnConnectionTypeSelected {
                    override fun onSelectedConnection(type: String) {
                        viewModel.reportChat(type)
                    }

                }).show()
            popupWindow.dismiss()
        }
        tvViewGroup.setOnClickListener {
            val intent = Intent(this, ChatProfileActivity::class.java)
            intent.putExtra(IntentConstant.CHAT_ID, chatID)
            intent.putExtra(IntentConstant.CHAT_NAME, name)
            intent.putExtra(IntentConstant.CHAT_IMG, chatImg)
            intent.putExtra(IntentConstant.CHAT_TYPE, 3)
            intent.putExtra(IntentConstant.FROM_CHAT, true)
            startActivity(intent)
            popupWindow.dismiss()
        }
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAsDropDown(
            binding.imgChatOptionMenu,
            0,
            0,
            (Gravity.START or Gravity.BOTTOM)
        )

    }
}