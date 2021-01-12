package com.seoultech.ecgmonitor.bluetooth.gatt

import android.bluetooth.BluetoothGatt

/**
 * BLE 디바이스와 GATT Connect 후 반환받은 Gatt 객체를 갖고 있는 클래스
 * 싱글턴으로 구현되어 앱 실행 중 유일한 객체
 * Gatt 를 갖고 있지 않으면 연결이 되어있지 않고 재시도도 하지 않는 상태
 */
class GattContainer: GattContainable {

    private var _gatt: BluetoothGatt? = null

    override var gatt: BluetoothGatt?
        get() = _gatt
        set(value) {
            _gatt = value
        }

    override fun hasGatt(): Boolean {
        return _gatt != null
    }
}