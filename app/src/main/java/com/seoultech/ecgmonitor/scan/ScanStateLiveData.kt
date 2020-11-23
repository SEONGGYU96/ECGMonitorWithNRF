package com.seoultech.ecgmonitor.scan

import androidx.lifecycle.LiveData

class ScanStateLiveData(private var bluetoothEnabled: Boolean, private var locationEnabled: Boolean)
    : LiveData<ScanStateLiveData>() {

    private var isScanStarted = false
    private var hasRecords = false

    init {
        postValue(this)
    }

    fun refresh() = postValue(this)

    fun startScan() {
        isScanStarted = true
        postValue(this)
    }

    fun stopScan() {
        isScanStarted = false
        postValue(this)
    }

    fun setBluetoothEnabled(isEnabled: Boolean) {
        bluetoothEnabled = isEnabled
        if (!isEnabled) {
            hasRecords = false
        }
        postValue(this)
    }

    fun setLocationEnabled(isEnabled: Boolean) {
        locationEnabled = isEnabled
        postValue(this)
    }

    fun setRecordFound() {
        hasRecords = true
        postValue(this)
    }

    fun isScanning() = isScanStarted

    fun hasRecords() = hasRecords

    fun isLocationEnabled() = locationEnabled

    fun isBluetoothEnabled() = bluetoothEnabled

    fun clearRecords() {
        hasRecords = false
        postValue(this)
    }
}