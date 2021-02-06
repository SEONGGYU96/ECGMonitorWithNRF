package com.seoultech.ecgmonitor.protocol

interface AbnormalProtocol {
    fun startAbnormalProtocol(averageBpm: Int)
}