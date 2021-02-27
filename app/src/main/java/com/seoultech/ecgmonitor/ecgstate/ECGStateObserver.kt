package com.seoultech.ecgmonitor.ecgstate

import androidx.lifecycle.Observer

class ECGStateObserver(private val ecgStateCallback: ECGStateCallback): Observer<BluetoothConnectStateLiveData> {
    private var isBluetoothDisabled = false

    override fun onChanged(bluetoothConnectStateLiveData: BluetoothConnectStateLiveData) {
        if (!bluetoothConnectStateLiveData.isBounded()) {
            ecgStateCallback.beforeBounded()
        } else {
            if (!bluetoothConnectStateLiveData.isBluetoothEnabled()) {
                isBluetoothDisabled = true
                ecgStateCallback.onBluetoothDisabled()
            } else {
                if (isBluetoothDisabled) {
                    ecgStateCallback.onBluetoothEnabled()
                    isBluetoothDisabled = false
                } else {
                    if (bluetoothConnectStateLiveData.isFailed()) {
                        ecgStateCallback.onFailure()
                    } else {
                        if (bluetoothConnectStateLiveData.isConnected()) {
                            ecgStateCallback.onConnected()
                        } else {
                            ecgStateCallback.onDisconnected()
                        }
                    }
                }
            }
        }
    }
}