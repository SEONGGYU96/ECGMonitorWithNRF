package com.seoultech.ecgmonitor.service

import android.app.Notification
import android.app.PendingIntent

interface NotificationGenerator {
    fun getConnectingNotification(pendingIntent: PendingIntent): Notification

    fun getDisconnectedNotification(pendingIntent: PendingIntent): Notification

    fun getBluetoothDisabledNotification(pendingIntent: PendingIntent): Notification

    fun getFailureNotification(pendingIntent: PendingIntent): Notification
}