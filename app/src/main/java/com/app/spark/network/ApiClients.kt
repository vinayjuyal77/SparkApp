package com.app.spark.network

import android.content.Context
import com.app.spark.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClients {
    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun getHttpClient(appContext: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(3000, TimeUnit.SECONDS)
            .readTimeout(2000, TimeUnit.SECONDS)
            .addInterceptor(NetworkConnectionInterceptor(appContext))
            .addInterceptor(interceptor)

            .build()
    }

    private val builder = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)


    fun callRetrofit(appContext: Context): ApiInterface {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = builder
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(getHttpClient(appContext))
            .build()
        return retrofit.create(ApiInterface::class.java)
    }

}
