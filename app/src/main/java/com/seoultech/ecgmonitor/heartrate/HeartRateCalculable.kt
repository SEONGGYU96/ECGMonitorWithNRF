package com.seoultech.ecgmonitor.heartrate

import com.seoultech.ecgmonitor.graph.HeartRate

interface HeartRateCalculable {

    fun startCalculating()

    fun stopCalculating()

    fun addValue(value: HeartRate)

}
