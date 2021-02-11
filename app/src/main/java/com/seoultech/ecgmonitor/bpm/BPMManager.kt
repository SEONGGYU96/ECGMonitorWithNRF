package com.seoultech.ecgmonitor.bpm

import com.seoultech.ecgmonitor.bpm.calculate.BPMCalculator
import com.seoultech.ecgmonitor.bpm.detect.AbnormalBPMDetector
import com.seoultech.ecgmonitor.protocol.AbnormalProtocol

class BPMManager(
    private val bpmCalculator: BPMCalculator,
    private val abnormalBPMDetector: AbnormalBPMDetector,
    private val abnormalProtocol: AbnormalProtocol
): BPMCalculator.BPMCalculateCallback {

    private var expectedBPMCalculatedCallback: ((Int) -> Unit)? = null

    fun startOperatingBPM(callback: (Int) -> Unit) {
        expectedBPMCalculatedCallback = callback
        bpmCalculator.startCalculating(this)
        abnormalBPMDetector.setOnStartAbnormalProtocolListener { averageBPM ->
            abnormalProtocol.startAbnormalProtocol(averageBPM)
        }
    }

    fun stopOperatingBPM() {
        bpmCalculator.stopCalculating()
    }

    fun addHeartBeatSample(heartBeatSample: Float) {
        bpmCalculator.addValue(heartBeatSample)
    }

    override fun onBPMCalculated(bpm: Int) {
        abnormalBPMDetector.addBpm(bpm)
    }

    override fun onExpectedBPMCalculated(bpm: Int) {
        expectedBPMCalculatedCallback?.let { it(bpm) }
    }
}