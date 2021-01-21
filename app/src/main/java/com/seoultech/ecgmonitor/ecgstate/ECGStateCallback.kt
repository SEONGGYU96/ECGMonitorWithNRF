package com.seoultech.ecgmonitor.ecgstate

interface ECGStateCallback {
    fun beforeBounded()

    fun onBluetoothDisabled()

    fun onBluetoothEnabled()

    fun onConnected()

    fun onDisconnected()

    fun onFailure()
}