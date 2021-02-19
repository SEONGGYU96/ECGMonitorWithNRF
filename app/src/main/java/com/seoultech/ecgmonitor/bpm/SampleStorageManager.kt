package com.seoultech.ecgmonitor.bpm

interface SampleStorageManager {
    fun startSave()

    fun saveSample(sample: Float, time: Long)

    fun safeStopSave()
}