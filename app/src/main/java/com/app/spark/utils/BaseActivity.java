package com.app.spark.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.app.spark.R;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.simform.videooperations.FFmpegQueryExtension;

import java.util.ArrayList;


/**
 * Created by techugo on 30/03/19.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private ProgressDialog dialog;
    private View layoutView;

    int height = 0;
    int width = 0;



    FFmpegQueryExtension ffmpegQueryExtension;



    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        new WebView(this).destroy();
      //  ffmpegQueryExtension = new  FFmpegQueryExtension();
        dialog = new ProgressDialog(this);
        ffmpegQueryExtension = new  FFmpegQueryExtension();


    }




    public void showView(View view){
        if(view.getVisibility()== View.GONE) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public void hideView(View view){
        view.setVisibility(View.GONE);
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if (v != null &&
                (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                !v.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                //  hideKeyboard(this);
            }
        }
        return super.dispatchTouchEvent(ev);
    }


    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }
    public static void showKeyboard(Activity activity,EditText editText) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    public void showDialog(){
       // dialog.setMessage("Matching Found . . .");
       // dialog.show();
        try {
            dialog.show();
        } catch (WindowManager.BadTokenException e) {e.printStackTrace();
        }
        dialog.setCancelable(false);
        dialog.getWindow()
                .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.progress_dialog_custom);
    }

    public void dismisDilaog(){
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }









}