package com.seoultech.ecgmonitor.protocol

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.seoultech.ecgmonitor.main.MainActivity
import com.seoultech.ecgmonitor.R

class AbnormalNotification(val context: Context) {

    companion object {
        private const val CHANNEL_ID = "AbnormalNotification"
        private const val NOTIFICATION_ID = 2
    }

    fun showNotification(type: AbnormalProtocol.AbnormalType, averageBPM: Int) {
        createChannel()
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, getNotification(type, averageBPM))
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.abnormal_channel_name)
            val descriptionText = context.getString(R.string.abnormal_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, 0, intent, 0)
    }

    private fun getNotification(
        type: AbnormalProtocol.AbnormalType,
        averageBPM: Int
    ): Notification {
        val contentText = getContentText(type)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_heart)
            .setContentTitle("이상 심박수 감지")
            .setContentText(String.format(contentText, averageBPM))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.red))
            .build()
    }

    private fun getContentText(type: AbnormalProtocol.AbnormalType): String {
        return when (type) {
            AbnormalProtocol.AbnormalType.Tachycardia -> {
                context.getString(R.string.abnormal_notification_tachycardia)
            }
            AbnormalProtocol.AbnormalType.Bradycardia -> {
                context.getString(R.string.abnormal_notification_bradycardia)
            }
            else -> {
                context.getString(R.string.abnormal_notification_arrhythmia)
            }
        }
    }
}