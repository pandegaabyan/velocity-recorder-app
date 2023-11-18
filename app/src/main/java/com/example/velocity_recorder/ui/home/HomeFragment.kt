package com.example.velocity_recorder.ui.home

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.velocity_recorder.components.ForegroundService
import com.example.velocity_recorder.components.LocationInitData
import com.example.velocity_recorder.components.LocationProvider
import com.example.velocity_recorder.databinding.FragmentHomeBinding
import com.example.velocity_recorder.db.AppDatabase
import com.example.velocity_recorder.ui.chart.LineChartView
import com.example.velocity_recorder.ui.permission.PermissionCheckActivity
import com.example.velocity_recorder.utils.ClockUtils
import com.example.velocity_recorder.utils.ConversionUtils
import com.example.velocity_recorder.utils.DialogUtils
import com.example.velocity_recorder.utils.LocationPermissionUtils
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private var isRunning = false

    private lateinit var viewBinding: FragmentHomeBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationProvider: LocationProvider
    private lateinit var lineChartView: LineChartView

    private val dataDao by lazy { AppDatabase.getDatabase(requireContext()).dataDao() }

    private val velocityEntries = ArrayList<Entry>() // Velocity data for the line curve

    private val permissionActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (LocationPermissionUtils.isLocationEnabled(requireContext())
                && LocationPermissionUtils.isBasicPermissionGranted(requireContext())
            ) {
                startRide()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentHomeBinding.inflate(inflater, container, false)
        viewBinding.startBtn.hide()
        viewBinding.stopBtn.hide()
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lineChartView = LineChartView(viewBinding.lineChart)
        lineChartView.setupChart()

        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProvider = LocationProvider(locationManager, dataDao, ::onChangeHandler, ::onMaxVelocityChangeHandler)

        viewBinding.startBtn.setOnClickListener {
            if (LocationPermissionUtils.isBasicPermissionGranted(requireContext())
                && LocationPermissionUtils.isLocationEnabled(requireContext())
            ) {
                startRide()
            } else {
                permissionActivityResultLauncher.launch(
                    PermissionCheckActivity.getOpenIntent(
                        context = requireContext()
                    )
                )
            }
        }

        viewBinding.stopBtn.setOnClickListener {
            DialogUtils.createDialog(
                context = requireContext(),
                message = "Stop Ride",
                positiveAction = "Stop",
                negativeAction = "No",
                onSuccessAction = {
                    stopRide(true)
                }).show()
        }
    }

    override fun onStart() {
        super.onStart()

        ForegroundService.stopService(requireContext())

        setInitialData()
    }

    override fun onStop() {
        super.onStop()

        if (isRunning) {
            stopRide(false)
        }
    }

    private fun startRide() {
        locationProvider.subscribe()
        viewBinding.velocityValue.text = "No motion"
        viewBinding.stopBtn.show()
        viewBinding.startBtn.hide()
        isRunning = true
    }

    private fun stopRide(isDone: Boolean) {
        val locationPrevData = locationProvider.unsubscribe(isDone)
        viewBinding.stopBtn.hide()
        viewBinding.startBtn.show()
        isRunning = false

        if (!isDone && locationPrevData.rideId != null) {
            ForegroundService.startService(
                requireContext(),
                locationPrevData.rideId,
                locationPrevData.startTime,
                locationPrevData.maxVelocity,
                locationPrevData.startLatitude,
                locationPrevData.startLongitude
            )
        }
    }

    private fun setInitialData() {
        viewLifecycleOwner.lifecycleScope.launch {
            dataDao.getRunningRide().let {rideEntity ->
                val isFresh = if (rideEntity != null) {
                    rideEntity.endTime - System.currentTimeMillis() < 60000
                } else {
                    null
                }
                val rideId = rideEntity?.id
                if (rideId != null && isFresh == true) {
                    viewBinding.velocityValue.text = "Loading ..."
                    delay(1000)
                    dataDao.getVelocities(rideId).let {velocityList ->
                        val firstItem = velocityList.getOrNull(0)
                        if (firstItem != null) {
                            locationProvider.setPrevData(LocationInitData(
                                rideId,
                                rideEntity.startTime,
                                rideEntity.maxVelocity,
                                firstItem.latitude,
                                firstItem.longitude
                            ))
                            velocityEntries.addAll(velocityList.map {
                                Entry(
                                    (it.timestamp - velocityList[0].timestamp).toFloat(),
                                    ConversionUtils.convertMeterSecToKmHr(it.velocity).toFloat()
                                )
                            })
                            lineChartView.setData(velocityEntries, "Velocity Data")
                            onMaxVelocityChangeHandler(rideEntity.maxVelocity)
                            startRide()
                        } else {
                            viewBinding.startBtn.show()
                        }
                    }
                } else {
                    viewBinding.startBtn.show()

                    if (isFresh == false) {
                        dataDao.stopRunningRide()
                    }
                }
            }
        }
    }

    private fun onChangeHandler(elapsedTime: Long, distance: Double, velocity: Double) {
        val elapsedTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime)
        viewBinding.timeValue.text = ClockUtils.getTime(elapsedTimeSeconds)

        viewBinding.distanceValue.text = ConversionUtils.getDistanceKm(distance)
        viewBinding.velocityValue.text = ConversionUtils.getVelocityKmHr(velocity)

        val avgVelocity = if (elapsedTime != 0L) {
            distance / elapsedTimeSeconds
        } else {
            0.0
        }
        viewBinding.avgVelocityValue.text = ConversionUtils.getVelocityKmHr(avgVelocity)

        velocityEntries.add(Entry(elapsedTime.toFloat(),  ConversionUtils.convertMeterSecToKmHr(velocity).toFloat()))
        lineChartView.setData(velocityEntries, "Velocity Data")
    }

    private fun onMaxVelocityChangeHandler(maxVelocity: Double) {
        viewBinding.maxVelocityValue.text = ConversionUtils.getVelocityKmHr(maxVelocity)
        lineChartView.setMaxLeftAxis(ConversionUtils.convertMeterSecToKmHr(maxVelocity).toFloat() * 1.2f)
    }
}