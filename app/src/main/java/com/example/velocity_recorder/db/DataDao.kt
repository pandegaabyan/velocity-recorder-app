package com.example.velocity_recorder.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DataDao {
    // rides table

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addRide(ride: RideEntity): Long

    @Query("SELECT * FROM rides ORDER BY created_at DESC")
    fun getLiveRides(): LiveData<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE is_running ORDER BY created_at ASC LIMIT 1")
    suspend fun getRunningRide(): RideEntity?

    @Query("UPDATE rides SET end_time = :endTime, distance = :distance, max_velocity = :maxVelocity, is_running = :isRunning WHERE id = :id")
    suspend fun updateRide(
        id: Long,
        endTime: Long,
        distance: Int,
        maxVelocity: Double,
        isRunning: Boolean = true
    )

    @Query("UPDATE rides SET is_running = 0 WHERE is_running")
    suspend fun stopRunningRide()

    @Query("DELETE FROM rides WHERE id = :id")
    suspend fun deleteRide(id: Long)

    // velocities table

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addVelocities(velocities: List<VelocityEntity>)

    @Query("SELECT * FROM velocities WHERE ride_id = :rideId ORDER BY timestamp ASC")
    fun getLiveVelocities(rideId: Long): LiveData<List<VelocityEntity>>

    @Query("SELECT * FROM velocities WHERE ride_id = :rideId ORDER BY timestamp ASC")
    suspend fun getVelocities(rideId: Long): List<VelocityEntity>

    @Query("DELETE FROM velocities WHERE ride_id = :rideId")
    suspend fun deleteVelocities(rideId: Long)
}