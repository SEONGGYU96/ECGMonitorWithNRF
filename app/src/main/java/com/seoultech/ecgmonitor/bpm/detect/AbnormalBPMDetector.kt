package com.seoultech.ecgmonitor.bpm.detect

import com.seoultech.ecgmonitor.protocol.AbnormalProtocol

interface AbnormalBPMDetector {

    fun addBpm(bpm: Int)

    fun setOnStartAbnormalProtocolListener(listener: (Int, AbnormalProtocol.AbnormalType) -> Unit)
}