package com.paz.velocity_recorder.ui_model

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.paz.velocity_recorder.utils.ConversionUtils
import com.paz.velocity_recorder.utils.SphericalUtils

data class RideMapData(
    private val velocityDataList: List<VelocitySimpleItemData>,
    private val polylineOptionList: List<PolylineOptions>
) {

    fun getStartPointMarker(): MarkerOptions? {
        return velocityDataList.firstOrNull()?.let {
            MarkerOptions()
                .position(LatLng(it.latitude, it.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(201f))
                .title("Start")
        }
    }

    fun getEndPointMarker(): MarkerOptions? {
        return velocityDataList.lastOrNull()?.let {
            MarkerOptions()
                .position(LatLng(it.latitude, it.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(17f))
                .title("End")
        }
    }

    fun getMaxVelocityPointMarker(): MarkerOptions? {
        return velocityDataList.maxByOrNull {
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
        val startEntity = velocityDataList.firstOrNull()
        val endEntity = velocityDataList.lastOrNull()
        if (startEntity != null && endEntity != null) {
            return LatLngBounds.Builder()
                .include(LatLng(startEntity.latitude, startEntity.longitude))
                .include(LatLng(endEntity.latitude, endEntity.longitude))
                .build()
        }

        return null
    }

    fun getLatLngToZoom(): LatLng? {
        val startEntity = velocityDataList.firstOrNull()
        if (startEntity != null) {
            return LatLng(startEntity.latitude, startEntity.longitude)
        }

        return null
    }

    fun getHeading(): Double {
        val startEntity = velocityDataList.firstOrNull()
        val endEntity = velocityDataList.lastOrNull()
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
            velocityDataList = emptyList(),
            polylineOptionList = emptyList()
        )
    }
}