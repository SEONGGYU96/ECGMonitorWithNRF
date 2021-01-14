package com.seoultech.ecgmonitor.heartrate

import androidx.lifecycle.LiveData

class HeartRateLiveData : LiveData<Int>() {
    fun setHeartRate(heartRate: Int) {
        postValue(heartRate)
    }
}