package com.seoultech.ecgmonitor.bluetooth.scan

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.seoultech.ecgmonitor.device.DeviceLiveData
import com.seoultech.ecgmonitor.bluetooth.FilterUtils
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult

class ScanViewModel @ViewModelInject constructor(
    private val bluetoothScanner: BluetoothScanable,
    private val _scanStateLiveData: ScanStateLiveData,
    private val _deviceLiveData: DeviceLiveData,
) : ViewModel() {

    companion object {
        private const val TAG = "ScanViewModel"
    }

    //For scan state observing
    val scanStateLiveData: ScanStateLiveData
        get() = _scanStateLiveData

    //For results of scanning
    val deviceLiveData: DeviceLiveData
        get() = _deviceLiveData

    /**
     * Refresh scan state after obtaining permission
     */
    fun refresh() {
        _scanStateLiveData.refresh()
    }

    /**
     * Start sacnning for BLE devices
     */
    fun startScan() {
        //If scan is already running, return.
        if (_scanStateLiveData.isScanning()) {
            return
        }
        bluetoothScanner.startScan(scanCallback)
        _scanStateLiveData.startScan()
    }

    /**
     * Stop scanning
     */
    fun stopScan() {
        bluetoothScanner.stopScan(scanCallback)
        _scanStateLiveData.stopScan()
    }

    /**
     * Set bluetooth enable state
     */
    fun setBluetoothEnabled(isEnabled: Boolean) {
        _scanStateLiveData.setBluetoothEnabled(isEnabled)
        if (!isEnabled) {
        _deviceLiveData.bluetoothDisabled()
        }
    }

    /**
     * Clear devices founded
     */
    fun clearDevices() {
        _deviceLiveData.clear()
        _scanStateLiveData.clearRecords()
    }


    //Scan callback which is called after finding devices
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (!FilterUtils.isNoise(result)) {
                _scanStateLiveData.setRecordFound()

                //Add device which just has found.
                _deviceLiveData.deviceDiscovered(result)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            for (result in results) {
                if (!FilterUtils.isNoise(result)) {
                    _scanStateLiveData.setRecordFound()

                    //Add device which just has found.
                    _deviceLiveData.deviceDiscovered(result)
                    return
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            stopScan()
        }
    }
}