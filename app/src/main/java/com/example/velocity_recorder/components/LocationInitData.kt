package com.example.velocity_recorder.components

data class LocationInitData(
    val rideId: Long?,
    val startTime: Long,
    val maxVelocity: Double,
    val startLatitude: Double,
    val startLongitude: Double
)
