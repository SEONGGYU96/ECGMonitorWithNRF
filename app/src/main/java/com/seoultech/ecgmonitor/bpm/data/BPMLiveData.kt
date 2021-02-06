package com.seoultech.ecgmonitor.bpm.data

import androidx.lifecycle.LiveData

class BPMLiveData: LiveData<Int>() {

    fun postBPM(value: Int) {
        postValue(value)
    }
}