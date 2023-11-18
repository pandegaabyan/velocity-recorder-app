package com.example.velocity_recorder.components

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import com.example.velocity_recorder.db.DataDao
import com.example.velocity_recorder.db.RideEntity
import com.example.velocity_recorder.ui_model.VelocitySimpleItemData
import com.example.velocity_recorder.ui_model.VelocitySimpleListData
import com.example.velocity_recorder.utils.SphericalUtils
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val UPDATE_RIDE_INTERVAL_MILLIS = 30000

class LocationProvider(
    private val locationManager: LocationManager,
    private val dataDao: DataDao,
    private val onChange: (elapsedTime: Long, distance: Double, velocity: Double) -> Unit,
    private val onMaxVelocityChange: (maxVelocity: Double) -> Unit,
) : LocationListener {

    private var rideId: Long? = null
    private var firstChange = true
    private var lastUpdateRideTime: Long = 0
    private var startTime: Long = 0
    private var startLatitude: Double = 0.0
    private var startLongitude: Double = 0.0

    private var distance: Double = 0.0
    private var currentTime: Long = 0
    private var currentVelocity: Double = 0.0
    private var maxVelocity: Double = 0.0 // Maximum velocity

    private val velocityListData = VelocitySimpleListData(mutableListOf<VelocitySimpleItemData>())

    fun setPrevData(prevData: LocationInitData) {
        if (prevData.rideId != null && prevData.rideId != -1L) {
            rideId = prevData.rideId
            maxVelocity = prevData.maxVelocity
            startTime = prevData.startTime
            startLatitude = prevData.startLatitude
            startLongitude = prevData.startLongitude
        }
    }

    @SuppressLint("MissingPermission")
    fun subscribe() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
    }

    fun unsubscribe(isDone: Boolean): LocationInitData {
        locationManager.removeUpdates(this)
        CoroutineScope(Dispatchers.IO).launch {
            updateRideVelocityData(isDone)
        }

        return LocationInitData(rideId, startTime, maxVelocity, startLatitude, startLongitude)
    }

    override fun onLocationChanged(location: Location) {
        currentVelocity = location.speed.toDouble()
        currentTime = System.currentTimeMillis()

        if (firstChange) {
            firstChange = false
            lastUpdateRideTime = currentTime

            if (rideId == null) {
                startTime = currentTime
                startLatitude = location.latitude
                startLongitude = location.longitude

                CoroutineScope(Dispatchers.IO).launch {
                    initializeRideData().let {
                        rideId = it
                    }
                }
            }
        }


        // Update total distance
        distance = SphericalUtils.computeDistanceBetween(
            LatLng(startLatitude, startLongitude),
            LatLng(location.latitude, location.longitude)
        )

        // Update max velocity and call onMaxVelocityChange
        if (currentVelocity > maxVelocity) {
            maxVelocity = currentVelocity
            onMaxVelocityChange(maxVelocity)
        }

        // Add and update data if conditions are satisfied
        velocityListData.add(
            VelocitySimpleItemData(
                timestamp = currentTime,
                velocity = currentVelocity,
                longitude = location.longitude,
                latitude = location.latitude,
            )
        )
        CoroutineScope(Dispatchers.IO).launch {
            updateRideVelocityDataConditionally()
        }

        // Call onChange
        onChange((currentTime - startTime), distance, currentVelocity)
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    private suspend fun initializeRideData(): Long {
        val rideIdNew = dataDao.addRide(
            RideEntity(
                startTime = currentTime,
                endTime = currentTime,
                distance = 0,
                maxVelocity = currentVelocity,
            )
        )
        Log.d("AppLog", "success add data with id $rideIdNew")

        return rideIdNew
    }

    private suspend fun updateRideVelocityData(isDone: Boolean) {
        var rideIdNotNull: Long
        try {
            rideIdNotNull = rideId!!
        } catch (npe: NullPointerException) {
            Log.d("AppLog", "failed to update data, npe: ${npe.toString()}")
            return
        }

        dataDao.updateRide(
            rideIdNotNull,
            currentTime,
            distance.toInt(),
            maxVelocity,
            !isDone
        )
        dataDao.addVelocities(velocityListData.getVelocityEntities(rideIdNotNull))
        velocityListData.clear()
        lastUpdateRideTime = currentTime
        Log.d("AppLog", "success update data with id $rideIdNotNull")
    }

    private suspend fun updateRideVelocityDataConditionally() {
        if (rideId != null && (currentTime - lastUpdateRideTime) > UPDATE_RIDE_INTERVAL_MILLIS) {
            updateRideVelocityData(false)
        }
    }

}