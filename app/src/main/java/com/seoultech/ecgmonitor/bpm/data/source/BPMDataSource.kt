package com.seoultech.ecgmonitor.bpm.data.source

import com.seoultech.ecgmonitor.bpm.data.BPM

interface BPMDataSource {

    interface GetRecentBPMsCallback {
        fun onRecentBPMsLoaded(bpms: List<BPM>)

        fun onDataNotAvailable()
    }

    fun getRecentBPMs(timeRangeMinute: Int, callback: GetRecentBPMsCallback)

    fun getAverageOfBPM(timeRangeMinute: Int, callback: (Int) -> Unit)

    fun insertBPM(bpmValue: Int)
}