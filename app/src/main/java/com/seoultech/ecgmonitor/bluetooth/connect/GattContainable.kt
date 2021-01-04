package com.seoultech.ecgmonitor.bluetooth.connect

import android.bluetooth.BluetoothGatt

interface GattContainable {
    var gatt: BluetoothGatt?

    fun hasGatt(): Boolean

}