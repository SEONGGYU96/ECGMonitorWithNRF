package com.seoultech.ecgmonitor.bluetooth.gatt

import android.bluetooth.BluetoothGatt

interface GattContainable {
    var gatt: BluetoothGatt?

    fun hasGatt(): Boolean

}