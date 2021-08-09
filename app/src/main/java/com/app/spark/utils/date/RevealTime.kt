package com.app.spark.utils.date

import android.util.Log
import kotlin.math.roundToInt


fun getRevealTime(millisUntilFinished: Long): CharSequence? {
        var minutes=(millisUntilFinished / 1000)/60
        var second=(millisUntilFinished / 1000) % 60
        return String.format("%02d:%02d",minutes,second)
}

fun getOneMintTime(millisUntilFinished: Long): Int {
        var second=((61000-millisUntilFinished) / 1000) % 60
        var value=(second / 60.0) * 100
        return value.roundToInt()
}