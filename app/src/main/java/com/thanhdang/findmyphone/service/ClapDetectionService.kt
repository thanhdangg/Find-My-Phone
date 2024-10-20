package com.thanhdang.findmyphone.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.thanhdang.findmyphone.R
import com.thanhdang.findmyphone.helper.notification.NotificationHelper
import com.thanhdang.findmyphone.utils.CONSTANT
import com.thanhdang.findmyphone.utils.ClapDetector

class ClapDetectionService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        ClapDetector.startListening(this) {
            // Handle double clap detected
            NotificationHelper.sendNotification(this)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ClapDetector.stopListening()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startForegroundService() {
        val channelId = CONSTANT.CHANNEL_ID
        val channelName = CONSTANT.CHANNEL_NAME
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.clap_detection))
            .setContentText("Listening for claps...")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        startForeground(1, notification)
    }
}