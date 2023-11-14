package com.example.velocity_recorder.ui_model

import com.example.velocity_recorder.utils.ClockUtils
import com.example.velocity_recorder.utils.ConversionUtils
import java.util.concurrent.TimeUnit

data class RideItemData(
    private val rideId: Long,
    private val startLocality: String?,
    private val endLocality: String?,
    private val avgVelocity: Double,
    private val distance: Int,
    private val startTime: Long,
    private val endTime: Long
) {

    fun getStartEndText(): String {
        if (startLocality != null && endLocality != null) {
            return "$startLocality - $endLocality"
        }
        val startTimeStr = ClockUtils.convertLongToString(startTime)
        val endTimeStr = ClockUtils.convertLongToString(endTime)
        return "$startTimeStr - $endTimeStr"
    }

    fun getAvgVelocity() = ConversionUtils.getVelocityKmHr(avgVelocity)

    fun getTotalTime() =
        ClockUtils.getTime(TimeUnit.MILLISECONDS.toSeconds(endTime - startTime))

    fun getTimeText() = ClockUtils.getRelativeTime(endTime)

    fun getRideId() = rideId

    fun getTotalDistance() = ConversionUtils.getDistanceKm(distance.toDouble())

}