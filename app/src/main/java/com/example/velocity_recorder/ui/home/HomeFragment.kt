package com.example.velocity_recorder.ui.home

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.velocity_recorder.R
import com.example.velocity_recorder.components.ForegroundService
import com.example.velocity_recorder.components.LocationInitData
import com.example.velocity_recorder.components.LocationProvider
import com.example.velocity_recorder.components.PermissionCheckActivity
import com.example.velocity_recorder.databinding.FragmentHomeBinding
import com.example.velocity_recorder.db.AppDatabase
import com.example.velocity_recorder.ui.chart.LineChartView
import com.example.velocity_recorder.utils.ClockUtils
import com.example.velocity_recorder.utils.ConversionUtils
import com.example.velocity_recorder.utils.DialogUtils
import com.example.velocity_recorder.utils.LocationPermissionUtils
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val FRESH_THRESHOLD_MILLIS = 300000

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
                startRide(true)
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

        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProvider = LocationProvider(
            locationManager,
            dataDao,
            ::onChangeHandler,
            ::onMaxVelocityChangeHandler
        )

        viewBinding.startBtn.setOnClickListener {
            if (LocationPermissionUtils.isBasicPermissionGranted(requireContext())
                && LocationPermissionUtils.isLocationEnabled(requireContext())
            ) {
                startRide(true)
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

        checkAndContinuePrevState()
    }

    override fun onStop() {
        super.onStop()

        if (isRunning) {
            stopRide(false)
        }
    }

    private fun startRide(shouldClear: Boolean) {
        if (shouldClear) {
            locationProvider.resetData()
            onChangeHandler(0, 0.0, 0.0, false)
            onMaxVelocityChangeHandler(0.0, false)
            velocityEntries.clear()
            lineChartView.clear()
        }
        locationProvider.subscribe()
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

    private fun checkAndContinuePrevState() {
        viewLifecycleOwner.lifecycleScope.launch {
            dataDao.getRunningRide().let { rideEntity ->
                val isFresh = if (rideEntity != null) {
                    System.currentTimeMillis() - rideEntity.endTime < FRESH_THRESHOLD_MILLIS
                } else {
                    null
                }
                val rideId = rideEntity?.id
                if (rideId != null && isFresh == true) {
                    viewBinding.velocityValue.text = getString(R.string.loading)
                    delay(1000)
                    dataDao.getVelocities(rideId).let { velocityList ->
                        val firstItem = velocityList.getOrNull(0)
                        if (firstItem != null) {
                            locationProvider.setPrevData(
                                LocationInitData(
                                    rideId,
                                    rideEntity.startTime,
                                    rideEntity.maxVelocity,
                                    firstItem.latitude,
                                    firstItem.longitude
                                )
                            )
                            velocityEntries.clear()
                            velocityEntries.addAll(velocityList.map {
                                Entry(
                                    (it.timestamp - velocityList[0].timestamp).toFloat(),
                                    ConversionUtils.convertMeterSecToKmHr(it.velocity).toFloat()
                                )
                            })
                            lineChartView.setData(velocityEntries, "Velocity Data")
                            onMaxVelocityChangeHandler(rideEntity.maxVelocity)
                            startRide(false)
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

    private fun onChangeHandler(
        elapsedTime: Long,
        distance: Double,
        velocity: Double,
        shouldUpdateChart: Boolean = true
    ) {
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

        if (shouldUpdateChart) {
            velocityEntries.add(
                Entry(
                    elapsedTime.toFloat(),
                    ConversionUtils.convertMeterSecToKmHr(velocity).toFloat()
                )
            )
            lineChartView.setData(velocityEntries, "Velocity Data")
        }
    }

    private fun onMaxVelocityChangeHandler(maxVelocity: Double, shouldUpdateChart: Boolean = true) {
        viewBinding.maxVelocityValue.text = ConversionUtils.getVelocityKmHr(maxVelocity)

        if (shouldUpdateChart) {
            lineChartView.setMaxLeftAxis(
                ConversionUtils.convertMeterSecToKmHr(maxVelocity).toFloat()
            )
        }
    }
}