package com.seoultech.ecgmonitor.bpm.data.source

import com.seoultech.ecgmonitor.bpm.data.BPM

interface BPMDataSource {

    interface GetBPMCallback {
        fun onBPMLoaded(bpm: List<BPM>)

        fun onDataNotAvailable()
    }

    interface GetFirstDateCallback {
        fun onFirstDateLoaded(timeInMillis: Long)

        fun onDataNotAvailable()
    }

    fun getRecentBPMs(timeRangeMinute: Int, callback: GetBPMCallback)

    fun getAverageOfBPM(timeRangeMinute: Int, callback: (Int) -> Unit)

    fun getBPMinRange(startTime: Long, endTime: Long, callback: GetBPMCallback)

    fun insertBPM(bpmValue: Int)

    fun getFirstDate(callback: GetFirstDateCallback)
}