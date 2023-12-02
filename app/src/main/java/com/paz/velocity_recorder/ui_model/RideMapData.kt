package com.paz.velocity_recorder.ui_model

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.paz.velocity_recorder.utils.ClockUtils
import com.paz.velocity_recorder.utils.ConversionUtils
import java.util.concurrent.TimeUnit

data class RideMapData(
    private val velocityDataList: List<VelocitySimpleItemData>,
    private val polylineOptionList: List<PolylineOptions>
) {
    fun getRelativeTime(timestamp: Long): String {
        val firstTimestamp = velocityDataList.firstOrNull()?.timestamp ?: 0
        return ClockUtils.getTime(
            TimeUnit.MILLISECONDS.toSeconds(timestamp - firstTimestamp)
        )
    }

    fun getStartPointMarker(): MarkerOptions? {
        return velocityDataList.firstOrNull()?.let {
            MarkerOptions()
                .position(LatLng(it.latitude, it.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(240f))
                .title("00:00")
                .snippet("Start of Ride")
        }
    }

    fun getEndPointMarker(): MarkerOptions? {
        return velocityDataList.lastOrNull()?.let {
            MarkerOptions()
                .position(LatLng(it.latitude, it.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(290f))
                .title(getRelativeTime(it.timestamp))
                .snippet("End of Ride")
        }
    }

    fun getMaxVelocityPointMarker(): MarkerOptions? {
        return velocityDataList.maxByOrNull {
            it.velocity
        }?.let {
            MarkerOptions()
                .position(LatLng(it.latitude, it.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(265f))
                .title(getRelativeTime(it.timestamp))
                .snippet("Max: ${ConversionUtils.getVelocityKmHr(it.velocity)}")
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

    fun getMapPolyLineOptionList(): List<PolylineOptions> = polylineOptionList

    companion object {
        fun empty(): RideMapData = RideMapData(
            velocityDataList = emptyList(),
            polylineOptionList = emptyList()
        )
    }
}