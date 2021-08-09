package com.app.spark.activity.camera.utill_camera

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent


class SingleTapConfirm : SimpleOnGestureListener() {
    override fun onSingleTapUp(event: MotionEvent): Boolean {
        return true
    }
}