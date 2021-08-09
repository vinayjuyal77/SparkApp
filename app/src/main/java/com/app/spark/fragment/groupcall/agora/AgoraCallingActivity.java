package com.app.spark.fragment.groupcall.agora;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.spark.R;
import com.app.spark.activity.chatroom.ChatRoomActivity;
import com.app.spark.constants.AppConstants;
import com.app.spark.constants.IntentConstant;
import com.app.spark.constants.PrefConstant;
import com.app.spark.dialogs.AppReportDiloag;
import com.app.spark.dialogs.SpeakersDiloag;
import com.app.spark.fragment.groupcall.AddUserActivity;
import com.app.spark.fragment.groupcall.AgoraBaseActivity;
import com.app.spark.fragment.groupcall.RaiseHandListBottomFragment;
import com.app.spark.fragment.groupcall.adapter.ListenersListAdapter;
import com.app.spark.fragment.groupcall.adapter.SpeakerListAdapter;
import com.app.spark.fragment.groupcall.callback.RaiseHandCallbackListner;
import com.app.spark.fragment.groupcall.callback.RoomClickListner;
import com.app.spark.fragment.groupcall.model.AGEventHandler;
import com.app.spark.fragment.groupcall.model.ConstantApp;
import com.app.spark.models.RoomData;
import com.app.spark.models.USER_INFO;
import com.app.spark.utils.SharedPrefrencesManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

import static com.app.spark.activity.reciver.AgoraReciverServiceKt.ACCEPTED_REQUEST;
import static com.app.spark.activity.reciver.AgoraReciverServiceKt.ACTION_ROOM;
import static com.app.spark.activity.reciver.AgoraReciverServiceKt.ADD_REQUEST;
import static com.app.spark.activity.reciver.AgoraReciverServiceKt.ADD_TO_ROOM;
import static com.app.spark.activity.reciver.AgoraReciverServiceKt.ADD_USER;
import static com.app.spark.activity.reciver.AgoraReciverServiceKt.END_ROOM;
import static com.app.spark.activity.reciver.AgoraReciverServiceKt.EXTRA_ROOM_TYPE;
import static com.app.spark.activity.reciver.AgoraReciverServiceKt.RAISE_HAND;
import static com.app.spark.activity.reciver.AgoraReciverServiceKt.RAISE_HOST_TYPE;
import static com.app.spark.activity.reciver.AgoraReciverServiceKt.REMOVE_FROM_ROOM;
import static com.app.spark.activity.reciver.AgoraReciverServiceKt.REMOVE_USER;
import static com.app.spark.activity.reciver.AgoraReciverServiceKt.REQUEST_ACCEPTED;
import static com.app.spark.constants.AppConstants.BundleConstants.ADD_USER_TO_ROOM;
import static com.app.spark.constants.AppConstants.Initilize.MAKE_HOST_LISTENER;
import static com.app.spark.constants.AppConstants.Initilize.MAKE_HOST_SPEAKER;
import static com.app.spark.constants.AppConstants.Initilize.MAKE_LISTENERS_TYPE;
import static com.app.spark.constants.AppConstants.Initilize.MAKE_OTHER_USER_LISTENER;
import static com.app.spark.constants.AppConstants.Initilize.MAKE_OTHER_USER_SPEAKER;
import static com.app.spark.constants.AppConstants.Initilize.MAKE_SPEAKERS_TYPE;
import static com.app.spark.constants.AppConstants.Initilize.MESSAGE_TYPE;
import static com.app.spark.constants.AppConstants.Initilize.REMOVE_TYPE;
import static com.app.spark.constants.AppConstants.Initilize.REPORT_TYPE;
import static com.app.spark.utils.KtUtilKt.showSnackBar;

public class AgoraCallingActivity extends AgoraBaseActivity implements AGEventHandler,View.OnClickListener, RoomClickListner,
        RaiseHandCallbackListner {
    private final static Logger log = LoggerFactory.getLogger(AgoraCallingActivity.class);
    private volatile boolean mAudioMuted = false;
    private volatile int mAudioRouting = -1;  // Default

    ImageButton ibMuteUnmute,ibVolumOnOff,ibHandList,ibRiseHand;
    private Handler mMainHandler;
    RecyclerView rvSpeakers,rvListeners;
    SwipeRefreshLayout swapRefresh;
    SpeakerListAdapter seakerListAdapter;
    ListenersListAdapter listenersListAdapter;
    ArrayList<USER_INFO> listItemSpeaker = new ArrayList();
    ArrayList<USER_INFO> listItemListeners = new ArrayList();
    RoomListViewModel viewModel;
    SharedPrefrencesManager pref;
    String userId,token;
    TextView tvRoomName,tvDescription,tvTotalUser,tvActiveUser,tvTimeSet,tvCountText;
    ImageView ivAddUser;
    ConstraintLayout constaaintList;




    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agora_calling);
        viewModel = ViewModelProviders.of(this).get(RoomListViewModel.class);
        //viewModel= ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(this.getApplication());
        pref = SharedPrefrencesManager.Companion.getInstance(this);
        userId = pref.getString(PrefConstant.USER_ID, "");
        token = pref.getString(PrefConstant.ACCESS_TOKEN, "");
        setCalling();
        rvSpeakers=findViewById(R.id.rvSpeakers);
        rvListeners=findViewById(R.id.rvListeners);
        setAdapter();

        //viewModel.setInitialData(userId,modelData.getRoom_id());
        LocalBroadcastManager.getInstance(this).registerReceiver(new MyBroadcastReceiver(), new IntentFilter(ACTION_ROOM));
    }
    Boolean isLoding=true;

    @Override
    public void onRaiseHandCallbackClick() {
        if (channelName!=null) getListAPI(channelName);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TAG", "onReceive:call "+intent.getStringExtra(EXTRA_ROOM_TYPE));
            switch (intent.getStringExtra(EXTRA_ROOM_TYPE)){
                case ADD_REQUEST:
                    showMyDialog();
                    break;
                case ACCEPTED_REQUEST:
                    if(isLoding) {
                        isLoding=false;
                        if(channelName!=null) getListAPI(channelName);
                    }
                    break;
                case ADD_TO_ROOM:
                    if(isLoding) {
                        isLoding=false;
                        if(channelName!=null) getListAPI(channelName);
                    }
                    break;
                case REMOVE_FROM_ROOM:
                    if(isLoding) {
                        isLoding=false;
                        if(channelName!=null) getListAPI(channelName);
                    }
                    break;
            }
        }
    }

    private void showMyDialog() {
        if(AppConstants.BundleConstants.INSTANCE.isVisible() && AppConstants.BundleConstants.INSTANCE.isDialogVisible()) {
            AppConstants.BundleConstants.INSTANCE.setDialogVisible(false);
            showCustomDialog();
        }
    }
    String channelName;
    private void setCalling() {
        tvDescription=findViewById(R.id.tvDescription);
        ibMuteUnmute=findViewById(R.id.ibMuteUnmute);
        ibVolumOnOff=findViewById(R.id.ibVolumOnOff);
        ibHandList=findViewById(R.id.ibHandList);
        tvRoomName=findViewById(R.id.tvRoomName);
        ibRiseHand=findViewById(R.id.ibRiseHand);
        tvTotalUser=findViewById(R.id.tvTotalUser);
        tvActiveUser=findViewById(R.id.tvActiveUser);
        tvTimeSet=findViewById(R.id.tvTimeSet);
        swapRefresh=findViewById(R.id.swapRefresh);
        ivAddUser=findViewById(R.id.ivAddUser);
        constaaintList=findViewById(R.id.constaaintList);
        tvCountText=findViewById(R.id.tvCountText);

        findViewById(R.id.ibVolumOnOff).setOnClickListener(this);
        findViewById(R.id.ibVolumOnOff).setOnClickListener(this);
        findViewById(R.id.llLeaveCall).setOnClickListener(this);
        findViewById(R.id.ibMuteUnmute).setOnClickListener(this);
        ibHandList.setOnClickListener(this);
        ibRiseHand.setOnClickListener(this);
        ivAddUser.setOnClickListener(this);

        event().addEventHandler(this);
        Intent i = getIntent();
        channelName = i.getStringExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME);
        if (channelName!=null) getListAPI(channelName);
        worker().joinChannel(channelName, config().mUid);


        swapRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swapRefresh.setRefreshing(true);
                if(isLoding) {
                    isLoding=false;
                    if(channelName!=null) getListAPI(channelName);
                }
            }
        });
        observer();
    }

    private void observer() {
        viewModel.getSpeaker().observe(this, users -> {
            /*listItemSpeaker.clear();
            listItemSpeaker.addAll(users);
            seakerListAdapter.notifyDataSetChanged();*/
            /*for (int i=0;i<users.size();i++){
                seakerListAdapter.updateList(users.get(i));
            }*/
            try {
                listItemSpeaker.clear();
                seakerListAdapter.updateList(users);
                listenersListAdapter.notifyDataSetChanged();
                isLoding = true;
                swapRefresh.setRefreshing(false);
            }catch (Exception e){e.printStackTrace();}
        });

        viewModel.getListener().observe(this, users -> {
            try{
                if (!listItemListeners.isEmpty()) {
                    listItemListeners.clear();
                }
                /*listItemListeners.addAll(users);
                listenersListAdapter.notifyDataSetChanged();*/
                listenersListAdapter.updateList(users);
                seakerListAdapter.notifyDataSetChanged();
                isLoding=true;
                swapRefresh.setRefreshing(false);
            }catch (Exception e){e.printStackTrace();}
        });
        viewModel.getModel().observe(this, model -> {
            try{
                modelData=model;
                updateUI();
                isLoding=true;
                swapRefresh.setRefreshing(false);
            }catch (Exception e){e.printStackTrace();}
        });

        viewModel.getMessageReceived().observe(this, it->{
            switch (it.getMsg_type()){
                case ADD_USER:
                    if(isLoding) {
                        isLoding=false;
                        if(channelName!=null) getListAPI(channelName);
                    }
                    break;
                case RAISE_HAND:
                    try {
                        if (it.getMsg().equals(RAISE_HOST_TYPE)) {
                            showMyDialog();
                        } else {
                            if (isLoding) {
                                isLoding = false;
                                if (channelName != null) getListAPI(channelName);
                            }
                        }
                    }catch (Exception e){e.printStackTrace();}
                    break;
                case REMOVE_USER:
                    try {
                        if (Integer.parseInt(it.getMsg()) == Integer.parseInt(userId)) {
                            /*onRoomEndDialog(getString(R.string.your_are_remove_by_the_host));
                            mAudioMuted = false;
                            onVoiceMuteClicked();*/
                            Toast.makeText(getApplicationContext(), getString(R.string.your_are_remove_by_the_host), Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            if (isLoding) {
                                isLoding = false;
                                if (channelName != null) getListAPI(channelName);
                            }
                        }
                    }catch (Exception e){e.printStackTrace();}
                    break;
                case END_ROOM:
                    try {
                        onRoomEndDialog(getString(R.string.this_group_is_deleted));
                        mAudioMuted = false;
                        onVoiceMuteClicked();
                    }catch (Exception e){e.printStackTrace();}
                    break;
                case REQUEST_ACCEPTED:
                    if(isLoding) {
                        isLoding=false;
                        if(channelName!=null) getListAPI(channelName);
                    }
                    break;
            }
        });
        viewModel.getSucessString().observe(this, it -> {
            switch (it){
                case REMOVE_USER:
                    if(isLoding) {
                        isLoding=false;
                        if(channelName!=null) getListAPI(channelName);
                    }
                    break;
            }
        });
        viewModel.getErrString().observe(this, err -> {
            try {
                if (!err.isEmpty()) showSnackBar(tvDescription, err);
            }catch (Exception e){e.printStackTrace();}
        });

        viewModel.getSuccessResult().observe(this, err -> {
            try {
                if (err==200) {
                     if(isLoding) {
                        isLoding=false;
                        if(channelName!=null) getListAPI(channelName);
                    }
                }
            }catch (Exception e){e.printStackTrace();}
        });
        viewModel.getCountText().observe(this, count -> {
            try {
               if(count==0){
                   tvCountText.setVisibility(View.GONE);
               }else{
                   tvCountText.setVisibility(View.VISIBLE);
                   tvCountText.setText(String.valueOf(count));
               }
            }catch (Exception e){e.printStackTrace();}
        });
    }

    private void updateUI() {
        try {
            viewModel.setInitialData(userId, modelData.getRoom_id());
            if (modelData.getRoom_title() != null)
                tvRoomName.setText(getString(R.string.welcome_to) + modelData.getRoom_title());
            if (modelData.getRoom_description() != null)
                tvDescription.setText(modelData.getRoom_description());
            tvTotalUser.setText(String.valueOf(modelData.getUser_count()));
            tvActiveUser.setText(String.valueOf(modelData.is_active()));
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Instant instant = Instant.parse(modelData.getCreated_date());
                Date dd = Date.from(instant);
                //Log.d("Date is:- ", "updateUI: "+dd);
                SimpleDateFormat output = new SimpleDateFormat("yyyy, MMM-dd hh:mm aaa");
                String formatted = output.format(dd);
                // Log.i("DATE", "" + formatted);
                tvTimeSet.setText("Started at " + formatted);
            }
            seakerListAdapter.setHostId(modelData.getCreated_by());
            listenersListAdapter.setHostId(modelData.getCreated_by());
            if (modelData.getCreated_by() == Integer.parseInt(userId)) {
                ibHandList.setVisibility(View.VISIBLE);
                constaaintList.setVisibility(View.VISIBLE);
                ibRiseHand.setVisibility(View.GONE);
                ibMuteUnmute.setVisibility(View.VISIBLE);
                ivAddUser.setVisibility(View.VISIBLE);
            } else if (viewModel.isSpeakerList().contains(Integer.parseInt(userId))) {
                ibRiseHand.setVisibility(View.GONE);
                ibMuteUnmute.setVisibility(View.VISIBLE);
                ivAddUser.setVisibility(View.GONE);
            } else {
                ibHandList.setVisibility(View.GONE);
                constaaintList.setVisibility(View.GONE);
                ibRiseHand.setVisibility(View.VISIBLE);
                ibMuteUnmute.setVisibility(View.GONE);
                ivAddUser.setVisibility(View.GONE);
                mAudioMuted = false;
                onVoiceMuteClicked();
            }
        }catch (Exception e){e.printStackTrace();}
    }

    RoomData modelData;
    private void getListAPI(String roomId) {
        try{
            viewModel.getListAPI(roomId);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    protected void initUIandEvent() {
    }

    private void setAdapter() {
        seakerListAdapter=new SpeakerListAdapter(this,listItemSpeaker,this);
        listenersListAdapter=new ListenersListAdapter(this,listItemListeners,this);

        GridLayoutManager layoutManager1=new GridLayoutManager(this,4);
        rvSpeakers.setLayoutManager(layoutManager1);
        rvSpeakers.setAdapter(seakerListAdapter);


        GridLayoutManager layoutManager2=new GridLayoutManager(this,4);
        rvListeners.setLayoutManager(layoutManager2);
        rvListeners.setAdapter(listenersListAdapter);
    }

    private static final int UPDATE_UI_MESSAGE = 0x1024;

    StringBuffer mMessageCache = new StringBuffer();

    private void notifyMessageChanged(String msg) {
        if (mMessageCache.length() > 10000) { // drop messages
            mMessageCache = new StringBuffer(mMessageCache.substring(10000 - 40));
        }

        mMessageCache.append(System.currentTimeMillis()).append(": ").append(msg).append("\n"); // append timestamp for messages

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                if (mMainHandler == null) {
                    mMainHandler = new Handler(getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);

                            if (isFinishing()) {
                                return;
                            }

                            switch (msg.what) {
                                case UPDATE_UI_MESSAGE:
                                    String content = (String) (msg.obj);
                                    //tvDescription.setText(content);
                                   //mMessageList.setSelection(content.length());
                                    break;

                                default:
                                    break;
                            }

                        }
                    };
                }

                mMainHandler.removeMessages(UPDATE_UI_MESSAGE);
                Message envelop = new Message();
                envelop.what = UPDATE_UI_MESSAGE;
                envelop.obj = mMessageCache.toString();
                mMainHandler.sendMessageDelayed(envelop, 1000l);
            }
        });
    }

    private void optional() {
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }
    private void optionalDestroy() {
    }
    public void onSwitchSpeakerClicked() {
        RtcEngine rtcEngine = rtcEngine();

        /*
          Enables/Disables the audio playback route to the speakerphone.
          This method sets whether the audio is routed to the speakerphone or earpiece.
          After calling this method, the SDK returns the onAudioRouteChanged callback
          to indicate the changes.
         */
        rtcEngine.setEnableSpeakerphone(mAudioRouting != 3);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(new MyBroadcastReceiver());
    }

    @Override
    protected void deInitUIandEvent() {
        optionalDestroy();

        doLeaveChannel();
        event().removeEventHandler(this);
    }

    /**
     * Allows a user to leave a channel.
     *
     * After joining a channel, the user must call the leaveChannel method to end the call before
     * joining another channel. This method returns 0 if the user leaves the channel and releases
     * all resources related to the call. This method call is asynchronous, and the user has not
     * exited the channel when the method call returns. Once the user leaves the channel,
     * the SDK triggers the onLeaveChannel callback.
     *
     * A successful leaveChannel method call triggers the following callbacks:
     *
     * The local client: onLeaveChannel.
     * The remote client: onUserOffline, if the user leaving the channel is in the
     * Communication channel, or is a BROADCASTER in the Live Broadcast profile.
     *
     */
    private void doLeaveChannel() {
        worker().leaveChannel(config().mChannel);
    }

    public void onEndCallClicked(View view) {
        log.info("onEndCallClicked " + view);
        quitCall();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(modelData.getCreated_by()==Integer.parseInt(userId)) showBackDialog(getString(R.string.if_you_exit),1);
        else  showBackDialog(getString(R.string.if_you_sure_want_to_exit),2);
      /*  log.info("onBackPressed");
        quitCall();*/
    }

    private void showBackDialog(String message,Integer value) {
        try{
            ViewGroup viewGroup = findViewById(android.R.id.content);
            //then we will inflate the custom alert dialog xml that we created
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_back_room, viewGroup, false);
            //Now we need an AlertDialog.Builder object
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //setting the view of the builder to our custom view that we already inflated
            builder.setView(dialogView);
            TextView tvAllow=dialogView.findViewById(R.id.tvAllow);
            TextView tvCancel=dialogView.findViewById(R.id.tvCancel);
            TextView tvMsg=dialogView.findViewById(R.id.tvMsg);
            //finally creating the alert dialog and displaying it
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            tvMsg.setText(message);
            tvAllow.setOnClickListener(view -> {
                if(value==1){
                    //Toast.makeText(getApplicationContext(),"Delete Room by host.",Toast.LENGTH_LONG).show();
                    viewModel.onDeleteRoom(Integer.parseInt(userId),modelData.getRoom_id());
                }else {
                    //Toast.makeText(getApplicationContext(),"remove user from this room.",Toast.LENGTH_LONG).show();
                    viewModel.onRemoveFromRoomAPI(modelData.getRoom_id(),Integer.parseInt(userId));
                }
                alertDialog.dismiss();
                quitCall();
            });
            tvCancel.setOnClickListener(view -> {
                alertDialog.dismiss();
            });
            alertDialog.show();
            alertDialog.setCancelable(false);
        }catch (Exception e){e.printStackTrace();}
    }
    private void onRoomEndDialog(String textMsg) {
        try{
            ViewGroup viewGroup = findViewById(android.R.id.content);
            //then we will inflate the custom alert dialog xml that we created
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_end_room, viewGroup, false);
            //Now we need an AlertDialog.Builder object
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //setting the view of the builder to our custom view that we already inflated
            builder.setView(dialogView);
            TextView tvAllow=dialogView.findViewById(R.id.tvAllow);
            TextView tvMessage=dialogView.findViewById(R.id.tvMessage);
            tvMessage.setText(textMsg);
            //finally creating the alert dialog and displaying it
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            tvAllow.setOnClickListener(view -> {
                finish();
            });
            if(!alertDialog.isShowing()) alertDialog.show();
            alertDialog.setCancelable(false);
        }catch (Exception e){e.printStackTrace();}
    }
    private void showCustomDialog() {
        try{
            ViewGroup viewGroup = findViewById(android.R.id.content);
            //then we will inflate the custom alert dialog xml that we created
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rise_hand, viewGroup, false);
            //Now we need an AlertDialog.Builder object
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //setting the view of the builder to our custom view that we already inflated
            builder.setView(dialogView);
            TextView tvAllow=dialogView.findViewById(R.id.tvAllow);
            TextView tvCancel=dialogView.findViewById(R.id.tvCancel);
            //finally creating the alert dialog and displaying it
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            tvAllow.setOnClickListener(view -> {
                AppConstants.BundleConstants.INSTANCE.setVisible(false);
                AppConstants.BundleConstants.INSTANCE.setDialogVisible(true);
                alertDialog.dismiss();
                new RaiseHandListBottomFragment(modelData.getRoom_id()).show(getSupportFragmentManager(), "raise_hand_list");
            });
            tvCancel.setOnClickListener(view -> {
                AppConstants.BundleConstants.INSTANCE.setVisible(true);
                alertDialog.dismiss();
            });
            if(!alertDialog.isShowing()) alertDialog.show();
            alertDialog.setCancelable(false);
        }catch (Exception e){e.printStackTrace();}
    }

    private void quitCall() {
        finish();
    }

    public void onVoiceMuteClicked() {
        RtcEngine rtcEngine = rtcEngine();
        rtcEngine.muteLocalAudioStream(mAudioMuted = !mAudioMuted);
        if (mAudioMuted) {
            ibMuteUnmute.setImageDrawable(getDrawable(R.drawable.ic_mic_off_24));
            //ibMuteUnmute.setColorFilter(getResources().getColor(R.color.green_text_color), PorterDuff.Mode.MULTIPLY);
        } else {
            ibMuteUnmute.setImageDrawable(getDrawable(R.drawable.ic_mic_on));
            //ibMuteUnmute.clearColorFilter();
        }
    }

    @Override
    public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
        String msg = "onJoinChannelSuccess " + channel + " " + (uid & 0xFFFFFFFFL) + " " + elapsed;
        log.debug(msg);

        notifyMessageChanged(msg);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                rtcEngine().muteLocalAudioStream(mAudioMuted);
            }
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        String msg = "onUserOffline " + (uid & 0xFFFFFFFFL) + " " + reason;
        log.debug(msg);

        notifyMessageChanged(msg);

    }

    @Override
    public void onExtraCallback(final int type, final Object... data) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                doHandleExtraCallback(type, data);
            }
        });
    }

    private void doHandleExtraCallback(int type, Object... data) {
        int peerUid;
        boolean muted;

        switch (type) {
            case AGEventHandler.EVENT_TYPE_ON_USER_AUDIO_MUTED: {
                peerUid = (Integer) data[0];
                muted = (boolean) data[1];

                notifyMessageChanged("mute: " + (peerUid & 0xFFFFFFFFL) + " " + muted);
                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AUDIO_QUALITY: {
                peerUid = (Integer) data[0];
                int quality = (int) data[1];
                short delay = (short) data[2];
                short lost = (short) data[3];

                notifyMessageChanged("quality: " + (peerUid & 0xFFFFFFFFL) + " " + quality + " " + delay + " " + lost);
                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_SPEAKER_STATS: {
                IRtcEngineEventHandler.AudioVolumeInfo[] infos = (IRtcEngineEventHandler.AudioVolumeInfo[]) data[0];

                if (infos.length == 1 && infos[0].uid == 0) { // local guy, ignore it
                    break;
                }

                StringBuilder volumeCache = new StringBuilder();
                for (IRtcEngineEventHandler.AudioVolumeInfo each : infos) {
                    peerUid = each.uid;
                    int peerVolume = each.volume;

                    if (peerUid == 0) {
                        continue;
                    }

                    volumeCache.append("volume: ").append(peerUid & 0xFFFFFFFFL).append(" ").append(peerVolume).append("\n");
                }

                if (volumeCache.length() > 0) {
                    String volumeMsg = volumeCache.substring(0, volumeCache.length() - 1);
                    notifyMessageChanged(volumeMsg);

                    if ((System.currentTimeMillis() / 1000) % 10 == 0) {
                        log.debug(volumeMsg);
                    }
                }
                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_APP_ERROR: {
                int subType = (int) data[0];

                if (subType == ConstantApp.AppError.NO_NETWORK_CONNECTION) {
                    showLongToast(getString(R.string.please_check_internet));
                }
                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AGORA_MEDIA_ERROR: {
                int error = (int) data[0];
                String description = (String) data[1];

                notifyMessageChanged(error + " " + description);

                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED: {
                notifyHeadsetPlugged((int) data[0]);

                break;
            }
        }
    }

    public void notifyHeadsetPlugged(final int routing) {
        log.info("notifyHeadsetPlugged " + routing);

        mAudioRouting = routing;
        if (mAudioRouting == 3) { // Speakerphone
            ibVolumOnOff.setImageDrawable(getDrawable(R.drawable.ic_icon_volume));
            //ibVolumOnOff.setColorFilter(getResources().getColor(R.color.green_text_color), PorterDuff.Mode.MULTIPLY);
        } else {
            ibVolumOnOff.setImageDrawable(getDrawable(R.drawable.ic_volume_mute_24));
            //.clearColorFilter();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ibVolumOnOff:
                onSwitchSpeakerClicked();
                //viewModel.initialize(userId,modelData.getRoom_id(),"","","","");
                break;
            case R.id.ibMuteUnmute:
                onVoiceMuteClicked();
                break;
            case R.id.llLeaveCall:
                onBackPressed();
                break;
            case R.id.ibHandList:
                AppConstants.BundleConstants.INSTANCE.setVisible(false);
                new RaiseHandListBottomFragment(modelData.getRoom_id()).show(getSupportFragmentManager(), "raise_hand_list");
                break;
            case R.id.ibRiseHand:
                viewModel.setRaiseHandAPI(modelData.getRoom_id(),Integer.parseInt(userId));
                break;
            case R.id.ivAddUser:
                AppConstants.BundleConstants.INSTANCE.setResume(true);
                Intent intent = new Intent(this, AddUserActivity.class);
                intent.setAction(ADD_USER_TO_ROOM);
                intent.putExtra(AppConstants.BundleConstants.ROOM_ID, String.valueOf(modelData.getRoom_id()));
                startActivity(intent);
                //Toast.makeText(this, "is working", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onResume() {
        if(isLoding && AppConstants.BundleConstants.INSTANCE.isResume()) {
            isLoding=false;
            AppConstants.BundleConstants.INSTANCE.setResume(false);
            if(channelName!=null) getListAPI(channelName);
        }
        super.onResume();
    }

    @Override
    public void onSpeakerItemClick(int pos,USER_INFO userData,String type) {
        switch (type){
            case MAKE_HOST_SPEAKER:
                new SpeakersDiloag.Builder(this)
                        .setType(1)
                        .setOkFunction((selectType) -> {
                            onHandleAction(selectType, userData);
                            return null;
                        })
                        .build();
                break;
            case MAKE_OTHER_USER_SPEAKER:
                new SpeakersDiloag.Builder(this)
                        .setType(3)
                        .setOkFunction((selectType) -> {
                            onHandleAction(selectType, userData);
                            return null;
                        })
                        .build();
                break;
        }

    }

    private void onHandleAction(String selectType,USER_INFO userData) {
        switch (selectType){
            case MESSAGE_TYPE:
                Intent intent = new Intent(this, ChatRoomActivity.class);
                intent.putExtra(IntentConstant.CHAT_ID, String.valueOf(userData.getUser_id()));
                intent.putExtra(IntentConstant.CHAT_NAME, String.valueOf(userData.getName()));
                intent.putExtra(IntentConstant.CHAT_IMG, userData.getProfile_pic());
                intent.putExtra("chatType", "2");
                startActivity(intent);
                break;
            case REMOVE_TYPE:
                viewModel.onRemoveFromRoomAPI(modelData.getRoom_id(),userData.getUser_id());
                break;
            case REPORT_TYPE:
                new AppReportDiloag.Builder(this)
                        .setTitle(getString(R.string.report_user),getString(R.string.do_you_want_report_this_post))
                        .setAction(getString(R.string.Cancel),getString(R.string.report))
                        .setOkFunction(() -> {
                            viewModel.reportPost(
                                    token,
                                    userId,
                                    String.valueOf(userData.getUser_id()));
                            return null;
                        })
                        .build();
                break;
            case MAKE_LISTENERS_TYPE:
                viewModel.onSendListneresType(
                        String.valueOf(modelData.getRoom_id()),
                        "listener",
                        String.valueOf(userData.getUser_id()));
                break;
            case MAKE_SPEAKERS_TYPE:
                viewModel.onSendListneresType(
                        String.valueOf(modelData.getRoom_id()),
                        "speaker",
                        String.valueOf(userData.getUser_id()));
                break;
        }
    }

    @Override
    public void onListnerItemClick(int pos, USER_INFO userData,String type) {
        switch (type){
            case MAKE_HOST_LISTENER:
                new SpeakersDiloag.Builder(this)
                        .setType(2)
                        .setOkFunction((selectType) -> {
                            onHandleAction(selectType, userData);
                            return null;
                        })
                        .build();
                break;
            case MAKE_OTHER_USER_LISTENER:
                new SpeakersDiloag.Builder(this)
                        .setType(3)
                        .setOkFunction((selectType) -> {
                            onHandleAction(selectType, userData);
                            return null;
                        })
                        .build();
                break;
        }
    }


}
