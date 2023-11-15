package com.example.velocity_recorder.ui_model

import com.example.velocity_recorder.db.VelocityEntity

data class VelocitySimpleListData(
    val velocities: MutableList<VelocitySimpleItemData>
) {
    fun add(velocity: VelocitySimpleItemData) {
        velocities.add(velocity)
    }

    fun clear() {
        velocities.clear()
    }

    fun getVelocityEntities(rideId: Long): List<VelocityEntity> {
        return velocities.map {
            VelocityEntity(
                rideId = rideId,
                timestamp = it.timestamp,
                latitude = it.latitude,
                longitude = it.longitude,
                velocity = it.velocity
            )
        }
    }
}