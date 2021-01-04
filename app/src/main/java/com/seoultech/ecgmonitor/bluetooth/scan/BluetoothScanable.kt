package com.seoultech.ecgmonitor.bluetooth.scan

import no.nordicsemi.android.support.v18.scanner.ScanCallback

interface BluetoothScanable {

    fun isBluetoothEnabled(): Boolean

    fun startScan(scanCallback: ScanCallback)

    fun stopScan(scanCallback: ScanCallback)
}