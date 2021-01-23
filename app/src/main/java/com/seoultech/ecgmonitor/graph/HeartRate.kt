package com.seoultech.ecgmonitor.graph

data class HeartRate(
    var data: Float = 0f,
    var time: Long = 0L,
    var next: HeartRate? = null,
    var isUsing: Boolean = false
) {
    companion object {
        private const val MAX_POOL_SIZE = 30
        private var heartRatePool: HeartRate? = null
        private var poolSize = 0

        @JvmStatic
        fun obtain(): HeartRate {
            if (heartRatePool != null) {
                val heartRate = heartRatePool
                heartRatePool = heartRate!!.next
                heartRate.next = null
                heartRate.isUsing = true
                poolSize--
                return heartRate
            }
            return HeartRate()
        }
    }

    fun recycle() {
        data = 0f
        time = 0L
        isUsing = false
        if (poolSize < MAX_POOL_SIZE) {
            next = heartRatePool
            heartRatePool = this
            poolSize++
        }
    }
}