package com.example.velocity_recorder.ui.home

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import com.example.velocity_recorder.databinding.FragmentHomeBinding
import com.example.velocity_recorder.db.DataDao
import com.example.velocity_recorder.db.RideEntity
import com.example.velocity_recorder.ui.chart.LineChartView
import com.example.velocity_recorder.ui_model.VelocitySimpleItemData
import com.example.velocity_recorder.ui_model.VelocitySimpleListData
import com.example.velocity_recorder.utils.ClockUtils
import com.example.velocity_recorder.utils.ConversionUtils
import com.example.velocity_recorder.utils.SphericalUtils
import com.github.mikephil.charting.data.Entry
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val UPDATE_RIDE_INTERVAL_MILLIS = 30000

class LocationProvider(
    private val binding: FragmentHomeBinding,
    private val locationManager: LocationManager,
    private val lineChartView: LineChartView,
    private val dataDao: DataDao
    ): LocationListener {

    private var firstChange = true
    private var startTime: Long = 0
    private var startLatitude: Double = 0.0
    private var startLongitude: Double = 0.0

    private var rideId: Long? = null
    private var lastUpdateRideTime: Long = 0

    private var distance: Double = 0.0
    private var currentTime: Long = 0
    private var currentVelocity: Double = 0.0
    private var maxVelocity: Double = 0.0 // Maximum velocity

    private val velocityListData = VelocitySimpleListData(mutableListOf<VelocitySimpleItemData>())
    private val velocityEntries = ArrayList<Entry>() // Velocity data for the line curve

    @SuppressLint("MissingPermission")
    fun subscribe() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
    }

    fun unsubscribe() {
        locationManager.removeUpdates(this)
        CoroutineScope(Dispatchers.IO).launch {
            updateRideVelocityData()
        }
    }

    override fun onLocationChanged(location: Location) {
        currentVelocity = location.speed.toDouble()
        binding.velocityValue.text = ConversionUtils.getVelocityKmHr(currentVelocity)

        currentTime = System.currentTimeMillis()
        if(firstChange) {
            firstChange = false
            startTime = currentTime
            startLatitude = location.latitude
            startLongitude = location.longitude
            lastUpdateRideTime = currentTime

            CoroutineScope(Dispatchers.IO).launch {
                initializeRideData().let {
                    rideId = it
                }
            }
        }

        // Update total distance
        distance = SphericalUtils.computeDistanceBetween(
            LatLng(startLatitude, startLongitude),
            LatLng(location.latitude, location.longitude)
        )
        binding.distanceValue.text = ConversionUtils.getDistanceKm(distance)

        // Update elapsed time
        val elapsedTime = (currentTime - startTime)
        val elapsedTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime)
        binding.timeValue.text = ClockUtils.getTime(elapsedTimeSeconds)

        // Update the max. velocity
        if (currentVelocity > maxVelocity) {
            maxVelocity = currentVelocity
            binding.maxVelocityValue.text = ConversionUtils.getVelocityKmHr(maxVelocity)
            lineChartView.setMaxLeftAxis(ConversionUtils.convertMeterSecToKmHr(maxVelocity).toFloat() * 1.2f)
        }

        // Update the avg. velocity
        val avgVelocity = distance / elapsedTimeSeconds
        binding.avgVelocityValue.text = ConversionUtils.getVelocityKmHr(avgVelocity)

        // Add and update data if conditions are satisfied
        velocityListData.add(VelocitySimpleItemData(
            timestamp = currentTime,
            velocity = currentVelocity,
            longitude = location.longitude,
            latitude = location.latitude,
        ))
        CoroutineScope(Dispatchers.IO).launch {
            updateRideVelocityDataConditionally()
        }

        // Add data to the line curve
        velocityEntries.add(Entry(elapsedTime.toFloat(),  ConversionUtils.convertMeterSecToKmHr(currentVelocity).toFloat()))
        lineChartView.setData(velocityEntries, "Velocity Data")
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

    private suspend fun updateRideVelocityData() {
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
        )
        dataDao.addVelocities(velocityListData.getVelocityEntities(rideIdNotNull))
        velocityListData.clear()
        lastUpdateRideTime = currentTime
        Log.d("AppLog", "success update data with id $rideIdNotNull")
    }

    private suspend fun updateRideVelocityDataConditionally() {
        if (rideId != null && (currentTime - lastUpdateRideTime) > UPDATE_RIDE_INTERVAL_MILLIS) {
            updateRideVelocityData()
        }
    }

}