package com.paz.velocity_recorder.ui_model

import com.google.android.gms.maps.model.*
import com.paz.velocity_recorder.utils.ConversionUtils
import com.paz.velocity_recorder.utils.SphericalUtils

data class RideMapData(
    private val velocities: List<VelocitySimpleItemData>,
    private val polylineOptionList: List<PolylineOptions>
) {

    fun getStartPointMarker(): MarkerOptions? {
        return velocities.firstOrNull()?.let {
            MarkerOptions()
                .position(LatLng(it.latitude, it.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(201f))
                .title("Start")
        }
    }

    fun getEndPointMarker(): MarkerOptions? {
        return velocities.lastOrNull()?.let {
            MarkerOptions()
                .position(LatLng(it.latitude, it.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(17f))
                .title("End")
        }
    }

    fun getMaxVelocityPointMarker(): MarkerOptions? {
        return velocities.maxByOrNull {
            it.velocity
        }?.let {
            val maxVelocityString = "Max velocity: ${ConversionUtils.getVelocityKmHr(it.velocity)}"
            MarkerOptions()
                .position(LatLng(it.latitude, it.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(49f))
                .title(maxVelocityString)
        }
    }

    fun getLatLngBounds(): LatLngBounds? {
        val startEntity = velocities.firstOrNull()
        val endEntity = velocities.lastOrNull()
        if (startEntity != null && endEntity != null) {
            return LatLngBounds.Builder()
                .include(LatLng(startEntity.latitude, startEntity.longitude))
                .include(LatLng(endEntity.latitude, endEntity.longitude)).build()
        }

        return null
    }

    fun getLatLngToZoom(): LatLng? {
        val startEntity = velocities.firstOrNull()
        if (startEntity != null) {
            return LatLng(startEntity.latitude, startEntity.longitude)
        }

        return null
    }

    fun getHeading(): Double {
        val startEntity = velocities.firstOrNull()
        val endEntity = velocities.lastOrNull()
        if (startEntity != null && endEntity != null) {
            return SphericalUtils.computeHeading(
                LatLng(startEntity.latitude, startEntity.longitude),
                LatLng(endEntity.latitude, endEntity.longitude)
            ) - 30
        }

        return 0.0
    }


    fun getMapPolyLineOptionList(): List<PolylineOptions> = polylineOptionList

    companion object {
        fun empty(): RideMapData = RideMapData(
            velocities = emptyList(),
            polylineOptionList = emptyList()
        )
    }
}