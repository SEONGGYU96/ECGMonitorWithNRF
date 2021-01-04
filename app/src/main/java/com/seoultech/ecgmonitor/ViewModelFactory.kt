package com.seoultech.ecgmonitor

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.seoultech.ecgmonitor.bluetooth.connect.BluetoothGattConnector
import com.seoultech.ecgmonitor.bluetooth.scan.BluetoothScanner
import com.seoultech.ecgmonitor.bluetooth.scan.ScanStateLiveData
import com.seoultech.ecgmonitor.monitor.MonitorViewModel
import com.seoultech.ecgmonitor.bluetooth.scan.ScanViewModel
import com.seoultech.ecgmonitor.device.DeviceLiveData
import com.seoultech.ecgmonitor.utils.PermissionUtil
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat


class ViewModelFactory private constructor(private val application: Application)
    : ViewModelProvider.NewInstanceFactory() {

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val bluetoothScanner = BluetoothScanner(
        bluetoothAdapter,
        BluetoothLeScannerCompat.getScanner()
    )

    private val scanStateLiveData = ScanStateLiveData(
        bluetoothScanner.isBluetoothEnabled(),
        PermissionUtil.isLocationPermissionsGranted(application)
    )

    private val deviceLiveData = DeviceLiveData()

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(ScanViewModel::class.java) ->
                    ScanViewModel(bluetoothScanner, scanStateLiveData, deviceLiveData)
                isAssignableFrom(MonitorViewModel::class.java) ->
                    MonitorViewModel(BluetoothGattConnector(application))
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile private var INSTANCE: ViewModelFactory? = null
        fun getInstance(application: Application) =
            INSTANCE ?: synchronized(ViewModelFactory::class.java) {
                INSTANCE ?: ViewModelFactory(application).also { INSTANCE = it }
            }
    }
}