package com.paz.velocity_recorder.ui_model

import com.paz.velocity_recorder.utils.ClockUtils
import com.paz.velocity_recorder.utils.ConversionUtils
import java.util.concurrent.TimeUnit

data class RideItemData(
    private val rideId: Long,
    private val startLocality: String?,
    private val endLocality: String?,
    private val maxVelocity: Double,
    private val distance: Int,
    private val startTime: Long,
    private val endTime: Long,
    private val isRunning: Boolean
) {

    private fun getDuration() = TimeUnit.MILLISECONDS.toSeconds(endTime - startTime)

    fun isRunning(): Boolean {
        return isRunning
    }

    fun isLocalityNull(): Boolean {
        return startLocality == null || endLocality == null
    }

    fun getStartTime(): Long {
        return startTime
    }

    fun getEndTime(): Long {
        return endTime
    }

    fun getStartText(): String {
        if (startLocality != null && endLocality != null) {
            return startLocality
        }
        return ClockUtils.convertLongToString(startTime)
    }

    fun getEndText(): String {
        if (startLocality != null && endLocality != null) {
            return endLocality
        }
        return ClockUtils.convertLongToString(endTime)
    }

    fun getStartEndText(): String {
        return "${getStartText()} - ${getEndText()}"
    }

    fun getTotalTime() = ClockUtils.getTime(getDuration())

    fun getTimeText() = ClockUtils.getRelativeTime(endTime)

    fun getRideId() = rideId

    fun getTotalDistance() = ConversionUtils.getDistanceKm(distance.toDouble())

    fun getMaxVelocity() = ConversionUtils.getVelocityKmHr(maxVelocity)

    fun getMaxVelocityNumber() = maxVelocity

    fun getAvgVelocity() =
        ConversionUtils.getVelocityKmHr(distance.toDouble() / getDuration())

}