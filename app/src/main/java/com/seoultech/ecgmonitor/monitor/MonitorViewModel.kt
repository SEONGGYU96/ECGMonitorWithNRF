package com.seoultech.ecgmonitor.monitor

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.seoultech.ecgmonitor.bluetooth.connect.BluetoothGattConnectible
import com.seoultech.ecgmonitor.bluetooth.gatt.GattContainable

class MonitorViewModel @ViewModelInject constructor(
    private val bluetoothGattConnector: BluetoothGattConnectible,
    private val gattContainer: GattContainable
) : ViewModel() {

    fun disconnect() {
        bluetoothGattConnector.disconnect()
    }

    //bind된 기기가 있는지 확인
   fun checkBoundedDevice() : Boolean {
        return gattContainer.hasGatt()
    }
}