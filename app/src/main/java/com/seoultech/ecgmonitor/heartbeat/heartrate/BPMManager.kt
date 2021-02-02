package com.seoultech.ecgmonitor.heartbeat.heartrate

import android.util.Log

class BPMManager {

    companion object {
        private const val TAG = "BPMManager"
        private const val MAXIMUM_BPM_NORMAL = 100
        private const val MINIMUM_BPM_NORMAL = 40
        private const val CRITERIA_ABNORMAL_MINUTE = 10
    }

    private val bpms = mutableListOf<Int>()
    private var sumOfBpms = 0

    fun addBpm(bpm: Int) {
        Log.d(TAG, "addBPM : $bpm")
        if (checkIsAbnormal(bpm) && checkWasAbnormal()) {
            startAbnormalProtocol()
        }
        bpms.add(bpm)
        sumOfBpms += bpm
    }

    private fun startAbnormalProtocol() {
        Log.d(TAG, "startAbnormalProtocol() : Abnormal!!!")
    }

    private fun checkIsAbnormal(bpm: Int): Boolean {
        if (bpm > MAXIMUM_BPM_NORMAL || bpm < MINIMUM_BPM_NORMAL) {
            Log.d(TAG, "checkIsAbnormal() : It is abnormal")
            return true
        }
        Log.d(TAG, "checkIsAbnormal() : It is normal")
        return false
    }

    private fun checkWasAbnormal(): Boolean {
        if (bpms.size < CRITERIA_ABNORMAL_MINUTE) {
            return false
        }
        for (i in 1 until CRITERIA_ABNORMAL_MINUTE) {
            if (!checkIsAbnormal(bpms[bpms.size - i])) {
                return false
            }
        }
        Log.d(TAG, "It was abnormal during $CRITERIA_ABNORMAL_MINUTE")
        return true
    }

    fun getAverageAndFlush(): Int {
        val average = sumOfBpms / bpms.size
        sumOfBpms = 0
        bpms.clear()
        return average
    }
}