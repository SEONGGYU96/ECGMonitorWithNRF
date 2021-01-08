package com.seoultech.ecgmonitor.service

import android.content.Context
import com.seoultech.ecgmonitor.bluetooth.connect.BluetoothGattConnector
import com.seoultech.ecgmonitor.bluetooth.gatt.GattContainer

/**
 * GattConnector 조립기
 */
object GattConnectorAssembler {
    fun inject(context: Context): BluetoothGattConnector {
        return BluetoothGattConnector(context, GattContainer.getInstance())
    }
 }