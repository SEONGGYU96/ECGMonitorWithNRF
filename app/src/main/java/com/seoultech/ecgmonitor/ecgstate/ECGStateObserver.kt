package com.seoultech.ecgmonitor.ecgstate

import androidx.lifecycle.Observer

class ECGStateObserver(private val ecgStateCallback: ECGStateCallback): Observer<ECGStateLiveData> {
    private var isBluetoothDisabled = false

    override fun onChanged(ecgStateLiveData: ECGStateLiveData) {
        if (!ecgStateLiveData.isBounded()) {
            ecgStateCallback.beforeBounded()
        } else {
            if (!ecgStateLiveData.isBluetoothEnabled()) {
                isBluetoothDisabled = true
                ecgStateCallback.onBluetoothDisabled()
            } else {
                if (isBluetoothDisabled) {
                    ecgStateCallback.onBluetoothEnabled()
                    isBluetoothDisabled = true
                } else {
                    if (ecgStateLiveData.isFailed()) {
                        ecgStateCallback.onFailure()
                    } else {
                        if (ecgStateLiveData.isConnected()) {
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