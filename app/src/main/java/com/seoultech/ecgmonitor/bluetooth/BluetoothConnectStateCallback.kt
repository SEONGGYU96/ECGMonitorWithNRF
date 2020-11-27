package com.seoultech.ecgmonitor.bluetooth

interface BluetoothConnectStateCallback {
    fun onConnected()
    fun onDisconnected()
    fun onValueChanged(value: Int)
    fun onFailure()
}