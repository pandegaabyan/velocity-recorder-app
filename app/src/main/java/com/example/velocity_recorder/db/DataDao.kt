package com.example.velocity_recorder.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DataDao {
    // rides table

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addRide(ride: RideEntity): Long

    @Query("SELECT * FROM rides ORDER BY created_at DESC")
    fun getRides(): LiveData<List<RideEntity>>

    @Query("UPDATE rides SET end_time = :endTime, distance = :distance, max_velocity = :maxVelocity, avg_velocity = :avgVelocity WHERE id = :id")
    suspend fun updateRide(id: Long, endTime: Long, distance: Int, maxVelocity: Double, avgVelocity: Double)

    @Query("DELETE FROM rides WHERE id = :id")
    suspend fun deleteRide(id: Long)

    // velocities table

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addVelocities(velocities: List<VelocityEntity>)

    @Query("DELETE FROM velocities WHERE ride_id = :rideId")
    suspend fun deleteVelocities(rideId: Long)
}