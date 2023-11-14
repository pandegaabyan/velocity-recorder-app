package com.example.velocity_recorder.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rides")
data class RideEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    @ColumnInfo(name = "start_locality") val startLocality: String?,
    @ColumnInfo(name = "end_locality") val endLocality: String?,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "end_time") val endTime: Long,
    @ColumnInfo(name = "distance") val distance: Int,
    @ColumnInfo(name = "avg_velocity") val avgVelocity: Double,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "velocities")
data class VelocityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "ride_id") val rideId: Long,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "velocity") val velocity: Float,
)