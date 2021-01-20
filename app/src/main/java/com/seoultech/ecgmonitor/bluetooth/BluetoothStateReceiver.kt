package com.seoultech.ecgmonitor.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.seoultech.ecgmonitor.ecgstate.ECGStateLiveData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BluetoothStateReceiver(private val bluetoothStateObservable: BluetoothStateObservable)
    : BroadcastReceiver() {

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
                bluetoothStateObservable.setBluetoothEnabled(true)
            }
            BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> {
                if (previousState != BluetoothAdapter.STATE_TURNING_OFF && previousState != BluetoothAdapter.STATE_OFF) {
                    bluetoothStateObservable.setBluetoothEnabled(false)
                }
            }
        }
    }
}