package com.seoultech.ecgmonitor.protocol

import android.content.Context
import android.util.Log

class AbnormalProtocol(private val context: Context) {

    companion object {
        private const val TAG = "AbnormalProtocol"
    }

    fun startAbnormalProtocol(averageBpm: Int) {
        Log.d(TAG, "startAbnormalProtocol() : bpm $averageBpm")
        SMSSender(context).send(averageBpm)
    }
}