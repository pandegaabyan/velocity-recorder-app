package com.paz.velocity_recorder.components

data class LocationInitData(
    val rideId: Long?,
    val startTime: Long,
    val distance: Double,
    val maxVelocity: Double,
    val lastLatitude: Double,
    val lastLongitude: Double
)
