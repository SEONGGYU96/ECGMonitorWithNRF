package com.seoultech.ecgmonitor.monitor

import android.app.Application
import android.bluetooth.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.seoultech.ecgmonitor.bluetooth.BluetoothConnectStateCallback
import com.seoultech.ecgmonitor.utils.BluetoothUtil

class MonitorViewModel(application: Application)
    : AndroidViewModel(application), BluetoothConnectStateCallback {

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
        BluetoothUtil.connect(getApplication(), device, this)
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