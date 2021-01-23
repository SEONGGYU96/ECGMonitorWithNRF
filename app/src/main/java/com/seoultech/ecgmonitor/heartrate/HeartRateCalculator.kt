package com.seoultech.ecgmonitor.heartrate

import com.seoultech.ecgmonitor.graph.HeartRate
import java.util.*

class HeartRateCalculator(private val heartRateLiveData: HeartRateLiveData) : HeartRateCalculable, Thread() {

    companion object {
        private const val TAG = "HeartRateCalculator"
        private const val THRESHOLD_COEFFICIENT = 0.5f
        private const val REFRESH_PERIOD_MILLI_SECOND = 1000L
    }

    private var isRunning = false

    private val queue = LinkedList<HeartRate>()

    private var averageOfValue = 0f
    private var threshold = 0f

    private fun addValueToAverage(value: Float) {
        averageOfValue = (averageOfValue + value) / 2
    }

    private fun calculateThreshold() {
        threshold = averageOfValue * THRESHOLD_COEFFICIENT
    }

    override fun startCalculating() {
        start()
    }

    override fun start() {
        isRunning = true
        super.start()
    }

    override fun stopCalculating() {
        isRunning = false
        queue.clear()
        averageOfValue = 0f
        threshold = 0f
    }

    override fun addValue(value: HeartRate) {
        if (threshold < value.data) {
            queue.addLast(value)
        } else {
            value.recycle()
        }
        addValueToAverage(value.data)
        calculateThreshold()
    }

    override fun run() {
        while (isRunning) {
            heartRateLiveData.setHeartRateValue(queue.size)
            val currentTime = System.currentTimeMillis()
            while (queue.peekFirst() != null && queue.peekFirst()!!.time < currentTime) {
                queue.pollFirst()!!.recycle()
            }
            sleep(REFRESH_PERIOD_MILLI_SECOND)
        }
    }
}