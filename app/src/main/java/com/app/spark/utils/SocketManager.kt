package com.app.spark.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.app.spark.BuildConfig
import com.app.spark.utils.ImagePickerUtil.context
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

import io.socket.engineio.client.transports.WebSocket
import okhttp3.Connection
import okhttp3.OkHttpClient
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


/**
 * Created by vikash.
 */

class SocketManager constructor(context: Context) {

    var socketId = ""

    val isConnected: Boolean get() = socket.connected()

    /*
    Method to Initialize and Add Listener on Socket
    */
    fun initialize(socketListener: SocketListener) {
        try {
            /****************SSL***/
            val myHostnameVerifier = HostnameVerifier { hostname, session -> true }
            val mySSLContext = SSLContext.getInstance("TLS")
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }


                override fun checkClientTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }
            })

            mySSLContext.init(null, trustAllCerts, java.security.SecureRandom())
            val okHttpClient = OkHttpClient.Builder()
                .callTimeout(5, TimeUnit.SECONDS)
                .hostnameVerifier(myHostnameVerifier)
                .sslSocketFactory(mySSLContext.socketFactory, object : X509TrustManager {

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }


                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {

                    }

                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {

                    }
                })
                .build()


            val options = IO.Options()
            options.webSocketFactory = okHttpClient
            //options.secure = true;
            options.transports = arrayOf(WebSocket.NAME)
            /*End of ssl**********************************************************/
            socket = IO.socket(socketUrl, options)
            socket.on(Socket.EVENT_CONNECT) {
                Handler(Looper.getMainLooper()).post {
                    socketListener.onConnected()
                    if (socket.connected()) {
                        socketId = socket.id()
                    }
                }
            }.on(Socket.EVENT_DISCONNECT) { args ->
                Handler(Looper.getMainLooper()).post {
                    if (args != null && args.isNotEmpty()) {
                        Log.e("Socket", "initialize: $args")
                    }
                }
            }
        } catch (ex: ConnectException) {
            //ex.printStackTrace()
        } catch (ex: Exception) {
            // ex.printStackTrace()
        } catch (ex: SocketTimeoutException) {

        }
    }

    /*
    Method to connect Socket
     */
    fun connect() {
        if (!socket.connected()){
            socket.connect()
        }else{
            Toast.makeText(context,"Message not connected",Toast.LENGTH_SHORT).show()
        }

    }

    /*
    Method to send message via Socket on a Key
     */
    fun sendMsg(key: String, vararg args: Any) {
        try {
            if (socket.connected()) {
                socket.emit(key, *args)
            }
        } catch (ex: SocketTimeoutException) {

        }
    }

    /*
    Add Listener to Socket
     */
    fun addListener(key: String, socketMessageListener: Emitter.Listener) {
        socket.on(key, socketMessageListener)


    }



    /*
   Remove Listener to Socket
     */
    fun removeListener(key: String, socketMessageListener: Emitter.Listener) {
        socket.off(key, socketMessageListener)
    }


    /*
    Disconnect Socket
     */
    fun disConnect() {
        if (socket.connected())
            socket.disconnect()
    }

    /*
    Interface to Handle Connect and Disconnect events of Socket
     */
    interface SocketListener {
        fun onConnected()
        fun onDisConnected()

    }

    /*
   Interface to Handle Message event of Socket
    */
    interface SocketMessageListener {
        fun onMessage(vararg args: Any)
    }

    companion object {
        lateinit var socket: Socket
        var socketManager: SocketManager? = null

        internal var socketUrl = BuildConfig.SOCKET_URL

        /**
         * Method to get the instance of socket class
         *
         * @param context
         * @return
         */
        fun getInstance(context: Context): SocketManager {
            if (socketManager == null) {
                socketManager = SocketManager(context)
            }
            return socketManager as SocketManager
        }
    }

}