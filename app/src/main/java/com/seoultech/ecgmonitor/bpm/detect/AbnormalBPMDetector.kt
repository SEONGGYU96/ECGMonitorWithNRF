package com.seoultech.ecgmonitor.bpm.detect

interface AbnormalBPMDetector {

    fun addBpm(bpm: Int)

    fun setOnStartAbnormalProtocolListener(listener: (Int) -> Unit)
}