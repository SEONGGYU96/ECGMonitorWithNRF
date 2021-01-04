package com.seoultech.ecgmonitor.bluetooth.connect

import android.bluetooth.BluetoothDevice

interface BluetoothGattConnectible {

    fun connect(bluetoothDevice: BluetoothDevice, callback: BluetoothConnectStateCallback)

    fun disconnect()
}