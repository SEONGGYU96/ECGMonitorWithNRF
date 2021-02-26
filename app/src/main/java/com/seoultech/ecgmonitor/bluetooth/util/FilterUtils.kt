package com.seoultech.ecgmonitor.bluetooth.util

import android.os.ParcelUuid
import no.nordicsemi.android.support.v18.scanner.ScanResult

object FilterUtils {
    private val EDDYSTONE_UUID =
        ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805f9b34fb")
    private val MESH_PROVISIONING_UUID =
        ParcelUuid.fromString("00001827-0000-1000-8000-00805f9b34fb")
    private val MESH_PROXY_UUID =
        ParcelUuid.fromString("00001828-0000-1000-8000-00805f9b34fb")

    private const val COMPANY_ID_MICROSOFT = 0x0006
    private const val COMPANY_ID_APPLE = 0x004C
    private const val COMPANY_ID_NORDIC_SEMI = 0x0059

    private const val EIR_TYPE_MESH_MESSAGE = 0x2A
    private const val EIR_TYPE_MESH_BEACON = 0x2B

    fun isBeacon(result: ScanResult?): Boolean {
        if (result != null && result.scanRecord != null) {
            val record = result.scanRecord
            val appleData = record!!.getManufacturerSpecificData(COMPANY_ID_APPLE)
            if (appleData != null) {
                // iBeacons
                if (appleData.size == 23 && appleData[0].toInt() == 0x02 && appleData[1].toInt() == 0x15) return true
            }
            val nordicData = record.getManufacturerSpecificData(COMPANY_ID_NORDIC_SEMI)
            if (nordicData != null) {
                // Nordic Beacons
                if (nordicData.size == 23 && nordicData[0].toInt() == 0x02 && nordicData[1].toInt() == 0x15) return true
            }
            val microsoftData = record.getManufacturerSpecificData(COMPANY_ID_MICROSOFT)
            if (microsoftData != null) {
                // Microsoft Advertising Beacon
                if (microsoftData[0].toInt() == 0x01) // Scenario Type = Advertising Beacon
                    return true
            }

            // Eddystone
            val eddystoneData = record.getServiceData(EDDYSTONE_UUID)
            if (eddystoneData != null) return true
        }
        return false
    }

    fun isAirDrop(result: ScanResult?): Boolean {
        if (result != null && result.scanRecord != null) {
            val record = result.scanRecord

            // iPhones and iMacs advertise with AirDrop packets
            val appleData = record!!.getManufacturerSpecificData(COMPANY_ID_APPLE)
            return appleData != null && appleData.size > 1 && appleData[0].toInt() == 0x10
        }
        return false
    }

    fun isMeshDevice(result: ScanResult?): Boolean {
        if (result != null && result.scanRecord != null) {
            val record = result.scanRecord

            // GATT bearer
            if (record!!.getServiceData(MESH_PROVISIONING_UUID) != null
                || record.getServiceData(MESH_PROXY_UUID) != null
            ) return true

            // ADV bearer
            val rawData = record.bytes
            if (rawData != null) {
                var offset = 0
                while (offset < rawData.size) {
                    val length: Int = rawData[offset++].toInt() and 0xFF
                    if (length == 0 || rawData.size < offset) return false
                    val type: Int = rawData[offset].toInt() and 0xFF
                    if (type == EIR_TYPE_MESH_BEACON || type == EIR_TYPE_MESH_MESSAGE) return true
                    offset += length
                }
            }
        }
        return false
    }

    fun isNoise(result: ScanResult): Boolean {
        if (!result.isConnectable) {
            return true
        }

        if (result.rssi < -80) {
            return true
        }

        if (isBeacon(result)) {
            return true
        }

        if (isAirDrop(result)) {
            return true
        }

        if (isMeshDevice(result)) {
            return true
        }
        return false
    }
}