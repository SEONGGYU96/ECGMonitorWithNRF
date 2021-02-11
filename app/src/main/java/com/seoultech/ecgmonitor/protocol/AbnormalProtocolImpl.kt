package com.seoultech.ecgmonitor.protocol

import android.content.Context
import android.util.Log

class AbnormalProtocolImpl(private val context: Context) : AbnormalProtocol {

    companion object {
        private const val TAG = "AbnormalProtocol"
    }

    override fun startAbnormalProtocol(averageBpm: Int, type: AbnormalProtocol.AbnormalType) {

        Log.d(TAG, "startAbnormalProtocol() : bpm $averageBpm")
        SMSSender(context).send(type, averageBpm)
        AbnormalNotification(context).showNotification(type, averageBpm)
    }
}