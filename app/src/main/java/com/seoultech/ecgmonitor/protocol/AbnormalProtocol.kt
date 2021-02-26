package com.seoultech.ecgmonitor.protocol

interface AbnormalProtocol {
    fun startAbnormalProtocol(averageBpm: Int, type: AbnormalType)

    enum class AbnormalType {
        Tachycardia, Bradycardia, Arrhythmia
    }
}