package com.example.velocity_recorder.ui.home

import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter

class LineChartViewHolder(
    private val lineChart: LineChart
) {
    fun setupChart() {
        lineChart.description = Description().apply {
            text = "Velocity (km/h) vs Time (mm:ss)"
            textColor = Color.RED
            textSize = 16f
        }
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
            textColor = Color.BLUE
        }
        lineChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 60f
            setDrawGridLines(true)
            setDrawAxisLine(false)
            textSize = 12f
            textColor = Color.BLUE
        }
        lineChart.axisRight.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.setTouchEnabled(false)
    }
}