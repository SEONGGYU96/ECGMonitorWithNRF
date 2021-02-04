package com.seoultech.ecgmonitor.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext

object PermissionUtil {
    fun isLocationPermissionsGranted(context: Context): Boolean {
        return checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun requestLocationPermission(activity: Activity, requestCode: Int) {
        requestPermission(activity, requestCode, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun isSMSPermissionsGranted(context: Context): Boolean {
        return checkPermission(context, Manifest.permission.SEND_SMS)
    }

    fun requestSMSPermission(activity: Activity, requestCode: Int) {
        requestPermission(activity, requestCode, Manifest.permission.SEND_SMS)
    }

    private fun checkPermission(context: Context, permission: String) : Boolean {
        return (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission(activity: Activity, requestCode: Int, permission: String) {
        ActivityCompat.requestPermissions(
            activity, arrayOf(permission), requestCode)
    }
}