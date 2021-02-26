package com.seoultech.ecgmonitor

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ECGMonitorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}