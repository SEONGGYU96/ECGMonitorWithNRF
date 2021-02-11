package com.seoultech.ecgmonitor.bpm.data

data class HeartBeatSample(
    var value: Float = 0f,
    var time: Long = 0L,
    var next: HeartBeatSample? = null,
    var isUsing: Boolean = false
) {
    companion object {
        private const val MAX_POOL_SIZE = 210
        private var heartBeatPool: HeartBeatSample? = null
        private var poolSize = 0

        @JvmStatic
        fun obtain(): HeartBeatSample {
            if (heartBeatPool != null) {
                val heartRate = heartBeatPool
                heartBeatPool = heartRate!!.next
                heartRate.next = null
                heartRate.isUsing = true
                poolSize--
                return heartRate
            }
            return HeartBeatSample()
        }
    }

    fun recycle() {
        value = 0f
        time = 0L
        isUsing = false
        if (poolSize < MAX_POOL_SIZE) {
            next = heartBeatPool
            heartBeatPool = this
            poolSize++
        }
    }
}
