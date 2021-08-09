package com.app.spark.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by user on 12/16/2015.
 */
class SharedPrefrencesManager {

    fun setString(key: String, value: String?) {
        val editor = sharedPreferences!!.edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun getString(key: String, dval: String): String? {
        return sharedPreferences!!.getString(key, dval)
    }

    fun setBoolean(key: String, value: Boolean) {
        val editor = sharedPreferences!!.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

    fun getBoolean(key: String, dval: Boolean): Boolean {
        return sharedPreferences!!.getBoolean(key, dval)
    }

    fun saveInt(key: String, value: Int) {

        val editor = sharedPreferences!!.edit()
        editor.putInt(key, value)
        editor.commit()

    }

    fun getInt(key: String, defVal: Int): Int {

        return sharedPreferences!!.getInt(key, defVal)
    }

    fun removeData(key: String) {
        val editor = sharedPreferences!!.edit()
        editor.remove(key)
        editor.apply()
        editor.commit()

    }

    fun clear() {
        sharedPreferences?.edit()?.clear()?.apply()
    }

    companion object {
       private var instance: SharedPrefrencesManager? = null

        private var sharedPreferences: SharedPreferences? = null

        fun getInstance(context: Context): SharedPrefrencesManager {

            if (instance == null) {
                instance = SharedPrefrencesManager()
                sharedPreferences =
                    context.getSharedPreferences("cancan", Context.MODE_PRIVATE)
            }
            return instance as SharedPrefrencesManager

        }
    }




}



