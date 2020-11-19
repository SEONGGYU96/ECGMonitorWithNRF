package com.seoultech.ecgmonitor.device

import android.bluetooth.BluetoothDevice
import android.os.Parcel
import android.os.Parcelable
import no.nordicsemi.android.support.v18.scanner.ScanResult

class Device(private val scanResult: ScanResult) : Parcelable {

    var device: BluetoothDevice? = scanResult.device

    var name: String? = scanResult.scanRecord?.deviceName

    val address
        get() = device?.address

    constructor(parcel: Parcel)
            : this(parcel.readParcelable<ScanResult>(ScanResult::class.java.classLoader)!!) {
        device = parcel.readParcelable(BluetoothDevice::class.java.classLoader)
        name = parcel.readString()
    }

    fun update() {
        device = scanResult.device
        name = scanResult.scanRecord?.deviceName
    }

    fun matches(scanResult: ScanResult) : Boolean {
        if (device == null) {
            return false
        }
        return device!!.address == scanResult.device.address
    }

    override fun equals(other: Any?): Boolean {
        if (other is Device) {
            if (device == null || other.device == null) {
                return false
            }
            return device!!.address == other.device!!.address
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return device.hashCode()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.run {
            writeParcelable(device, flags)
            writeString(name)
        }
    }

    companion object CREATOR : Parcelable.Creator<Device> {
        override fun createFromParcel(parcel: Parcel): Device {
            return Device(parcel)
        }

        override fun newArray(size: Int): Array<Device?> {
            return arrayOfNulls(size)
        }
    }
}