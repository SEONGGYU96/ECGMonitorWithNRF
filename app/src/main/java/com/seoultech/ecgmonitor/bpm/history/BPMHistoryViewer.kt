package com.seoultech.ecgmonitor.bpm.history

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorRes
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.bpm.data.BPM
import com.seoultech.ecgmonitor.utils.TimeUtil
import java.util.*
import kotlin.math.max
import kotlin.math.min

class BPMHistoryViewer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "BPMHistoryViewer"
        private const val GRAPH_WIDTH_DP = 1
        private const val DOT_RADIUS = 6f
        private const val NUMBER_OF_HORIZONTAL_DATA = 1440 //하루 1,440 분
        private const val GAP_OF_DATA_DP = 5
        private const val PADDING_VERTICAL_DP = 20
        private const val PADDING_HORIZONTAL_DP = 16
        private const val TEXT_SIZE = 30f
        private const val TIME_TEXT_FORMAT = "%s %d시"
        private const val TIME_TEXT_AM = "오전"
        private const val TIME_TEXT_PM = "오후"
    }

    private var measuredVerticalSize = 0f
    private var measuredHorizontalSize = 0f
    private var verticalDataGapUnit = 0f
    private var graphAreaHeight = 0f

    private var bpmData = mutableListOf<BPM>()
    private var maxBPM = 0
    private var minBPM = 0
    private var averageOfBPM = 0
    private var maxAndMinBPMIsInitialized = false
    private var measuredSizeIsInitialized = false
    private var dataGapIsInitialized = false

    private val paddingVerticalPx = PADDING_VERTICAL_DP.px
    private val paddingHorizontalPx = PADDING_HORIZONTAL_DP.px
    private val horizontalGapOfDataPx = GAP_OF_DATA_DP.px
    private val graphWidthPx = GRAPH_WIDTH_DP.px

    private fun getTextHeight(): Int {
        val rect = Rect()
        textPaint.getTextBounds("후", 0, 1, rect)
        return rect.height()
    }

    private var tempCalendar = GregorianCalendar()

    private val graphPaint = getStrokePaint(R.color.colorPrimary, graphWidthPx, true)

    private val gridPaint = getStrokePaint(R.color.colorGray, graphWidthPx, false)

    private val circlePaint = getPaint(R.color.colorPrimary, true)

    private val textPaint = getPaint(R.color.colorGray, true).apply {
        textSize = TEXT_SIZE
        textAlign = Paint.Align.CENTER
    }

    private val measuredTextHeight = getTextHeight()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        dataGapIsInitialized = false
        measuredHorizontalSize = w.toFloat()
        measuredVerticalSize = h.toFloat()
        measuredSizeIsInitialized = true
        graphAreaHeight = measuredVerticalSize - paddingVerticalPx - measuredTextHeight
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            (NUMBER_OF_HORIZONTAL_DATA * horizontalGapOfDataPx + paddingHorizontalPx * 2).toInt(),
            getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        )
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

        drawTopAndBottomGrid(canvas)
        drawVerticalGridAndTime(canvas)
        drawBPMGraph(canvas)
    }

    private fun getStrokePaint(@ColorRes colorRes: Int, width: Float, isAntiAlias: Boolean): Paint {
        return getPaint(colorRes, isAntiAlias).apply {
            style = Paint.Style.STROKE
            strokeWidth = width
        }
    }

    private fun getPaint(@ColorRes colorRes: Int, isAntiAlias: Boolean): Paint {
        val paint = if (isAntiAlias) {
            Paint(ANTI_ALIAS_FLAG)
        } else {
            Paint()
        }
        return paint.apply {
            this.color = context.getColor(colorRes)
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
        initMaxAndMinAndAverageBPM()
        initVerticalDataGapUnit()
        invalidate()
    }

    private fun initVerticalDataGapUnit() {
        verticalDataGapUnit =
            (measuredVerticalSize - paddingVerticalPx * 2 - measuredTextHeight) / (maxBPM - minBPM)
        dataGapIsInitialized = true
    }

    private fun initMaxAndMinAndAverageBPM() {
        for (bpm in bpmData) {
            if (!maxAndMinBPMIsInitialized) {
                maxBPM = bpm.bpm
                minBPM = bpm.bpm
                maxAndMinBPMIsInitialized = true
            } else {
                maxBPM = max(maxBPM, bpm.bpm)
                minBPM = min(minBPM, bpm.bpm)
            }
            averageOfBPM += bpm.bpm
        }
        averageOfBPM /= bpmData.size
    }

    private fun drawTopAndBottomGrid(canvas: Canvas) {
        drawHorizontalGrid(canvas, 0f)
        drawHorizontalGrid(canvas, measuredVerticalSize - measuredTextHeight)
    }

    private fun drawVerticalGridAndTime(canvas: Canvas) {
        for (hour in 0..23) {
            val currentX = hour * horizontalGapOfDataPx * 60 + paddingHorizontalPx
            drawVerticalGrid(canvas, currentX)
            drawTimeText(canvas, hour, currentX)
        }
    }

    private fun drawTimeText(canvas: Canvas, hour: Int, x: Float) {
        val amPmText = if (hour < 12) {
            TIME_TEXT_AM
        } else {
            TIME_TEXT_PM
        }
        val time = if (hour <= 12) {
            hour
        } else {
            hour - 12
        }
        val text = String.format(TIME_TEXT_FORMAT, amPmText, time)
        canvas.drawText(text, x, measuredVerticalSize, textPaint)
    }

    private fun drawVerticalGrid(canvas: Canvas, x: Float) {
        canvas.drawLine(
            x,
            0f,
            x,
            measuredVerticalSize - measuredTextHeight,
            gridPaint
        )
    }

    private fun drawBPMGraph(canvas: Canvas) {
        tempCalendar.timeInMillis = bpmData[0].time
        TimeUtil.initCalendarBelowDay(tempCalendar)
        val startMillis = tempCalendar.timeInMillis
        var lastX = -1f
        var lastY = -1f

        for (bpm in bpmData) {
            val currentX = getXPosition(bpm.time, startMillis)
            val currentY = getYPosition(bpm.bpm)

            if (lastX == -1f || lastY == -1f || currentX - lastX > horizontalGapOfDataPx) {
                canvas.drawCircle(lastX, lastY, DOT_RADIUS, circlePaint)
                canvas.drawCircle(currentX, currentY, DOT_RADIUS, circlePaint)
            } else {
                canvas.drawLine(lastX, lastY, currentX, currentY, graphPaint)
            }
            lastX = currentX
            lastY = currentY
        }
    }

    private fun getXPosition(time: Long, startMillis: Long): Float {
        val minute = TimeUtil.getMinuteDiff(time, startMillis)
        return minute * horizontalGapOfDataPx + paddingHorizontalPx
    }

    private fun getYPosition(bpm: Int): Float {
        return graphAreaHeight - ((bpm - minBPM) * verticalDataGapUnit)
    }

    private fun drawHorizontalGrid(canvas: Canvas, y: Float) {
        canvas.drawLine(0f, y, measuredHorizontalSize, y, gridPaint)
    }
}

val Float.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Float
    get() = this * Resources.getSystem().displayMetrics.density