package com.seoultech.ecgmonitor.bluetooth

import android.bluetooth.BluetoothGatt
import com.seoultech.ecgmonitor.bluetooth.connect.GattContainable

object GattContainer : GattContainable {
    private var _gatt: BluetoothGatt? = null

    override var gatt: BluetoothGatt?
        get() = _gatt
        set(value) {
            _gatt = value
        }

    override fun hasGatt(): Boolean {
        return _gatt != null
    }
}