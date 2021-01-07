package com.seoultech.ecgmonitor.monitor

import android.bluetooth.*
import androidx.lifecycle.ViewModel
import com.seoultech.ecgmonitor.bluetooth.connect.BluetoothGattConnector
import com.seoultech.ecgmonitor.bluetooth.gatt.GattLiveData

class MonitorViewModel(
    private val bluetoothGattConnector: BluetoothGattConnector,
    private val _gattLiveData: GattLiveData
) : ViewModel() {

    val gattLiveData: GattLiveData
        get() = _gattLiveData

    fun connect(device: BluetoothDevice) {
        bluetoothGattConnector.connect(device, gattLiveData)
    }

    fun disconnect() {
        bluetoothGattConnector.disconnect()
    }
}