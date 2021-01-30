package com.seoultech.ecgmonitor.heartrate

interface HeartRateCalculable {

    fun startCalculating()

    fun stopCalculating()

    fun addValue(value: Float)
}
