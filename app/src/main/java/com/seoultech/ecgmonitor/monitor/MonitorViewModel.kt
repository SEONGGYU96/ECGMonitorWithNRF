package com.seoultech.ecgmonitor.monitor

import android.bluetooth.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seoultech.ecgmonitor.bluetooth.connect.BluetoothConnectStateCallback
import com.seoultech.ecgmonitor.bluetooth.connect.BluetoothGattConnector

class MonitorViewModel(private val bluetoothGattConnector: BluetoothGattConnector)
    : ViewModel(), BluetoothConnectStateCallback {

    private val _isConnected = MutableLiveData(false)
    val isConnected : LiveData<Boolean>
        get() = _isConnected

    private val _receivedValue = MutableLiveData(0)
    val receivedValue: LiveData<Int>
        get() = _receivedValue

    private val _isFailure = MutableLiveData(false)
    val isFailure: LiveData<Boolean>
        get() = _isFailure

    fun connect(device: BluetoothDevice) {
        bluetoothGattConnector.connect(device, this)
    }

    fun disconnect() {
        bluetoothGattConnector.disconnect()
    }

    override fun onConnected() {
        _isConnected.postValue(true)
    }

    override fun onDisconnected() {
        _isConnected.postValue(false)
    }

    override fun onValueChanged(value: Int) {
        _receivedValue.postValue(value)
    }

    override fun onFailure() {
        _isFailure.postValue(true)
    }
}