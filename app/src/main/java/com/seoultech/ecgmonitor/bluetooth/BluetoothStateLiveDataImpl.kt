package com.seoultech.ecgmonitor.bluetooth

class BluetoothStateLiveDataImpl : BluetoothStateLiveData() {

    override fun setBluetoothEnabled(enabled: Boolean) {
        value = enabled
    }
}