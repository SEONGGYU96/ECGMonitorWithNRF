package com.seoultech.ecgmonitor.monitor

import android.app.Application
import android.bluetooth.*
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.seoultech.ecgmonitor.utils.BluetoothUtil

class MonitorViewModel(application: Application) : AndroidViewModel(application) {

    private val characteristics = mutableListOf<BluetoothGattCharacteristic>()

    fun connect(device: BluetoothDevice) {
        BluetoothUtil.connect(getApplication(), device, callback)
    }

    private val callback = object : BluetoothGattCallback() {
        private val TAG = "BluetoothGattCallback"

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "onConnectionStateChange(): Connected to GATT server")
                    gatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "onConnectionStateChange(): Disconnected from GATT server")
                }
            }

        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.d(TAG, "onServicesDiscovered(): onServicesDiscovered received : $status")
            gatt?.let {
                for (service in it.services) {
                    for (characteristic in service.characteristics) {
                        if (characteristic.properties == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
                            gatt.setCharacteristicNotification(characteristic, true)
                            for (descriptor in characteristic.descriptors) {
                                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                gatt.writeDescriptor(descriptor)
                            }
                            Log.d(TAG, "start observing value of ${characteristic.uuid}")
                        }
                    }
                }
            }?: run {
                Log.d(TAG, "onServicesDiscovered(): gatt is null")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?,
                                          characteristic: BluetoothGattCharacteristic?,
                                          status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic != null) {
                    val flag = characteristic.properties
                    val format = when (flag and 0x01) {
                        0x01 -> {
                            Log.d(TAG, "onCharacteristicChanged(): format UINT16.")
                            BluetoothGattCharacteristic.FORMAT_UINT16
                        }
                        else -> {
                            Log.d(TAG, "onCharacteristicChanged(): format UINT8.")
                            BluetoothGattCharacteristic.FORMAT_UINT8
                        }
                    }
                    val value = characteristic.getIntValue(format, 1)
                    Log.d(TAG, "onCharacteristicChanged(): received value : $value")
                } else {
                    Log.d(TAG, "onCharacteristicChanged(): characteristic is null")
                }
            } else {
                Log.d(TAG, "onCharacteristicChanged(): status is not success")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?,
                                             characteristic: BluetoothGattCharacteristic?) {
            Log.d(TAG, "onCharacteristicChanged()")

            if (characteristic != null) {
                val flag = characteristic.properties
                val format = when (flag and 0x01) {
                    0x01 -> {
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    }
                    else -> {
                        BluetoothGattCharacteristic.FORMAT_UINT8
                    }
                }
                val value = characteristic.getIntValue(format, 0)
                Log.d(TAG, "onCharacteristicChanged(): received value : $value")
            } else {
                Log.d(TAG, "onCharacteristicChanged(): characteristic is null")
            }
        }
    }
}