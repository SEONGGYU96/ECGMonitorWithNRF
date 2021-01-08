package com.seoultech.ecgmonitor.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.seoultech.ecgmonitor.R

/**
 * ForegroundService에 사용되는 Notification 생성 클래스
 */
class ECGNotification(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "ecg_monitor"
        private const val TAG = "ECGNotification"
    }

    //정상 연결 상태
    private var connectedNotification: Notification? = null

    //연결 끊김 상태
    private var disconnectedNotification: Notification? = null

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    //정상 연결 상태 Notification 생성/반환
    fun getConnectingNotification(pendingIntent: PendingIntent): Notification {
        if (connectedNotification == null) {
            connectedNotification = generateNotification(true, pendingIntent)
        }
        return connectedNotification!!
    }

    //연결 끊김 상태 Notification 생성/반환
    fun getDisconnectedNotification(pendingIntent: PendingIntent): Notification {
        if (disconnectedNotification == null) {
            disconnectedNotification = generateNotification(false, pendingIntent)
        }
        return disconnectedNotification!!
    }

    //Notification 생성
    private fun generateNotification(isConnected: Boolean, pendingIntent: PendingIntent) : Notification {
        initNotificationChannel()

        //연결 상태에 따른 문구 설정
        val text = if (isConnected) {
            context.getString(R.string.notification_connected)
        } else {
            context.getString(R.string.notification_disconnected)
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_icon_transparent)
            .setShowWhen(false)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        //문구 스타일과 색 변경
        if (!isConnected) {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
        } else {
            notificationBuilder.color = ContextCompat.getColor(context, R.color.red)
        }

        return notificationBuilder.build()
    }

    //API Level 26 이상은 Notification Channel을 할당해주어야함
    private fun initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT).apply {
                setSound(null, null)
                enableVibration(false)
                vibrationPattern = null
                setShowBadge(false)
                description = context.getString(R.string.notification_description)
            }
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }
}