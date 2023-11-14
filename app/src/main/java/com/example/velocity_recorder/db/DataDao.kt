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
    @Insert
    fun addRide(ride: RideEntity)

    @Query("SELECT * FROM rides ORDER BY created_at DESC")
    fun getRides(): LiveData<List<RideEntity>>

    @Query("DELETE FROM rides WHERE id = :id")
    suspend fun deleteRide(id: Long)
}