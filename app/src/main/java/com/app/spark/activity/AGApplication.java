package com.app.spark.activity;

import android.app.Application;

import com.app.spark.fragment.groupcall.model.CurrentUserSettings;
import com.app.spark.fragment.groupcall.model.WorkerThread;

import io.alterac.blurkit.BlurKit;

public class AGApplication extends Application {

    private WorkerThread mWorkerThread;
    public void onCreate() {
        super.onCreate();
        BlurKit.init(this);
    }
    public synchronized void initWorkerThread() {
        if (mWorkerThread == null) {
            mWorkerThread = new WorkerThread(getApplicationContext());
            mWorkerThread.start();

            mWorkerThread.waitForReady();
        }
    }

    public synchronized WorkerThread getWorkerThread() {
        return mWorkerThread;
    }

    public synchronized void deInitWorkerThread() {
        mWorkerThread.exit();
        try {
            mWorkerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mWorkerThread = null;
    }

    public static final CurrentUserSettings mAudioSettings = new CurrentUserSettings();
}
