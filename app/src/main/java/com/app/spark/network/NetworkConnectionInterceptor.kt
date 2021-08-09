package com.app.spark.network

import android.content.Context
import android.net.ConnectivityManager
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.jvm.Throws

class NetworkConnectionInterceptor(private val mContext: Context) :
    Interceptor {
    @Throws (IOException::class, NoConnectivityException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isConnected) {
            throw NoConnectivityException()
            // Throwing our custom exception 'NoConnectivityException'
        }
            try {
                val builder = chain.request()//.newBuilder()
                return chain.proceed(builder)
            }
            catch (e: Exception){
                throw NoConnectivityException()
            }

    }


    @Suppress("DEPRECATION")
    private val isConnected: Boolean
        get() {
            val connectivityManager =
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = connectivityManager.activeNetworkInfo
            return netInfo != null && netInfo.isConnected
        }

}