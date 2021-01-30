package com.seoultech.ecgmonitor.heartbeat.heartrate

import androidx.lifecycle.LiveData

class HeartRateLiveData: LiveData<Int>() {

    fun setHeartRateValue(value: Int) {
        postValue(value)
    }
}