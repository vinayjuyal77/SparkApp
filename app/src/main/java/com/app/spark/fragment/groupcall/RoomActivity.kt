package com.app.spark.fragment.groupcall

import android.content.Intent
import android.graphics.PorterDuff
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.app.spark.R
import com.app.spark.constants.AppConstants
import com.app.spark.databinding.ActivityRoomBinding
import com.app.spark.dialogs.RiseHandDiloag
import com.app.spark.dialogs.SpeakersDiloag
import com.app.spark.fragment.groupcall.adapter.ListenersListAdapter
import com.app.spark.fragment.groupcall.adapter.SpeakerListAdapter
import com.app.spark.fragment.groupcall.model.AGEventHandler
import com.app.spark.fragment.groupcall.model.ConstantApp
import com.app.spark.models.USER_INFO
import io.agora.rtc.IRtcEngineEventHandler.AudioVolumeInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RoomActivity : AgoraBaseActivity(),View.OnClickListener, AGEventHandler {
    private lateinit var binding: ActivityRoomBinding
    private var listItem : ArrayList<USER_INFO> = ArrayList()
    private lateinit var seakerListAdapter: SpeakerListAdapter
    private lateinit var listenersListAdapter: ListenersListAdapter
    lateinit var roomId:String
    /*
    * agora initilize*/
    private val log: Logger? = LoggerFactory.getLogger(RoomActivity::class.java)
    @Volatile
    private var mAudioMuted = false
    @Volatile
    private var mAudioRouting = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_room)
        //roomId = intent.extras!!.getString(AppConstants.BundleConstants.ROOM_ID)!!
       /* adapterInit()
        intilize()*/
    }
    private fun intilize() {
        binding.ibRiseHand.setOnClickListener(this)
        binding.ivAddUser.setOnClickListener(this)
        binding.ibHandList.setOnClickListener(this)
        binding.ibMuteUnmute.setOnClickListener(this)
        binding.llLeaveCall.setOnClickListener(this)
        binding.ibVolumOnOff.setOnClickListener(this)
    }
    private fun adapterInit() {
        /*seakerListAdapter=SpeakerListAdapter(this,listItem)
        *//*binding.rvSpeakers.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false).apply {
            scrollToPosition(0)
        }*//*
        binding.rvSpeakers.layoutManager = GridLayoutManager(this,4)
        binding.rvSpeakers.adapter = seakerListAdapter
        seakerListAdapter.onItemSpeak = {Pos->
            SpeakersDiloag.Builder(this)
                .setType(1)
                .setOkFunction {}
                .build()
        }

        listenersListAdapter=ListenersListAdapter(this,listItem)*/
       /* binding.rvListeners.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false).apply {
            scrollToPosition(0)
        }*/
     /*   binding.rvListeners.layoutManager = GridLayoutManager(this,4)
        binding.rvListeners.adapter = listenersListAdapter
        listenersListAdapter.onItemListener = {Pos->
            SpeakersDiloag.Builder(this)
                .setType(2)
                .setOkFunction {}
                .build()
        }*/
    }
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.ibRiseHand->{
                RiseHandDiloag.Builder(this)
                    .setOkFunction {}
                    .build()
            }
            R.id.ivAddUser->{
                if(!roomId.isNullOrEmpty()) {
                    startActivity(Intent(this, AddUserActivity::class.java).apply {
                        action = AppConstants.BundleConstants.ADD_USER_TO_ROOM
                        putExtra(AppConstants.BundleConstants.ROOM_ID, roomId)
                    })
                }
            }
            R.id.ibHandList->{
                //RaiseHandListBottomFragment().show(supportFragmentManager, "raise_hand_list")
            }
            R.id.ibVolumOnOff->{
                onSwitchSpeakerClicked()
            }
            R.id.ibMuteUnmute->{
                onVoiceMuteClicked()
            }
            R.id.llLeaveCall->{
                quitCall()
            }
        }
    }

    /*ag0raaaaaa
    *
    * initial view
    * */

    override fun initUIandEvent() {
        event().addEventHandler(this)
        roomId = intent.extras!!.getString(AppConstants.BundleConstants.ROOM_ID)!!
        val channelName = intent.extras!!.getString(ConstantApp.ACTION_KEY_CHANNEL_NAME)!!
        worker().joinChannel(roomId, config().mUid)
        binding.tvTitle.text = channelName
        optional()
        adapterInit()
        intilize()
    }
    private var mMainHandler: Handler? = null
    private val UPDATE_UI_MESSAGE = 0x1024
    var mMessageCache = StringBuffer()
    private fun notifyMessageChanged(msg: String) {
        if (mMessageCache.length > 10000) { // drop messages
            mMessageCache = StringBuffer(mMessageCache.substring(10000 - 40))
        }
        mMessageCache.append(System.currentTimeMillis()).append(": ").append(msg)
            .append("\n") // append timestamp for messages
        runOnUiThread(Runnable {
            if (isFinishing) {
                return@Runnable
            }
            if (mMainHandler == null) {
                mMainHandler = object : Handler(mainLooper) {
                    override fun handleMessage(msg: Message) {
                        super.handleMessage(msg)
                        if (isFinishing) {
                            return
                        }
                        when (msg.what) {
                            UPDATE_UI_MESSAGE -> {
                                val content = msg.obj as String
                                binding.tvDescription!!.setText(content)
                                //binding.tvDescription!!.setSelection(content.length)
                            }
                            else -> {
                            }
                        }
                    }
                }
            }
            mMainHandler!!.removeMessages(UPDATE_UI_MESSAGE)
            val envelop = Message()
            envelop.what = UPDATE_UI_MESSAGE
            envelop.obj = mMessageCache.toString()
            mMainHandler!!.sendMessageDelayed(envelop, 1000L)
        })
    }
    private fun optional() {
        volumeControlStream = AudioManager.STREAM_VOICE_CALL
    }
    private fun optionalDestroy() {}
    fun onSwitchSpeakerClicked() {
        log!!.info("onSwitchSpeakerClicked $this $mAudioMuted $mAudioRouting")
        val rtcEngine = rtcEngine()
        rtcEngine.setEnableSpeakerphone(mAudioRouting != 3)
    }
    override fun deInitUIandEvent() {
        optionalDestroy()
        doLeaveChannel()
        event().removeEventHandler(this)
    }
    private fun doLeaveChannel() {
        worker().leaveChannel(config().mChannel)
    }
    override fun onBackPressed() {
        super.onBackPressed()
        quitCall()
    }
    private fun quitCall() {
        finish()
    }

    fun onVoiceMuteClicked() {
        val rtcEngine = rtcEngine()
        rtcEngine.muteLocalAudioStream(!mAudioMuted.also { mAudioMuted = it })
        if (mAudioMuted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.ibMuteUnmute.setColorFilter(getColor(R.color.green_txt_color), PorterDuff.Mode.MULTIPLY)
            }
        } else {
            binding.ibMuteUnmute.clearColorFilter()
        }
    }


    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        val msg = "onJoinChannelSuccess " + channel + " " + (uid and 0xFFFFFFFFL.toInt()) + " " + elapsed
        log!!.debug(msg)
        notifyMessageChanged(msg)
        runOnUiThread(Runnable {
            if (isFinishing) {
                return@Runnable
            }
            rtcEngine().muteLocalAudioStream(mAudioMuted)
        })
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        val msg = "onUserOffline " + (uid and 0xFFFFFFFFL.toInt()) + " " + reason
        log!!.debug(msg)
        notifyMessageChanged(msg)
    }

    override fun onExtraCallback(type: Int, vararg data: Any?) {
        runOnUiThread(Runnable {
            if (isFinishing) {
                return@Runnable
            }
            doHandleExtraCallback(type,data)
        })
    }

    private fun doHandleExtraCallback(type: Int, vararg data: Any) {
        var peerUid: Int
        val muted: Boolean
        when (type) {
            AGEventHandler.EVENT_TYPE_ON_USER_AUDIO_MUTED -> {
                peerUid = data[0] as Int
                muted = data[1] as Boolean
                notifyMessageChanged("mute: " + (peerUid and 0xFFFFFFFFL.toInt()) + " " + muted)
            }
            AGEventHandler.EVENT_TYPE_ON_AUDIO_QUALITY -> {
                peerUid = data[0] as Int
                val quality = data[1] as Int
                val delay = data[2] as Short
                val lost = data[3] as Short
                notifyMessageChanged("quality: " + (peerUid and 0xFFFFFFFFL.toInt()) + " " + quality + " " + delay + " " + lost)
            }
            AGEventHandler.EVENT_TYPE_ON_SPEAKER_STATS -> {
                val infos = data[0] as Array<AudioVolumeInfo>
                // local guy, ignore it
                if (infos.size == 1 && infos[0].uid == 0) {
                    // break
                }
                val volumeCache = StringBuilder()
                for (each in infos) {
                    peerUid = each.uid
                    val peerVolume = each.volume
                    if (peerUid == 0) {
                        continue
                    }
                    volumeCache.append("volume: ").append(peerUid and 0xFFFFFFFFL.toInt()).append(" ")
                        .append(peerVolume).append("\n")
                }
                if (volumeCache.length > 0) {
                    val volumeMsg = volumeCache.substring(0, volumeCache.length - 1)
                    notifyMessageChanged(volumeMsg)
                    if (System.currentTimeMillis() / 1000 % 10 == 0L) {
                        log!!.debug(volumeMsg)
                    }
                }
            }
            AGEventHandler.EVENT_TYPE_ON_APP_ERROR -> {
                val subType = data[0] as Int
                if (subType == ConstantApp.AppError.NO_NETWORK_CONNECTION) {
                    showLongToast(getString(R.string.please_check_internet))
                }
            }
            AGEventHandler.EVENT_TYPE_ON_AGORA_MEDIA_ERROR -> {
                val error = data[0] as Int
                val description = data[1] as String
                notifyMessageChanged("$error $description")
            }
            AGEventHandler.EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED -> {
                notifyHeadsetPlugged(data[0] as Int)
            }
        }
    }

    fun notifyHeadsetPlugged(routing: Int) {
        log!!.info("notifyHeadsetPlugged $routing")
        mAudioRouting = routing
        if (mAudioRouting == 3) { // Speakerphone
            binding.ibVolumOnOff.setColorFilter(resources.getColor(R.color.green_txt_color), PorterDuff.Mode.MULTIPLY)
        } else {
            binding.ibVolumOnOff.clearColorFilter()
        }
    }
}