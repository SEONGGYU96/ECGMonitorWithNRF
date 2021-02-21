package com.seoultech.ecgmonitor.bpm.history

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.bpm.data.BPM
import com.seoultech.ecgmonitor.utils.TimeUtil
import java.util.*

class BPMHistoryViewer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "BPMHistoryViewer"
        private const val GRAPH_WIDTH = 2f
        private const val DOT_RADIUS = 6f
        private const val NUMBER_OF_HORIZONTAL_DATA = 20 //하루 1,440 분
    }

    private var measuredVerticalSize = 0f
    private var measuredHorizontalSize = 0f
    private var verticalDataGapUnit = 0f
    private var horizontalDataGapUnit = 0f

    private var bpmData = mutableListOf<BPM>()
    private var maxBPM = 0
    private var minBPM = 0
    private var maxAndMinBPMIsInitialized = false
    private var measuredSizeIsInitialized = false
    private var dataGapIsInitialized = false

    private var tempCalendar = GregorianCalendar()

    private val graphPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.colorPrimary)
        style = Paint.Style.STROKE
        strokeWidth = GRAPH_WIDTH
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        dataGapIsInitialized = false
        measuredHorizontalSize = w.toFloat()
        measuredVerticalSize = h.toFloat()
        measuredSizeIsInitialized = true
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) {
            Log.e(TAG, "onDraw(): canvas is null")
            return
        }
        if (!maxAndMinBPMIsInitialized || !dataGapIsInitialized || !measuredSizeIsInitialized) {
            Log.e(TAG, "onDraw(): values is not initialized yet")

            return
        }

        if (bpmData.isEmpty()) {
            canvas.drawColor(Color.WHITE)
            return
        }

        tempCalendar.timeInMillis = bpmData[0].time
//        TimeUtil.initCalendarBelowDay(tempCalendar)
        //Todo: 시작점을 그 날의 00시 00분으로 맞춰야함 지금은 디버깅용
        val startMillis = tempCalendar.timeInMillis
        var lastX = -1f
        var lastY = -1f

        for (bpm in bpmData) {
            val minute = TimeUtil.getMinuteDiff(bpm.time, startMillis)
            Log.d(TAG, "minute : $minute, bpm : $bpm, horizontalUnit: $horizontalDataGapUnit")
            val currentX = minute * horizontalDataGapUnit
            val currentY = measuredHorizontalSize - ((bpm.bpm - minBPM) * verticalDataGapUnit)

            //Todo: 데이터가 떨어져있을 경우 원 그리도록
            if (lastX == -1f || lastY == -1f) {
                lastX = currentX
                lastY = currentY
                canvas.drawCircle(currentX, currentY, DOT_RADIUS, graphPaint)
                Log.d(TAG, "drawCircle : $currentX, $currentY")
            } else {
                canvas.drawLine(lastX, lastY, currentX, currentY, graphPaint)
                lastX = currentX
                lastY = currentY
                Log.d(TAG, "drawLine : $lastX, $lastY -> $currentX, $currentY")
            }
        }
    }

    fun setBPMData(data: List<BPM>) {
        if (data.isEmpty()) {
            Log.d(TAG, "setBPMData: empty data")
            bpmData.clear()
            invalidate()
            return
        }
        maxAndMinBPMIsInitialized = false
        bpmData.clear()
        bpmData.addAll(data)
        initMaxAndMinData()
        initVerticalDataGapUnit()
        invalidate()
    }

    private fun initVerticalDataGapUnit() {
        horizontalDataGapUnit = measuredHorizontalSize / NUMBER_OF_HORIZONTAL_DATA
        verticalDataGapUnit = measuredVerticalSize / (maxBPM - minBPM)
        dataGapIsInitialized = true
    }

    private fun initMaxAndMinData() {
        for (bpm in bpmData) {
            if (!maxAndMinBPMIsInitialized) {
                maxBPM = bpm.bpm
                minBPM = bpm.bpm
                maxAndMinBPMIsInitialized = true
            } else {
                maxBPM = maxBPM.coerceAtLeast(bpm.bpm)
                minBPM = minBPM.coerceAtMost(bpm.bpm)
            }
        }
    }
}