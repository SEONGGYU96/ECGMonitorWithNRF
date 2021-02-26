package com.seoultech.ecgmonitor.bpm.data.source.local

import android.util.Log
import com.seoultech.ecgmonitor.bpm.data.BPM
import com.seoultech.ecgmonitor.bpm.data.source.BPMDataSource
import com.seoultech.ecgmonitor.utils.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class BPMLocalDataSource(private val bpmDao: BPMDao) : BPMDataSource {

    companion object {
        private const val TAG = "BPMLocalDataSource"
    }

    override fun getRecentBPMs(
        timeRangeMinute: Int,
        callback: BPMDataSource.GetBPMCallback
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val result = bpmDao.getBpmsAbove(TimeUtil.getMillisecondBefore(timeRangeMinute))
            if (result.isEmpty()) {
                callback.onDataNotAvailable()
            } else {
                callback.onBPMLoaded(result)
            }
        }
    }

    override fun getAverageOfBPM(timeRangeMinute: Int, callback: (Int) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            callback(bpmDao.getAverageOfBPMAbove(TimeUtil.getMillisecondBefore(timeRangeMinute)))
        }
    }

    override fun getBPMinRange(
        startTime: Long,
        endTime: Long,
        callback: BPMDataSource.GetBPMCallback
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val result = bpmDao.getBPMInRange(startTime, endTime)
            if (result.isEmpty()) {
                callback.onDataNotAvailable()
            } else {
                callback.onBPMLoaded(result)
            }
        }
    }

    override fun insertBPM(bpmValue: Int) {
        GlobalScope.launch(Dispatchers.IO) {

            bpmDao.insertBpm(BPM(getNormalizedTime(), bpmValue))
            Log.d(TAG, "insertBPM() : $bpmValue is inserted")
        }
    }

    override fun getFirstDate(callback: BPMDataSource.GetFirstDateCallback) {
        GlobalScope.launch(Dispatchers.IO) {
            val result = bpmDao.getFirstDate()
            if (result == null) {
                callback.onDataNotAvailable()
            } else {
                callback.onFirstDateLoaded(result)
            }
        }
    }

    private fun getNormalizedTime(): Long {
        val current = GregorianCalendar()
        val next = (current.clone() as GregorianCalendar).apply { add(Calendar.MINUTE, 1) }
        TimeUtil.initCalendarBelowMinute(next)
        val before = current.clone() as GregorianCalendar
        TimeUtil.initCalendarBelowMinute(before)
        return if (current.timeInMillis - before.timeInMillis < next.timeInMillis - current.timeInMillis) {
            before.timeInMillis
        } else {
            next.timeInMillis
        }
    }
}