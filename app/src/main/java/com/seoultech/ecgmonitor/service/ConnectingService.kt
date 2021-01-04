package com.seoultech.ecgmonitor.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.seoultech.ecgmonitor.ECGMonitorApplication
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.monitor.MonitorActivity

class ConnectingService : Service() {

    companion object {
        private const val TAG = "ConnectingService"

    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val device = intent?.getParcelableExtra<BluetoothDevice>(MonitorActivity.EXTRA_DEVICE)
        if (device == null) {
            Log.e(TAG, "device is null")
            stopSelf()
        }
        val pendingIntent: PendingIntent = Intent(this, MonitorActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("device", device)
        }.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification: Notification = NotificationCompat.Builder(this, ECGMonitorApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_icon)
            .setShowWhen(false)
            .setContentText("Connecting device...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        Log.d(TAG, "onStartCommand() : startForeground")

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}