package com.example.velocity_recorder.ui.chart

import com.example.velocity_recorder.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

class LineChartView(
    private val lineChart: LineChart
) {
    fun setupChart() {
        lineChart.clear()
        lineChart.setNoDataTextColor(R.color.purple_500)
        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            labelCount = 5
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val seconds = value / 1000
                    val minutes = seconds / 60
                    val remainingSeconds = seconds % 60
                    return "%02d:%02d".format(minutes.toInt(), remainingSeconds.toInt())
                }
            }
            textColor = R.color.purple_500
        }
        lineChart.axisLeft.apply {
            axisMinimum = 0f
            setDrawGridLines(true)
            setDrawAxisLine(false)
            textSize = 12f
            textColor = R.color.purple_500
        }
        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.setTouchEnabled(false)
    }

    fun setMaxLeftAxis(value: Float) {
        lineChart.axisLeft.axisMaximum = value * 1.2f
    }

    fun setData(entries: List<Entry>, label: String) {
        val dataSet = LineDataSet(entries, label)

        // Configure the appearance of the line curve
        dataSet.color = R.color.purple_500
        dataSet.setCircleColor(R.color.purple_500)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 1f
        dataSet.setDrawCircleHole(false)
        dataSet.setDrawValues(false)
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER

        // Update the line chart
        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }

    fun clear() {
        lineChart.clear()
    }
}