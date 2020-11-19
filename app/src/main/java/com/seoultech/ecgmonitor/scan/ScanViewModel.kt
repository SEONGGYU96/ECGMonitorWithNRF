package com.seoultech.ecgmonitor.scan

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.seoultech.ecgmonitor.device.DeviceLiveData
import com.seoultech.ecgmonitor.utils.BluetoothUtil
import com.seoultech.ecgmonitor.utils.FilterUtils
import com.seoultech.ecgmonitor.utils.PermissionUtil
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ScanViewModel"
    }

    val scanStateLiveData: ScanStateLiveData = ScanStateLiveData(
        BluetoothUtil.isBluetoothEnabled(),
        PermissionUtil.isLocationPermissionsGranted(application)
    )

    val deviceLiveData = DeviceLiveData()

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(bluetoothStateBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "onCleared() : Receiver not registered")
        }
    }

    fun refresh() {
        scanStateLiveData.refresh()
    }

    fun startScan() {
        if (scanStateLiveData.isScanning()) {
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
                deviceLiveData.deviceDiscovered(result)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            for (result in results) {
                if (!isNoise(result)) {
                    scanStateLiveData.setRecordFound()
                    deviceLiveData.deviceDiscovered(result)
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
                        deviceLiveData.bluetoothDisabled()
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