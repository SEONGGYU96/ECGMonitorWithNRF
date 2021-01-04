package com.seoultech.ecgmonitor.bluetooth.connect

interface BluetoothConnectStateCallback {
    fun onConnected()
    fun onDisconnected()
    fun onValueChanged(value: Int)
    fun onFailure()
}