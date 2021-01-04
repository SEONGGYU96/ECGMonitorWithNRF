package com.seoultech.ecgmonitor.utils

import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.seoultech.ecgmonitor.bluetooth.BluetoothConnectStateCallback
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanSettings

object BluetoothUtil {
    private const val TAG = "BluetoothUtil"
    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val bluetoothLeScanner: BluetoothLeScannerCompat by lazy { BluetoothLeScannerCompat.getScanner() }
    private var gatt : BluetoothGatt? = null

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

    fun connect(context: Context, bluetoothDevice: BluetoothDevice, callback: BluetoothConnectStateCallback) {
        Log.d(TAG, "connect() : Try connection")
        gatt = bluetoothDevice.connectGatt(context, true, object : BluetoothGattCallback() {
            private val TAG = "BluetoothGattCallback"

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d(TAG, "onConnectionStateChange(): Connected to GATT server")
                        if (gatt == null) {
                            callback.onFailure()
                        } else {
                            gatt.discoverServices()
                            callback.onConnected()
                        }
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d(TAG, "onConnectionStateChange(): Disconnected from GATT server")
                        callback.onDisconnected()
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                Log.d(TAG, "onServicesDiscovered(): onServicesDiscovered received : $status")
                if (gatt == null) {
                    Log.d(TAG, "onServiceDiscovered() : gatt is null")
                } else {
                    val notifyCharacteristic = getNotifyCharacteristic(gatt.services)
                    if (notifyCharacteristic != null) {
                        gatt.setCharacteristicNotification(notifyCharacteristic, true)
                        setEnableNotification(gatt, notifyCharacteristic)
                    }
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?,
                                                 characteristic: BluetoothGattCharacteristic?) {
                if (characteristic != null) {
                    val value = characteristic.getIntValue(getFormat(characteristic), 0)
                    //Log.d(TAG, "onCharacteristicChanged(): received value : $value")
                    callback.onValueChanged(value)
                } else {
                    Log.d(TAG, "onCharacteristicChanged(): characteristic is null")
                }
            }
        })
    }

    fun disconnect() {
        if (gatt == null) {
            Log.d(TAG, "disconnect() : Bluetooth GATT not connected")
        } else {
            gatt!!.disconnect()
            Log.d(TAG, "disconnect() : Bluetooth GATT disconnected")
        }
    }

    private fun getFormat(characteristic: BluetoothGattCharacteristic): Int {
        val flag = characteristic.properties
        return when (flag and 0x01) {
            0x01 -> {
                BluetoothGattCharacteristic.FORMAT_UINT16
            }
            else -> {
                BluetoothGattCharacteristic.FORMAT_UINT8
            }
        }
    }

    private fun getNotifyCharacteristic(services: MutableList<BluetoothGattService>): BluetoothGattCharacteristic? {
        for (service in services) {
            for (characteristic in service.characteristics) {
                if (characteristic.properties == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
                    return characteristic
                }
            }
        }
        return null
    }

    private fun setEnableNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        for (descriptor in characteristic.descriptors) {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }
    }
}