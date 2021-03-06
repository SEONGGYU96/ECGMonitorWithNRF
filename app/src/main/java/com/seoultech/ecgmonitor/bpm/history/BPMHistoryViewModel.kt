package com.seoultech.ecgmonitor.bpm.history

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seoultech.ecgmonitor.bpm.data.BPM
import com.seoultech.ecgmonitor.bpm.data.source.BPMDataSource
import com.seoultech.ecgmonitor.utils.TimeUtil
import java.util.*

class BPMHistoryViewModel @ViewModelInject constructor(
    private val bpmDataSource: BPMDataSource
): ViewModel() {

    private val _bpmData = MutableLiveData<List<BPM>>()
    val bpmData : LiveData<List<BPM>>
        get() = _bpmData

    private val _firstDate = MutableLiveData<Long?>(null)
    val firstDate: LiveData<Long?>
        get() = _firstDate

    private fun getFirstDate() {
        bpmDataSource.getFirstDate(object: BPMDataSource.GetFirstDateCallback {
            override fun onFirstDateLoaded(timeInMillis: Long) {
                _firstDate.postValue(timeInMillis)
                getBPMDataOnDate(GregorianCalendar())
            }

            override fun onDataNotAvailable() {
                _firstDate.value = -1L
            }
        })
    }

    fun getBPMDataOnDate(baseCalendar: Calendar) {
        val startTime = GregorianCalendar().apply { timeInMillis = baseCalendar.timeInMillis }
        val endTime = GregorianCalendar().apply { timeInMillis = baseCalendar.timeInMillis }
        TimeUtil.initCalendarBelowDay(startTime)
        TimeUtil.initCalendarBelowDayToFull(endTime)
        bpmDataSource.getBPMinRange(startTime.timeInMillis, endTime.timeInMillis, object: BPMDataSource.GetBPMCallback {
            override fun onBPMLoaded(bpm: List<BPM>) {
                _bpmData.postValue(bpm)
            }

            override fun onDataNotAvailable() {
                _bpmData.postValue(mutableListOf())
            }
        })
    }

    fun refresh() {
        getFirstDate()
    }
}