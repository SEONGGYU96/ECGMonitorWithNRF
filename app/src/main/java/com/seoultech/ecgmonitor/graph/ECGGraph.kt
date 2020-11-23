package com.seoultech.ecgmonitor.graph

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.seoultech.ecgmonitor.R

class ECGGraph @JvmOverloads constructor(context: Context,
                                         attrs: AttributeSet? = null,
                                         defStyleAttr: Int = 0)
    : LineChart(context, attrs, defStyleAttr) {

    companion object {
        private const val MAX = 50F
    }

    init {
        isDragEnabled = false
        description = null
        setBackgroundColor(context.getColor(R.color.colorMonitorBackground))
        setScaleEnabled(false)
        setTouchEnabled(false)

        xAxis.run {
            isEnabled = true
            position = XAxis.XAxisPosition.BOTTOM_INSIDE
            axisLineColor = Color.BLACK
            setDrawAxisLine(true)
            setDrawGridLines(false)
            //set right padding of viewport
            //for put a last vertex on center of viewport
            spaceMax = MAX / 2
        }

        axisLeft.run {
            isEnabled = true
            textColor = Color.BLACK
            axisLineColor = Color.BLACK
            labelCount = 0
            setDrawGridLines(false)
            setDrawAxisLine(true)
        }

        axisRight.isEnabled = false
    }

    fun addValue(num: Double) {
        var data: LineData? = this.data
        if (data == null) {
            data = LineData()
            this.data = data
        }

        var set = data.getDataSetByIndex(0)
        // set.addEntry(...); // can be called as well
        if (set == null) {
            set = createSet()
            data.addDataSet(set)
        }

        data.run {
            //add random entry
            addEntry(Entry(set.entryCount.toFloat(), num.toFloat()), 0)
            notifyDataChanged()
        }

        refreshChart()
    }

    private fun refreshChart() {
        // let the chart know it's data has changed
        notifyDataSetChanged()
        setVisibleXRangeMaximum(MAX)
        setVisibleXRangeMinimum(MAX)
        // this automatically refreshes the chart (calls invalidate())
        moveViewToX(data.entryCount.toFloat())

    }

    private fun createSet(): LineDataSet {
        //init data set
        return LineDataSet(null, "Real-time Electrocardiogram").apply {
            //customize appearance of line
            color = Color.BLACK
            label = null
            setDrawCircles(false)
            setDrawValues(false)
        }
    }
}