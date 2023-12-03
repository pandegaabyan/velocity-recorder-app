package com.paz.velocity_recorder.ui_model

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.paz.velocity_recorder.utils.ClockUtils
import com.paz.velocity_recorder.utils.ConversionUtils
import java.util.concurrent.TimeUnit

data class RideMapData(
    private val velocityDataList: List<VelocitySimpleItemData>
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
                .icon(BitmapDescriptorFactory.defaultMarker(265f))
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
                .icon(BitmapDescriptorFactory.defaultMarker(200f))
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

    fun getMapPolylineList(): List<Pair<PolylineOptions, MarkerOptions>> {
        val maxVelocity = velocityDataList.maxByOrNull { p -> p.velocity }?.velocity ?: 0.0

        val polylineList = mutableListOf<Pair<PolylineOptions, MarkerOptions>>()

        val velocitiesWithNext = velocityDataList.mapIndexed { index, entity ->
            VelocityNextItemData(
                timestamp = entity.timestamp,
                velocity = entity.velocity,
                latitude = entity.latitude,
                longitude = entity.longitude,
                nextLatitude = velocityDataList.getOrNull(index + 1)?.latitude,
                nextLongitude = velocityDataList.getOrNull(index + 1)?.longitude,
            )
        }

        velocitiesWithNext.forEach {
            val velocityColor = getColorBasedOnVelocity(maxVelocity, it.velocity)

            val polylineOptions = PolylineOptions()
            polylineOptions.width(10f)
            polylineOptions.startCap(RoundCap())
            polylineOptions.endCap(RoundCap())
            polylineOptions.add(LatLng(it.latitude, it.longitude))
            if (it.nextLatitude != null && it.nextLongitude != null) {
                polylineOptions.add(
                    LatLng(
                        it.nextLatitude,
                        it.nextLongitude
                    )
                )
            }
            polylineOptions.geodesic(false)
            polylineOptions.color(velocityColor.first)

            val markerOptions = MarkerOptions()
                .position(LatLng(it.latitude, it.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(velocityColor.second))
                .title(getRelativeTime(it.timestamp))
                .snippet(ConversionUtils.getVelocityKmHr(it.velocity))

            polylineList.add(Pair(polylineOptions, markerOptions))
        }

        return polylineList
    }

    private fun getColorBasedOnVelocity(
        maxVelocity: Double,
        velocity: Double
    ): Pair<Int, Float> {
        val percentage =
            if (maxVelocity > 0) (velocity / maxVelocity) * 100 else 100.0
        return when {
            percentage < 33 -> {
                Pair(0xffff2b00.toInt(), 10f)
            }

            percentage < 67 -> {
                Pair(0xffffaa00.toInt(), 40f)
            }

            else -> {
                Pair(0xff00aaff.toInt(), 200f)
            }
        }
    }

    companion object {
        fun empty(): RideMapData = RideMapData(
            velocityDataList = emptyList()
        )
    }
}