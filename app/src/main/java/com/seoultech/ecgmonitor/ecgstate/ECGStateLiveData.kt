package com.seoultech.ecgmonitor.ecgstate

import androidx.lifecycle.LiveData
import com.seoultech.ecgmonitor.bluetooth.connect.BluetoothConnectStateCallback
import com.seoultech.ecgmonitor.bluetooth.gatt.GattContainable
import com.seoultech.ecgmonitor.bluetooth.state.BluetoothStateObservable
import com.seoultech.ecgmonitor.heartrate.HeartRateSnapshotLiveData

class ECGStateLiveData(
    private val heartRateSnapshotLiveData: HeartRateSnapshotLiveData,
    private val gattContainable: GattContainable
    ) : LiveData<ECGStateLiveData>(), BluetoothStateObservable, BluetoothConnectStateCallback {

    private var isBluetoothEnabled = true
    //연결 여부
    private var isConnected = false

    //실패 여부
    private var isFailure = false

    override fun setBluetoothEnabled(enabled: Boolean) {
        isBluetoothEnabled = enabled
        postValue(this)
    }

    override fun onConnected() {
        setIsConnected(true)
    }

    override fun onDisconnected() {
        setIsConnected(false)
    }

    override fun onValueChanged(value: Float) {
        heartRateSnapshotLiveData.setHeartRateSnapshot(value, System.currentTimeMillis())
    }

    override fun onFailure() {
        isFailure = true
        postValue(this)
    }

    private fun setIsConnected(isConnected: Boolean) {
        this.isConnected = isConnected
        postValue(this)
    }

    fun isBluetoothEnabled() = isBluetoothEnabled

    fun isConnected() = isConnected

    fun isFailed() = isFailure

    fun isBounded() = gattContainable.hasGatt()
}
