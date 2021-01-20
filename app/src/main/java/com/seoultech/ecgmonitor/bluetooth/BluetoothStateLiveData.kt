package com.seoultech.ecgmonitor.bluetooth

import androidx.lifecycle.LiveData

class BluetoothStateLiveData : LiveData<Boolean>(), BluetoothStateObservable {
    override fun setBluetoothEnabled(enabled: Boolean) {
        value = enabled
    }
}