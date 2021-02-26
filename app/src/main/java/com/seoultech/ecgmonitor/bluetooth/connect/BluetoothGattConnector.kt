package com.seoultech.ecgmonitor.bluetooth.connect

import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.seoultech.ecgmonitor.bluetooth.gatt.GattContainable


/**
 * Bluetooth Gatt Connect Class
 */
class BluetoothGattConnector(
    private val context: Context,
    private val gattContainer: GattContainable
) : BluetoothGattConnectible {

    companion object {
        private const val TAG = "BluetoothGattConnector"
    }

    /**
     * Try connect with the bluetooth device
     * @param bluetoothDevice target
     * @param callback The acts when state of connection is changed
     */
    override fun connect(
        bluetoothDevice: BluetoothDevice,
        callback: BluetoothConnectStateCallback
    ) {
        Log.d(TAG, "connect() : Try connection")

        gattContainer.gatt?.close()



        //Connect
        gattContainer.gatt = bluetoothDevice.connectGatt(
            context,
            true,
            object : BluetoothGattCallback() {
                private val TAG = "BluetoothGattCallback"

                override fun onConnectionStateChange(
                    gatt: BluetoothGatt?,
                    status: Int,
                    newState: Int
                ) {
                    when (newState) {
                        //Device is connected
                        BluetoothProfile.STATE_CONNECTED -> {
                            Log.d(TAG, "onConnectionStateChange(): Connected to GATT server")
                            if (gatt == null) {
                                callback.onFailure()
                            } else {

                                //Try to discover services of ths device
                                gatt.discoverServices()
                                callback.onConnected()
                            }
                        }

                        //Device is disconnected
                        BluetoothProfile.STATE_DISCONNECTED -> {
                            Log.d(TAG, "onConnectionStateChange(): Disconnected from GATT server")
                            callback.onDisconnected()
                            if (!gattContainer.hasGatt()) {
                                gatt?.close()
                            }
                        }
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    Log.d(TAG, "onServicesDiscovered(): onServicesDiscovered received : $status")
                    if (gatt == null) {
                        Log.d(TAG, "onServiceDiscovered() : gatt is null")
                    } else {
                        //Find the characteristic has a property of notify --> It will change a value as heart rate
                        val notifyCharacteristic = getNotifyCharacteristic(gatt.services)
                        if (notifyCharacteristic != null) {

                            //Observe this characteristic
                            gatt.setCharacteristicNotification(notifyCharacteristic, true)
                            setEnableNotification(gatt, notifyCharacteristic)
                        }
                    }
                }

                override fun onCharacteristicChanged(
                    gatt: BluetoothGatt?,
                    characteristic: BluetoothGattCharacteristic?
                ) {
                    if (characteristic != null) {
                        //Characteristic is changed --> new heart rate value
                        val value = characteristic.getIntValue(getFormat(characteristic), 0)
                        //Log.d(TAG, "onCharacteristicChanged(): received value : $value")
                        callback.onValueChanged(value.toFloat())
                    } else {
                        Log.d(TAG, "onCharacteristicChanged(): characteristic is null")
                    }
                }
            })
    }

    /**
     * Disconnect with the device.
     */
    override fun disconnect() {
        if (!gattContainer.hasGatt()) {
            Log.d(TAG, "disconnect() : Bluetooth GATT not connected")
        } else {
            gattContainer.gatt!!.disconnect()
            gattContainer.gatt = null
            Log.d(TAG, "disconnect() : Bluetooth GATT disconnected")
        }
    }

    override fun sendAliveMessage() {
        if (!gattContainer.hasGatt()) {
            Log.e(TAG, "sendAliveMessage() : Bluetooth GATT not connected")
        } else {
            val gatt = gattContainer.gatt!!
            val writable = getWritableCharacteristic(gatt.services)
            if (writable != null) {
                writable.value = ByteArray(1)
                gatt.writeCharacteristic(writable)
                Log.d(TAG, "sendAliveMessage() : done!")
            } else {
                Log.e(TAG, "sendAliveMessage() : There is no characteristic writable")
            }
        }
    }

    private fun getWritableCharacteristic(services: List<BluetoothGattService>): BluetoothGattCharacteristic? {
        for (service in services) {
            for (characteristic in service.characteristics) {
                if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                    return characteristic
                }
            }
        }
        return null
    }

    //Find the characteristic has a property of notify --> It will change a value as heart rate
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

    //Observe this characteristic
    private fun setEnableNotification(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        for (descriptor in characteristic.descriptors) {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }
    }
}