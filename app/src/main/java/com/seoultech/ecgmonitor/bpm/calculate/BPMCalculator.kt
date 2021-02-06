package com.seoultech.ecgmonitor.bpm.calculate

interface BPMCalculator {

    interface BPMCalculateCallback {

        fun onBPMCalculated(bpm: Int)

        fun onExpectedBPMCalculated(bpm: Int)
    }

    fun startCalculating(callback: BPMCalculateCallback)

    fun stopCalculating()

    fun addValue(value: Float)
}
