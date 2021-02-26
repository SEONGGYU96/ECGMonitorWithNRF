package com.seoultech.ecgmonitor.utils

import android.content.Context
import android.location.LocationManager

object LocationUtil {

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE)
                as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}