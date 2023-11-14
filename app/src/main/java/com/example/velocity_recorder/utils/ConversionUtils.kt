package com.example.velocity_recorder.utils

import java.util.Locale

private const val KM_MULT = 3.6
private const val SECONDS_HOUR_MULT = 0.000277778
private const val METER_KM = 0.001

object ConversionUtils {

    fun getDistanceKm(meter: Double): String {
        return roundDouble(convertMeterToKm(meter)) + " km"
    }

    fun getVelocityKmHr(meterPerSecond: Double): String {
        return roundDouble(convertMeterSecToKmHr(meterPerSecond)) + " km/h"
    }

    fun convertSecToHour(sec: Double): Double {
        return if (sec > 0.2) {
            sec * SECONDS_HOUR_MULT
        } else 0.0
    }

    private fun convertMeterToKm(meter: Double): Double {
        return if (meter > 10) {
            meter * METER_KM
        } else 0.0
    }

    fun convertMeterSecToKmHr(meterPerSec: Double): Double {
        return meterPerSec * KM_MULT
    }

    fun convertKmHrToMeterSec(kmPerHour: Double): Double {
        return kmPerHour / KM_MULT
    }

    private fun roundDouble(value: Double) = String.format(Locale.getDefault(), "%.2f", value)

}