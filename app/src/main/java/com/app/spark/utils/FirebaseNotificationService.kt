package com.app.spark.utils


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.app.spark.R
import com.app.spark.activity.main.MainActivity
import com.app.spark.activity.reciver.MyIntentService
import com.app.spark.constants.PrefConstant
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject


class FirebaseNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", message.toString())
        try {
            if (message.data.isNotEmpty()) {
                val extras = Bundle()
                for ((key, value) in message.data) {
                    extras.putString(key, value)
                }
                /*sendNotification(
                    extras.getString("title",""),
                    extras.getString("body","")
                )*/
                if(extras.getString("type","").equals("Match Found")){
                    var json  = JSONObject(extras.getString("noti_data",""))
                    var to_user=json.getString("to_user")
                    var from_user=json.getString("from_user")
                    sendNotification(
                        extras.getString("title","")+"->To user:- "+to_user,
                        extras.getString("body","")+"->From user:- "+from_user
                    )
                    MyIntentService.startAction(this,extras)
                }else {
                    sendNotification(
                        extras.getString("title",""),
                        extras.getString("body","")
                    )
                }
                Log.d("notify:-", ""+extras.toString())
            } else {
                sendNotification(
                    message.notification?.title ?: "",
                    message.notification?.body ?: ""
                )
            }
        } catch (t: Throwable) {
        }
    }
    /*  *
      * Called if InstanceID token is updated. This may occur if the security of
      * the previous token had been compromised. Note that this is called when the InstanceID token
      * is initially generated so this is where you would retrieve the token.
  */

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        if (token.isNotEmpty())
            SharedPrefrencesManager.getInstance(this).setString(PrefConstant.DEVICE_TOKEN, token)


    }

    /* *
     * Create and show a simple notification containing the received FCM message.
     *
 */

    private fun sendNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0
            /*Request code*/, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val channelId = getString(R.string.default_notification_channel_id)
        val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder =
            NotificationCompat.Builder(this, channelId)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_icon_trans)
                .setSound(defaultSoundUri)
                .setFullScreenIntent(pendingIntent, true)


        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(
            0, notificationBuilder.build()
        )
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
