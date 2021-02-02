package com.seoultech.ecgmonitor.heartbeat.heartrate

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * 심박수 계산 클래스
 * http://jkais99.org/journal/Vol18No12/vol18no12p18.pdf
 * 위 논문을 토대로 알고리즘을 작성함
 * Todo: 갱신을 3초마다 하고 있어서 사실상 정확한 BPM 이 아님. "이대로 1분을 유지한다면" 에 해당하는 값이기 때문에 추가적인 개선책이 필요함
 */
class HeartRateCalculatorImpl(private val heartRateLiveData: HeartRateLiveData) :
    HeartRateCalculator {

    companion object {
        private const val R_PEAK_DETECTION_CORE_THREAD_POOL = 30
        private const val PERIOD_REFRESH_EXPECTED_BPM_MILLI_SECOND = 3000L
        private const val PERIOD_REFRESH_BPM_MILLI_SECOND = 60000L
    }

    private val TAG = javaClass.simpleName

    private var sampleList = mutableListOf<Float>()

    private val calculateBPMThread : ScheduledThreadPoolExecutor by lazy {
        ScheduledThreadPoolExecutor(R_PEAK_DETECTION_CORE_THREAD_POOL).apply {
            removeOnCancelPolicy = true
        }
    }

    private var expectedBPMScheduledFuture : ScheduledFuture<*>? = null

    private var bpmScheduledFuture : ScheduledFuture<*>? = null

    private val calculateExpectedBPMRunnable : Runnable by lazy { Runnable { calculateExpectedBPM() } }

    private val calculateBPMRunnable: Runnable by lazy { Runnable { calculateBPM() } }

    private var addEcgDataThread : HandlerThread? = null

    private var addEcgDataHandler : Handler? = null

    private var isRunning = false

    private var maxDataOfCycle = Float.MIN_VALUE

    private var minDataOfCycle = Float.MAX_VALUE

    private var onBPMCalculatedListener: ((Int) -> Unit)? = null

    private var bpm = 0

    private fun addEcgDataToQueue(sample: Float) {
        maxDataOfCycle = maxDataOfCycle.coerceAtLeast(sample)
        minDataOfCycle = minDataOfCycle.coerceAtMost(sample)
        sampleList.add(sample)
    }

    private fun calculateExpectedBPM() {
        val tempMaxDataOfCycle = maxDataOfCycle
        val tempMinDataOfCycle = minDataOfCycle
        maxDataOfCycle = Float.MIN_VALUE
        minDataOfCycle = Float.MAX_VALUE
        val tempList = sampleList
        sampleList = mutableListOf()

        if (tempList.isEmpty()) {
            Log.d(TAG, "detectRPeakRunnable : sample list is empty yet")
            return
        }

        val thresholds = (tempMaxDataOfCycle + (tempMaxDataOfCycle + tempMinDataOfCycle) / 2) / 2
        //Log.d(TAG, "detectRPeakRunnable : thresholds = $thresholds")

        var countOfRPeakOfThisCycle = 0

        for (i in 0 until tempList.size) {
            if (i == 0 || i == tempList.size - 1) {
                //Todo: 현재 사이클의 마지막 샘플은 무시하도록 하였으나
                    // 해당 샘플이 R-Peak라면 오차가 발생하니 개선하여야 함
                continue
            }
            if (tempList[i] <= thresholds) {
                continue
            }
            if (tempList[i] <= tempList[i - 1] || tempList[i] < tempList[i + 1]) {
                continue
            }
            countOfRPeakOfThisCycle++
            bpm++
            //Log.d(TAG, "detectRPeakRunnable : R-Peak = ${tempList[i]}")
        }

        heartRateLiveData.setHeartRateValue((countOfRPeakOfThisCycle *
                (60000 / PERIOD_REFRESH_EXPECTED_BPM_MILLI_SECOND)).toInt())
    }

    private fun calculateBPM() {
        Log.d(TAG, "calculateBPM : $bpm")
        onBPMCalculatedListener?.let { it(bpm) }
        bpm = 0
    }

    override fun startCalculating() {
        if (isRunning) {
            Log.d(TAG, "startCalculating() : Calculating is already started")
            return
        }
        bpm = 0
        isRunning = true
        addEcgDataThread = HandlerThread("addEcgData")
        addEcgDataThread!!.start()
        addEcgDataHandler = Handler(addEcgDataThread!!.looper) {
            addEcgDataToQueue(it.obj as Float)
            return@Handler true
        }

        expectedBPMScheduledFuture = calculateBPMThread.scheduleWithFixedDelay(
            calculateExpectedBPMRunnable,
            PERIOD_REFRESH_EXPECTED_BPM_MILLI_SECOND,
            PERIOD_REFRESH_EXPECTED_BPM_MILLI_SECOND,
            TimeUnit.MILLISECONDS
        )

        bpmScheduledFuture = calculateBPMThread.scheduleWithFixedDelay(
            calculateBPMRunnable,
            PERIOD_REFRESH_BPM_MILLI_SECOND,
            PERIOD_REFRESH_BPM_MILLI_SECOND,
            TimeUnit.MILLISECONDS
        )
    }

    override fun stopCalculating() {
        if (!isRunning) {
            Log.d(TAG, "stopCalculating() : Calculating is already stopped")
        }
        isRunning = false
        addEcgDataThread?.quit()
        addEcgDataThread = null
        expectedBPMScheduledFuture?.cancel(false)
        bpmScheduledFuture?.cancel(false)
    }

    override fun addValue(value: Float) {
        addEcgDataHandler?.sendMessage(Message.obtain().apply { obj = value })
    }

    override fun setOnBPMCalculatedListener(listener: (Int) -> Unit) {
        this.onBPMCalculatedListener = listener
    }
}