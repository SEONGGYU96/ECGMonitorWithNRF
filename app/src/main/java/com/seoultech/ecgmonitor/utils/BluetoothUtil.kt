package com.seoultech.ecgmonitor.utils

import android.bluetooth.BluetoothAdapter
import android.util.Log
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanSettings

object BluetoothUtil {
    private const val TAG = "BluetoothUtil"
    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val bluetoothLeScanner: BluetoothLeScannerCompat by lazy { BluetoothLeScannerCompat.getScanner() }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter != null && bluetoothAdapter!!.isEnabled
    }

    fun startScan(scanCallback: ScanCallback) {
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(500)
            .setLegacy(false)
            .setUseHardwareBatchingIfSupported(false)
            .build()
        bluetoothLeScanner.startScan(null, settings, scanCallback)
        Log.d(TAG, "startScan() : start")
    }

    fun stopScan(scanCallback: ScanCallback) {
        bluetoothLeScanner.stopScan(scanCallback)
        Log.d(TAG, "stopScan() : stop")
    }
}