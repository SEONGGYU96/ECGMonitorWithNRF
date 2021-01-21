package com.seoultech.ecgmonitor.heartrate

import androidx.lifecycle.LiveData

class HeartRateSnapshotLiveData: LiveData<Float>() {

    fun setHeartRateValue(value: Float) {
        postValue(value)
    }
}