package com.seoultech.ecgmonitor.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class ConnectRequest(private val device: BluetoothDevice) {

    fun connect() {
        Connect().start()
    }

    inner class Connect : Thread() {
        private val TAG = "Connect"

        private val socket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            //device.fetchUuidsWithSdp()
            device.createRfcommSocketToServiceRecord(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"))
        }

        private var inputStream: InputStream? = null
        private var outputStream: OutputStream? = null
        private val buffer = ByteArray(1024)

        override fun run() {
            socket?.use { socket ->
                socket.connect()

                var numBytes: Int

                inputStream = socket.inputStream
                outputStream = socket.outputStream

                while (true) {
                    numBytes = try {
                        inputStream!!.read(buffer)
                    } catch (e: IOException) {
                        Log.e(TAG, "Input stream was disconnected", e)
                        break
                    }
                    Log.d(TAG, "Data : $numBytes")
                }
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "cancel() : Could not close the client socket", e)
            }
        }
    }
}