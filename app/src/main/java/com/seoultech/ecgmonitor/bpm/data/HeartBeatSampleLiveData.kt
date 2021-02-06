package com.seoultech.ecgmonitor.bpm.data

import androidx.lifecycle.LiveData

class HeartBeatSampleLiveData: LiveData<HeartBeatSampleLiveData>() {

    private var _value = 0f
    val value : Float
        get() = _value

    private var _time = 0L
    val time: Long
        get() = _time

    fun setHeartRateSnapshot(value: Float, time: Long) {
        this._value = value
        this._time = time
        postValue(this)
    }
}