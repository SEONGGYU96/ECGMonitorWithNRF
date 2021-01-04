package com.seoultech.ecgmonitor

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class ECGMonitorApplication : Application() {

    companion object {
        const val CHANNEL_ID = "ecg_monitor"
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "ECGMonitor",
                NotificationManager.IMPORTANCE_DEFAULT).apply {
                    setSound(null, null)
                    enableVibration(false)
                    vibrationPattern = null
                    setShowBadge(false)
                    description = "TEST"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(serviceChannel)
        }
    }
}