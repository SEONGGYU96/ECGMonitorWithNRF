package com.seoultech.ecgmonitor.heartbeat.heartrate

interface HeartRateCalculator {

    fun startCalculating()

    fun stopCalculating()

    fun addValue(value: Float)

    fun setOnBPMCalculatedListener(listener: (Int) -> Unit)
}
