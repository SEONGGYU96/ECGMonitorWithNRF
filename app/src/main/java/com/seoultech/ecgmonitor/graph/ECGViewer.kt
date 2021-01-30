package com.seoultech.ecgmonitor.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View

/**
 * ECG Viewer
 * @author KIM SEONGYU
 * @since 2020/12/11
 */
class ECGViewer @JvmOverloads constructor(context: Context,
                                          attrs: AttributeSet? = null,
                                          defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "ECGView"
        private const val GRAPH_COLOR = "#C14D4D"
        private const val GRAPH_WIDTH = 2f
        private const val CURRENT_DOT_COLOR = "#C14D4D"
        private const val CURRENT_DOT_RADIUS = 10f
        private const val CURRENT_LINE_COLOR = "#150A7A"
        private const val CURRENT_LINE_WIDTH = 2f
        private const val HORIZONTAL_GRID_COLOR = "#C5C5C5"
        private const val VERTICAL_GRID_COLOR = "#F3F3F3"
        private const val GRID_WIDTH = 2f
        private const val NUMBER_OF_HORIZONTAL_GRID = 4

        private const val SECOND_PER_SCREEN = 10
        private const val INTERVAL_REFRESH_SECOND = 0.1f

        private const val INITIAL_AVERAGE_FOR_OFFSET = 82f
    }

    // Vertical center of this view. It plays the role of baseline of graph.
    private var verticalCenterOfView = 0

    private var sumOfValue = INITIAL_AVERAGE_FOR_OFFSET

    private var previousAverage = 0f

    // For graph line.
    private val graphPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor(GRAPH_COLOR)
        style = Paint.Style.STROKE
        strokeWidth = GRAPH_WIDTH
    }

    private val currentDotPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor(CURRENT_DOT_COLOR)
    }

    // For cursor (vertical) line.
    private val cursorLinePaint = Paint().apply {
        color = Color.parseColor(CURRENT_LINE_COLOR)
        style = Paint.Style.STROKE
        strokeWidth = CURRENT_LINE_WIDTH
    }

    // For horizontal grid line.
    private val horizontalGridPaint = Paint().apply {
        color = Color.parseColor(HORIZONTAL_GRID_COLOR)
        style = Paint.Style.STROKE
        strokeWidth = GRID_WIDTH
    }

    // For vertical line
    private val verticalGridPaint = Paint().apply {
        color = Color.parseColor(VERTICAL_GRID_COLOR)
        style = Paint.Style.STROKE
        strokeWidth = GRID_WIDTH
    }

    // For background.
    private val backgroundPaint = Paint().apply {
        color = Color.WHITE
    }

    // Lists for heart rate data. It will be used alternately.
    private val heartRateDataList1 = mutableListOf<HeartBeat>()
    private val heartRateDataList2 = mutableListOf<HeartBeat>()

    // Heart rate data of previous cycle. It will be placed to below of current heart rate data.
    // It will be either full or empty.
    private var previousHeartRateList = heartRateDataList1

    // Heart rate data of current cycle. So It may not be full.
    private var currentHeartRateList = heartRateDataList2

    // Thread is running or not.
    private var isRunning = false

    // Start time of current cycle.
    private var startTime: Long = -1

    // If there's more than one data in current interval, It will be true.
    private var isAdded = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        verticalCenterOfView = h / 2
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) {
            Log.e(TAG, "onDraw(): canvas is null")
            return
        }

        // Fill background.
        fillBackground(canvas)

        // Draw grids.
        drawGrid(canvas)

        // If thread is not running, return.
        if (!isRunning) {
            return
        }

        // Draw previous graph using heart rate data of last cycle. It starts from end of current graph.
        // But It may start from start of view.
        drawPreviousGraph(canvas)

        // If there is no heart rate data in current heart rate data, return.
        if (currentHeartRateList.isEmpty()) {
            return
        }

        // Draw current graph using heart rate data of current cycle. It starts from start of view.
        // But It may be draw to the end of view.
        drawCurrentGraph(canvas)
    }

    // Fill background.
    private fun fillBackground(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
    }

    // Draw grid.
    private fun drawGrid(canvas: Canvas) {
        // Horizontal grid
        canvas.run {
            // View height / number of horizontal grid = spacing of horizontal grid
            val unit = height / NUMBER_OF_HORIZONTAL_GRID.toFloat()
            for (i in 1 .. NUMBER_OF_HORIZONTAL_GRID) {
                drawLine(0f, i * unit, width.toFloat(), i * unit, horizontalGridPaint)
            }
        }

        // Vertical grid
        canvas.run {
            // view width / second per screen = spacing of vertical grid per a second
            val unit = width / SECOND_PER_SCREEN.toFloat()
            for (x in 1 until SECOND_PER_SCREEN) {
                drawLine(x * unit, 0f, x * unit, height.toFloat(), verticalGridPaint)
            }
        }
    }

    // Draw current graph using heart rate data of current cycle. It starts from start of view.
    // But It may be draw to the end of view.
    private fun drawCurrentGraph(canvas: Canvas) {
        drawGraph(canvas, startX = 0f, currentHeartRateList)
    }

    // Draw previous graph using heart rate data of last cycle. It starts from end of current graph.
    // But It may start from start of view when current heart rate list is empty.
    private fun drawPreviousGraph(canvas: Canvas) {
        val startX = if (currentHeartRateList.isEmpty()) {
            0f
        } else {
            currentHeartRateList.last().second
        }
        drawGraph(canvas, startX = convertToX(startX), previousHeartRateList)
    }


    // Draw graph using dataList from startX to end of data.
    private fun drawGraph(canvas: Canvas, startX: Float, dataList: MutableList<HeartBeat>) {
        // Init last x (time) and y (value of data) to start of view, center.
        var lastX = 0f
        var lastY = verticalCenterOfView.toFloat()

        for (heartRate in dataList) {
            // Convert time and data to graph unit.
            val currentX = convertToX(heartRate.second)
            val currentY = convertToY(heartRate.data)

            // If converted value of time is bigger than "startX", draw this value as a line.
            // Else It can not be drawn.
            if (currentX > startX) {
                canvas.drawLine(lastX, lastY, currentX, currentY, graphPaint)
            }
            // Change last x (time) and y to current x and y.
            lastX = currentX
            lastY = currentY
        }

        if (dataList.isNotEmpty()) {
            // Draw cursor at point means current time. It is vertical line.
            drawCursorLine(canvas, lastX)

            // Draw current dot at point means current value.
            drawCurrentDot(canvas, lastX, lastY)
        }
    }

    private fun drawCurrentDot(canvas: Canvas, centerX: Float, centerY: Float) {
        canvas.drawCircle(centerX, centerY, CURRENT_DOT_RADIUS, currentDotPaint)
    }

    // Draw cursor at point means current time. It is vertical line.
    private fun drawCursorLine(canvas: Canvas, x: Float) {
        canvas.drawLine(x, 0f, x, height.toFloat(), cursorLinePaint)
    }

    // Convert value of heart rate data to graph unit.
    private fun convertToY(data: Float): Float {
        return data * (-1) + verticalCenterOfView
    }

    // Convert value of time to graph unit.
    // It means that time will be scaled to match with width of view.
    private fun convertToX(second: Float): Float {
        return second * (width / SECOND_PER_SCREEN)
    }

    // Swap previous list and current list.
    // The list was current list will be previous graph because interval of this is finished.
    // The list was previous list will be current graph with clearing all data.
    // Because those data is not shown (used) at graph for being hidden by new data.
    private fun swapList() {
        previousAverage = sumOfValue / currentHeartRateList.size
        sumOfValue = 0f
        val temp = currentHeartRateList
        currentHeartRateList = previousHeartRateList
        previousHeartRateList = temp
        currentHeartRateList.clear()
    }

    // Request the system to update this view by calling invalidate().
    // And init isAdded to false. because new interval will be start.
    private fun refresh() {
        invalidate()
        isAdded = false
    }

    /**
     * Add a data of heart rate.
     * You must call start() before call this.
     * @param data Only one data received right now.
     */
    fun addValue(data: Float, time: Long) {
        // If a
        if (!isRunning) {
            Log.e(TAG, "addValue() : Drawing is not started. Did you call start()?")
            return
        }

        // Add this data to current heart rate list with current time (second)
        val currentSecond = (time - startTime) / 1000f
        currentHeartRateList.add(HeartBeat.obtain().apply {
            val convertedData = if (currentHeartRateList.isNotEmpty()) {
                data - (sumOfValue / currentHeartRateList.size)
            } else {
                data - previousAverage
            }
            this.data = convertedData
            this.second = currentSecond
        })
        sumOfValue += data
        if (previousHeartRateList.isNotEmpty() && currentSecond - previousHeartRateList.first().second > SECOND_PER_SCREEN) {
            previousHeartRateList.removeFirst().recycle()
        }

        // Mark that data is added in this interval.
        isAdded = true
    }

    /**
     * Start drawing the graph in real time.
     * If you call this, View wil count the second and draw graph using data you add.
     */
    fun start() {
        if (isRunning) {
            return
        }
        // Clear list before start.
        currentHeartRateList.clear()
        previousHeartRateList.clear()

        // Record current time.
        startTime = System.currentTimeMillis()
        // Mark that drawing is started.
        isRunning = true

        val handler = Handler(Looper.getMainLooper()) {
            // If there is no data in this interval, add a data with value 0 and current second.
            if (!isAdded) {
                currentHeartRateList.add(HeartBeat.obtain().apply {
                    this.data = 0f
                    this.second =  it.obj as Float
                })
            }
            // refresh graph
            refresh()
            false
        }

        // Run background thread.
        Thread {
            // Init second to 0
            var second = 0f

            while (isRunning) {
                // Sleep a sort time to collect data to buffer.
                Thread.sleep((INTERVAL_REFRESH_SECOND * 1000).toLong())
                second += INTERVAL_REFRESH_SECOND

                // After sleep, call handler with current second.
                // If second is bigger than the second per screen, init second and start time.
                // And swap previous list and current list.
                if (second > SECOND_PER_SCREEN.toFloat()) {
                    second = 0f
                    startTime = System.currentTimeMillis()
                    swapList()
                }
                handler.sendMessage(handler.obtainMessage(1, second))
            }
        }.apply {
            start()
        }
    }

    /**
     * Stop drawing the graph.
     * You must call this after using this view with start() or when application goes in background.
     * Please call this in onPause() or onStop()
     */
    fun stop() {
        isRunning = false
    }

    private data class HeartBeat(
        var data: Float = 0f,
        var second: Float = 0f,
        var next: HeartBeat? = null,
        var isUsing: Boolean = false
    ) {
        companion object {
            private const val MAX_POOL_SIZE = 10
            private var heartBeatPool: HeartBeat? = null
            private var poolSize = 0

            @JvmStatic
            fun obtain(): HeartBeat {
                if (heartBeatPool != null) {
                    val heartRate = heartBeatPool
                    heartBeatPool = heartRate!!.next
                    heartRate.next = null
                    heartRate.isUsing = true
                    poolSize--
                    return heartRate
                }
                return HeartBeat()
            }
        }

        fun recycle() {
            data = 0f
            second = 0f
            isUsing = false
            if (poolSize < MAX_POOL_SIZE) {
                next = heartBeatPool
                heartBeatPool = this
                poolSize++
            }
        }
    }
}