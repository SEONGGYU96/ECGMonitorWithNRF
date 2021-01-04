package com.seoultech.ecgmonitor.bluetooth.scan

import android.bluetooth.BluetoothAdapter
import android.util.Log
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanSettings

/**
 * Bluetooth scan class
 */
class BluetoothScanner(
    private val bluetoothAdapter: BluetoothAdapter?,
    private val bluetoothLeScanner: BluetoothLeScannerCompat
) : BluetoothScanable {

    companion object {
        private const val TAG = "BluetoothScanner"
    }

    /**
     * Check whether bluetooth is enabled or not
     */
    override fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }

    /**
     * Start scanning for BLE devices
     * @param scanCallback The callback called when find BLE devices
     */
    override fun startScan(scanCallback: ScanCallback) {
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(500)
            .setLegacy(false)
            .setUseHardwareBatchingIfSupported(false)
            .build()
        bluetoothLeScanner.startScan(null, settings, scanCallback)
        Log.d(TAG, "startScan() : start")
    }

    /**
     * Stop scanning
     * @param scanCallback The callback which was used to start scan
     */
    override fun stopScan(scanCallback: ScanCallback) {
        bluetoothLeScanner.stopScan(scanCallback)
        Log.d(TAG, "stopScan() : stop")
    }
}