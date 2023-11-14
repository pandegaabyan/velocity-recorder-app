package com.example.velocity_recorder.ui.home

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import com.example.velocity_recorder.R
import com.example.velocity_recorder.databinding.FragmentHomeBinding
import com.example.velocity_recorder.utils.ClockUtils
import com.example.velocity_recorder.utils.ConversionUtils
import com.example.velocity_recorder.utils.SphericalUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.maps.model.LatLng
import java.util.ArrayList
import java.util.Locale

class LocationProvider(
    private val binding: FragmentHomeBinding,
    private val locationManager: LocationManager,
    private val lineChart: LineChart,
    ): LocationListener {

    private var maxVelocity: Double = 0.0 // Maximum velocity
    private var sumVelocity: Double = 0.0 // Total velocity
    private var countVelocity = 0 // Number of velocities (i.e. number of velocity measurements)
    private val velocityData = ArrayList<Entry>() // Velocity data for the line curve
    private var firstCount = false
    private var startTime: Long = 0
    private var startLatitude: Double = 0.0
    private var startLongitude: Double = 0.0

    @SuppressLint("MissingPermission")
    fun subscribe() {
        Log.d("MyLog", "start subscribe location")
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
        Log.d("MyLog", "location subscribed")
    }

    fun unsubscribe() {
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        val velocity = location.speed.toDouble()
        binding.velocityValue.text = ConversionUtils.getVelocityKmHr(velocity)

        if(!firstCount) {
            firstCount = true
            startTime = System.currentTimeMillis()
            startLatitude = location.latitude
            startLongitude = location.longitude
        }
        val endTime = System.currentTimeMillis()

        // Update total distance
        val distanceInMeters = SphericalUtils.computeDistanceBetween(
            LatLng(startLatitude, startLongitude),
            LatLng(location.latitude, location.longitude)
        )
        binding.distanceValue.text = ConversionUtils.getDistanceKm(distanceInMeters)

        // Update elapsed time
        val elapsedTime = (endTime - startTime)
        binding.timeValue.text = ClockUtils.getTime(elapsedTime / 1000)

        // Update the max. velocity
        if (velocity > maxVelocity) {
            maxVelocity = velocity
            binding.maxVelocityValue.text = ConversionUtils.getVelocityKmHr(maxVelocity)
            lineChart.axisLeft.axisMaximum = ConversionUtils.convertMeterSecToKmHr(maxVelocity).toFloat() * 1.2f
        }

        // Update the avg. velocity
        sumVelocity += velocity
        countVelocity++
        val avgVelocity = sumVelocity / countVelocity
        binding.avgVelocityValue.text = ConversionUtils.getVelocityKmHr(avgVelocity)

        // Add data to the line curve
        velocityData.add(Entry(elapsedTime.toFloat(),  ConversionUtils.convertMeterSecToKmHr(velocity).toFloat()))
        val dataSet = LineDataSet(velocityData, "Velocity Data")

        // Configure the appearance of the line curve
        dataSet.color = R.color.purple_500
        dataSet.setCircleColor(R.color.purple_500)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 1f
        dataSet.setDrawCircleHole(false)
        dataSet.setDrawValues(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        // Update the line chart
        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}
}