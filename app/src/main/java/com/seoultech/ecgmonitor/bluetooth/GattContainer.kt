package com.seoultech.ecgmonitor.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt

class GattContainer private constructor(): GattContainable {

    private var _gatt: BluetoothGatt? = null

    override var gatt: BluetoothGatt?
        get() = _gatt
        set(value) {
            _gatt = value
        }

    override fun hasGatt(): Boolean {
        return _gatt != null
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile private var INSTANCE: GattContainer? = null
        fun getInstance() =
            INSTANCE ?: synchronized(GattContainer::class.java) {
                INSTANCE ?: GattContainer().also { INSTANCE = it }
            }
    }
}