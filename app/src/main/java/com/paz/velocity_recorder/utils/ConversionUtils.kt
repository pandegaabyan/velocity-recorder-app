package com.paz.velocity_recorder.utils

import java.util.Locale

object ConversionUtils {

    private const val M_S_TO_KM_HR = 3.6
    private const val METER_TO_KM = 0.001

    fun getDistanceKm(meter: Double): String {
        return roundDouble(convertMeterToKm(meter)) + " km"
    }

    fun getVelocityKmHr(meterPerSecond: Double): String {
        return roundDouble(convertMeterSecToKmHr(meterPerSecond)) + " km/h"
    }

    private fun convertMeterToKm(meter: Double): Double {
        return if (meter > 10) {
            meter * METER_TO_KM
        } else 0.0
    }

    fun convertMeterSecToKmHr(meterPerSec: Double): Double {
        return meterPerSec * M_S_TO_KM_HR
    }

    private fun roundDouble(value: Double) = String.format(Locale.getDefault(), "%.2f", value)

}