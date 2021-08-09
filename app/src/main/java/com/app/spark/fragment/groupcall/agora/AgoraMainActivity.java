package com.app.spark.fragment.groupcall.agora;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.app.spark.R;
import com.app.spark.fragment.groupcall.AgoraBaseActivity;
import com.app.spark.fragment.groupcall.model.ConstantApp;

public class AgoraMainActivity extends AgoraBaseActivity implements View.OnClickListener {

    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agora_main);
    }
    String channelName;
    @Override
    protected void initUIandEvent() {
        getData();
       // findViewById(R.id.ivApplogo).setOnClickListener(this);
    }

    private void setHandler() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!channelName.isEmpty() && channelName!=null) {
                    vSettings().mChannelName = channelName;
                    Intent i = new Intent(AgoraMainActivity.this, AgoraCallingActivity.class);
                    i.putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, channelName);
                    // i.putExtra(AppConstants.BundleConstants.ROOM_ID,channelName);
                    startActivity(i);
                    finish();
                }
            }
        }, 1500);
    }

    private void getData() {
        Intent i = getIntent();
        channelName = i.getStringExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME);
        if(channelName==null){
            finish();
        }else{
            setHandler();
        }
    }

    @Override
    protected void deInitUIandEvent() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivApplogo:
                if(!channelName.isEmpty() && channelName!=null) {
                    vSettings().mChannelName = channelName;
                    Intent i = new Intent(AgoraMainActivity.this, AgoraCallingActivity.class);
                    i.putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, channelName);
                    // i.putExtra(AppConstants.BundleConstants.ROOM_ID,channelName);
                    startActivity(i);
                    finish();
                }else{
                    Toast.makeText(this,"Channal is null:"+channelName,Toast.LENGTH_LONG).show();
                }
            break;
        }
    }
}
