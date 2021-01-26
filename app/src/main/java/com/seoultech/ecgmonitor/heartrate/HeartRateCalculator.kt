package com.seoultech.ecgmonitor.heartrate

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.seoultech.ecgmonitor.graph.HeartRate
import java.util.*

class HeartRateCalculator(
    private val heartRateLiveData: HeartRateLiveData
) : HeartRateCalculable, Runnable {

    companion object {
        private const val TAG = "HeartRateCalculator"
        private const val THRESHOLD_COEFFICIENT = 0.5f
        private const val REFRESH_PERIOD_MILLI_SECOND = 3000L
        private const val VALID_DATA_TIME_RANGE_MILLI_SECOND = 1000L
    }

    private var isRunning = false

    private val queue = LinkedList<HeartRate>()

    private var averageOfValue = 0f
    private var threshold = 0f

    private var handlerThread : HandlerThread? = null

    private fun addValueToAverage(value: Float) {
        averageOfValue = (averageOfValue + value) / 2
    }

    private fun calculateThreshold() {
        threshold = averageOfValue * THRESHOLD_COEFFICIENT
    }

    override fun startCalculating() {
        if (!isRunning) {
            isRunning = true
            handlerThread = HandlerThread("heartRateCalculating")
            handlerThread!!.start()
            Handler(handlerThread!!.looper).post(this)
        }
    }

    override fun stopCalculating() {
        isRunning = false
        handlerThread?.quit()
        handlerThread = null
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
            var count = 0
            val currentTime = System.currentTimeMillis()
            while (queue.peekFirst() != null &&
                currentTime - queue.peekFirst()!!.time > VALID_DATA_TIME_RANGE_MILLI_SECOND) {
                queue.pollFirst()!!.recycle()
                count++
            }
            heartRateLiveData.setHeartRateValue(queue.size)
            Thread.sleep(REFRESH_PERIOD_MILLI_SECOND)
        }
    }
}