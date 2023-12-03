package com.paz.velocity_recorder.ui.ride_detail

import android.util.Log
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.paz.velocity_recorder.ui_model.RideMapData

class GoogleMapHandler(private val googleMap: GoogleMap) {
    private var mapDefaultCamera: CameraUpdate? = null
    private var mapActiveMarker: Marker? = null
    private var isMapHybrid = false

    fun setupMap(rideMapData: RideMapData) {
        googleMap.clear()

        // plot polylines
        try {
            rideMapData.getMapPolylineList().forEach { pair ->
                val marker = googleMap.addMarker(pair.second)
                marker?.isVisible = false
                val polyline = googleMap.addPolyline(pair.first)
                polyline.isClickable = true
                polyline.tag = marker
            }
        } catch (e: Exception) {
            Log.d("AppLog", "failed to plot polylines in map, ${e}: ${e.stackTrace}")
        }

        // add markers
        val startMarker = rideMapData.getStartPointMarker()
        val endMarker = rideMapData.getEndPointMarker()
        val maxVelocityMarker = rideMapData.getMaxVelocityPointMarker()
        if (startMarker != null) {
            googleMap.addMarker(startMarker)
        }
        if (endMarker != null) {
            googleMap.addMarker(endMarker)
        }
        if (maxVelocityMarker != null) {
            googleMap.addMarker(maxVelocityMarker)
        }

        // set map default camera based on latitude longitude bounds
        val latLngBounds = rideMapData.getLatLngBounds()
        latLngBounds?.let {
            mapDefaultCamera =
                CameraUpdateFactory.newLatLngBounds(latLngBounds, 250)
        }

        setMapType()
        fitMapCamera()

        // override onMarkerClick
        googleMap.setOnMarkerClickListener(fun(marker): Boolean {
            mapActiveMarker?.isVisible = false
            marker.showInfoWindow()
            return true
        })

        // show marker when click polyline
        googleMap.setOnPolylineClickListener {
            mapActiveMarker?.isVisible = false
            mapActiveMarker = it.tag as? Marker
            mapActiveMarker?.isVisible = true
            mapActiveMarker?.showInfoWindow()
        }
    }

    fun setMapType() {
        if (isMapHybrid) {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        } else {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
        isMapHybrid = !isMapHybrid
    }

    fun fitMapCamera() {
        val mapCamera = mapDefaultCamera
        if (mapCamera != null) {
            googleMap.moveCamera(mapCamera)
        }
    }
}