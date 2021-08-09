package com.app.spark.utils

import android.app.Application
import io.alterac.blurkit.BlurKit

class SparkkApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        BlurKit.init(this)
    }
}