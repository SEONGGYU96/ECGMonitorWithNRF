package com.seoultech.ecgmonitor.bluetooth.scan

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.seoultech.ecgmonitor.SecretString
import com.seoultech.ecgmonitor.bluetooth.util.FilterUtils
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult

class ScanViewModel @ViewModelInject constructor(
    private val bluetoothScanner: BluetoothScanable,
    private val _scanStateLiveData: ScanStateLiveData
) : ViewModel() {

    companion object {
        private const val TAG = "ScanViewModel"
        private const val UUID = SecretString.UUID
    }

    //For scan state observing
    val scanStateLiveData: ScanStateLiveData
        get() = _scanStateLiveData

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
    }

    /**
     * Clear devices founded
     */
    fun clearDevices() {
        _scanStateLiveData.clearRecords()
    }

    private fun validateResult(result: ScanResult) {
        if (!FilterUtils.isNoise(result) && result.scanRecord != null) {
            val uuids = result.scanRecord!!.serviceUuids
            if (uuids != null) {
                if (uuids[0].uuid.toString() == UUID) {
                    _scanStateLiveData.setRecordFound(result.device)
                    stopScan()
                }
            }
        }
    }



//Scan callback which is called after finding devices
private val scanCallback = object : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult) {
        validateResult(result)
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>) {
        for (result in results) {
            validateResult(result)
        }
    }

    override fun onScanFailed(errorCode: Int) {
        stopScan()
    }
}
}