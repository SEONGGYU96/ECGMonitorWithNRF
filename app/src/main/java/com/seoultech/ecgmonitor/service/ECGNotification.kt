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
class ECGNotification(private val context: Context) : NotificationGenerator {

    companion object {
        private const val CHANNEL_ID = "ecg_monitor"
        private const val TAG = "ECGNotification"
    }

    //정상 연결 상태
    private var connectedNotification: Notification? = null

    //연결 끊김 상태
    private var disconnectedNotification: Notification? = null

    private var bluetoothDisconnectedNotification: Notification? = null

    private var connectionFailureNotification: Notification? = null

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    //정상 연결 상태 Notification 생성/반환
    override fun getConnectingNotification(pendingIntent: PendingIntent): Notification {
        if (connectedNotification == null) {
            connectedNotification = generateNotification(
                context.getString(R.string.notification_connected),
                true, pendingIntent
            )
        }
        return connectedNotification!!
    }

    //연결 끊김 상태 Notification 생성/반환
    override fun getDisconnectedNotification(pendingIntent: PendingIntent): Notification {
        if (disconnectedNotification == null) {
            disconnectedNotification = generateNotification(context.getString(R.string.notification_disconnected),
                false, pendingIntent
            )
        }
        return disconnectedNotification!!
    }

    override fun getBluetoothDisabledNotification(pendingIntent: PendingIntent): Notification {
        if (bluetoothDisconnectedNotification == null) {
            bluetoothDisconnectedNotification = generateNotification(
                context.getString(R.string.notification_bluetooth_disabled),
                false, pendingIntent
            )
        }
        return bluetoothDisconnectedNotification!!
    }

    override fun getFailureNotification(pendingIntent: PendingIntent): Notification {
        if (connectionFailureNotification == null) {
            connectionFailureNotification = generateNotification(
                context.getString(R.string.notification_failure),
                false,
                pendingIntent
            )
        }
        return connectionFailureNotification!!
    }

    //Notification 생성
    private fun generateNotification(text: String, isColorful: Boolean, pendingIntent: PendingIntent) : Notification {
        initNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_icon_transparent)
            .setShowWhen(false)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        //문구 스타일과 색 변경
        if (!isColorful) {
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