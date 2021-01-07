package com.seoultech.ecgmonitor.bluetooth

import android.bluetooth.BluetoothGatt

interface GattContainable {
    var gatt: BluetoothGatt?

    fun hasGatt(): Boolean

}