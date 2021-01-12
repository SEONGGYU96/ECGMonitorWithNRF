package com.seoultech.ecgmonitor.bluetooth.gatt

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.seoultech.ecgmonitor.bluetooth.connect.BluetoothConnectStateCallback

/**
 * BLE 디바이스와의 연결 상태 및 송신 값 LiveData를 가지고 있는 클래스
 * 싱글턴
 */
class GattLiveData : BluetoothConnectStateCallback {

    //연결 여부
    private var _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean>
        get() = _isConnected

    //수신 값
    private var _receivedValue = MutableLiveData<Float>()
    val receivedValue: LiveData<Float>
        get() = _receivedValue

    //실패 여부
    private var _isFailure = MutableLiveData<Boolean>()
    val isFailure: LiveData<Boolean>
        get() = _isFailure

    override fun onConnected() {
        Log.d(TAG, "onConnected(): Connected!")
        _isConnected.postValue(true)
    }

    override fun onDisconnected() {
        Log.d(TAG, "onConnected(): Disconnected")
        _isConnected.postValue(false)
    }

    override fun onValueChanged(value: Float) {
        _receivedValue.postValue(value)
    }

    override fun onFailure() {
        _isFailure.postValue(true)
    }

    companion object {
        private const val TAG = "GattLiveData"
    }
}