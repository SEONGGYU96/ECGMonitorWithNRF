package com.seoultech.ecgmonitor.viewmodel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.seoultech.ecgmonitor.scan.ScanStateLiveData
import com.seoultech.ecgmonitor.utils.BluetoothUtil
import com.seoultech.ecgmonitor.utils.FilterUtils
import com.seoultech.ecgmonitor.utils.PermissionUtil
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ScanViewModel"
    }

    private val scanStateLiveData: ScanStateLiveData = ScanStateLiveData(
        BluetoothUtil.isBluetoothEnabled(),
        PermissionUtil.isLocationPermissionsGranted(application)
    )

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(bluetoothStateBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "onCleared() : Receiver not registered")
        }
    }

    fun getScanState() = scanStateLiveData

    fun refresh() {
        scanStateLiveData.refresh()
    }

    fun startScan() {
        if (scanStateLiveData.isScanning()) {
            Log.d(TAG, "startScan(): already scanning...")
            return
        }
        BluetoothUtil.startScan(scanCallback)
        scanStateLiveData.startScan()
    }

    fun stopScan() {
        BluetoothUtil.stopScan(scanCallback)
        scanStateLiveData.stopScan()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (!isNoise(result)) {
                scanStateLiveData.setRecordFound()
                Log.d(TAG, "device : " + result.device.name)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            for (result in results) {
                if (!isNoise(result)) {
                    scanStateLiveData.setRecordFound()
                    Log.d(TAG, "device : " + result.device.name)
                    return
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            stopScan()
        }
    }

    private fun registerBluetoothStateBroadcastReceiver(application: Application) {
        application.registerReceiver(
            bluetoothStateBroadcastReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
        Log.d(TAG, "register bluetooth state")
    }

    private val bluetoothStateBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                return
            }

            val state = intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF
            )
            val previousState = intent.getIntExtra(
                BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF
            )

            when (state) {
                BluetoothAdapter.STATE_ON -> {
                    scanStateLiveData.setBluetoothEnabled(true)
                }
                BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> {
                    if (previousState != BluetoothAdapter.STATE_TURNING_OFF && previousState != BluetoothAdapter.STATE_OFF) {
                        stopScan()
                        scanStateLiveData.setBluetoothEnabled(false)
                    }
                }
            }
        }
    }

    private fun isNoise(result: ScanResult): Boolean {
        if (!result.isConnectable) {
            return true
        }

        if (result.rssi < -80) {
            return true
        }

        if (FilterUtils.isBeacon(result)) {
            return true
        }

        if (FilterUtils.isAirDrop(result)) {
            return true
        }

        if (FilterUtils.isMeshDevice(result)) {
            return true
        }
        return false
    }

    init {
        //notify when bluetooth goes on or off
        registerBluetoothStateBroadcastReceiver(application)
    }
}