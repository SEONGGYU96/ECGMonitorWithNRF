package com.seoultech.ecgmonitor.heartrate

import androidx.lifecycle.LiveData

class HeartRateLiveData: LiveData<Int>() {

    fun setHeartRateValue(value: Int) {
        postValue(value)
    }
}