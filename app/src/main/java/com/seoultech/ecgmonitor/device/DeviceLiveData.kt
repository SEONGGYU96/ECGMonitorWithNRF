package com.seoultech.ecgmonitor.device

import androidx.lifecycle.LiveData
import no.nordicsemi.android.support.v18.scanner.ScanResult

class DeviceLiveData : LiveData<List<Device>?>() {

    private val devices: MutableList<Device>? = mutableListOf()

    @Synchronized
    fun bluetoothDisabled() {
        devices?.clear()
        postValue(null)
    }

    @Synchronized
    fun deviceDiscovered(scanResult: ScanResult) {
        val index = indexOf(scanResult)
        val currentDevice =  if (index == -1) {
            val device = Device(scanResult)
            devices?.add(device)
            device
        } else {
            devices?.get(index)
        }

        currentDevice?.update()
        postValue(devices)
    }

    @Synchronized
    fun clear() {
        devices?.clear()
        postValue(null)
    }

    private fun indexOf(scanResult: ScanResult) : Int {
        if (devices != null) {
            for (i in 0 until devices.size) {
                if (devices[i].matches(scanResult)) {
                    return i
                }
            }
        }
        return -1
    }
}