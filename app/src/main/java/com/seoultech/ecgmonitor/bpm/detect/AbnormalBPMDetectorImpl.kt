package com.seoultech.ecgmonitor.bpm.detect

import android.util.Log
import com.seoultech.ecgmonitor.bpm.data.BPM
import com.seoultech.ecgmonitor.bpm.data.source.BPMDataSource
import com.seoultech.ecgmonitor.protocol.AbnormalProtocol

class AbnormalBPMDetectorImpl(
    private val bpmDataSource: BPMDataSource
): AbnormalBPMDetector {

    companion object {
        private const val TAG = "abnormalBPMDetector"
        private const val MAXIMUM_BPM_NORMAL = 100
        private const val MINIMUM_BPM_NORMAL = 40
        private const val CRITERIA_ABNORMAL_MINUTE = 5
    }

    private var abnormalListener: ((Int, AbnormalProtocol.AbnormalType) -> Unit)? = null

    override fun addBpm(bpm: Int) {
        Log.d(TAG, "addBPM : $bpm")
        if (checkIsAbnormal(bpm)) {
            checkWasAbnormal {
                if (it) {
                    startAbnormalProtocol()
                }
            }
        }
        bpmDataSource.insertBPM(bpm)
    }

    override fun setOnStartAbnormalProtocolListener(listener: (Int, AbnormalProtocol.AbnormalType) -> Unit) {
        abnormalListener = listener
    }

    private fun startAbnormalProtocol() {
        Log.d(TAG, "startAbnormalProtocol() : Abnormal!!!")
        getAverageFrom { average ->
            abnormalListener?.let { listener -> listener(average, getAbnormalType(average)) }
        }
    }

    private fun getAbnormalType(averageBPM: Int): AbnormalProtocol.AbnormalType {
        return if (averageBPM > MAXIMUM_BPM_NORMAL) {
            AbnormalProtocol.AbnormalType.Tachycardia
        } else {
            AbnormalProtocol.AbnormalType.Bradycardia
        }
        //Todo: 부정맥도 감지하여야 함
    }

    private fun checkIsAbnormal(bpm: Int): Boolean {
        if (bpm > MAXIMUM_BPM_NORMAL || bpm < MINIMUM_BPM_NORMAL) {
            Log.d(TAG, "checkIsAbnormal() : It is abnormal")
            return true
        }
        Log.d(TAG, "checkIsAbnormal() : It is normal")
        return false
    }

    private fun checkWasAbnormal(callback: (wasAbnormal: Boolean) -> Unit) {
        bpmDataSource.getRecentBPMs(
            CRITERIA_ABNORMAL_MINUTE,
            object : BPMDataSource.GetRecentBPMsCallback {
                override fun onRecentBPMsLoaded(bpms: List<BPM>) {
                    if (bpms.size < CRITERIA_ABNORMAL_MINUTE) {
                        callback(false)
                    } else {
                        for (bpm in bpms) {
                            if (!checkIsAbnormal(bpm.bpm)) {
                                callback(false)
                                return
                            }
                        }
                        Log.d(TAG, "It was abnormal during $CRITERIA_ABNORMAL_MINUTE")
                        callback(true)
                    }
                }

                override fun onDataNotAvailable() {
                    callback(false)
                }
            })
    }

    private fun getAverageFrom(callback: (Int) -> Unit) {
        bpmDataSource.getAverageOfBPM(CRITERIA_ABNORMAL_MINUTE, callback)
    }
}