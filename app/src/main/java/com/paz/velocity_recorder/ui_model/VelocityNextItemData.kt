package com.paz.velocity_recorder.ui_model

data class VelocityNextItemData(
    val timestamp: Long,
    val velocity: Double,
    val latitude: Double,
    val longitude: Double,
    val nextLatitude: Double?,
    val nextLongitude: Double?,
)
