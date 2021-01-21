package com.seoultech.ecgmonitor.heartrate

import androidx.lifecycle.LiveData

class HeartRateLiveData: LiveData<Float>() {

    fun setHeartRateValue(value: Float) {
        postValue(value)
    }
}