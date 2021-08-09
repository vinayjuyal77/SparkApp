package com.app.spark.utils

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.app.spark.R
import com.app.spark.activity.FixYourProfileActivity
import com.app.spark.interfaces.MainActivityInterface

/**
 * Created by techugo on 30/03/19.
 */
abstract class BaseFragment : Fragment() {
    internal var mainActivityInterface: MainActivityInterface? = null



    fun registerMainInterface(mainActivityInterface: MainActivityInterface) {
        this.mainActivityInterface = mainActivityInterface
    }

    fun removeInterface() {
        mainActivityInterface = null
    }
     open fun stopMusic(){}




}