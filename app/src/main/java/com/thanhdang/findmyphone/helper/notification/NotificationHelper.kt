package com.thanhdang.findmyphone.helper.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.thanhdang.findmyphone.R

object NotificationHelper {
    private const val CHANNEL_ID = "clap_detection_channel"
    private const val NOTIFICATION_ID = 1

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name =  context.getString(R.string.clap_detection)
            val descriptionText = context.getString(R.string.notifications_for_clap_detection)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.clap_detected))
            .setContentText(context.getString(R.string.clap_sound_detected))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (areNotificationsEnabled()) {
                notify(NOTIFICATION_ID, builder.build())
            } else {
                // Handle the case where notifications are not enabled
            }
        }
    }
}