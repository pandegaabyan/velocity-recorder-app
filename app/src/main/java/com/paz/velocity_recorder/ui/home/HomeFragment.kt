package com.paz.velocity_recorder.ui.home

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.Entry
import com.paz.velocity_recorder.R
import com.paz.velocity_recorder.components.ForegroundService
import com.paz.velocity_recorder.components.LocationInitData
import com.paz.velocity_recorder.components.LocationProvider
import com.paz.velocity_recorder.components.PermissionCheckActivity
import com.paz.velocity_recorder.databinding.FragmentHomeBinding
import com.paz.velocity_recorder.db.AppDatabase
import com.paz.velocity_recorder.ui.chart.LineChartHandler
import com.paz.velocity_recorder.utils.ClockUtils
import com.paz.velocity_recorder.utils.ConversionUtils
import com.paz.velocity_recorder.utils.DialogUtils
import com.paz.velocity_recorder.utils.LocationPermissionUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val FRESH_THRESHOLD_MILLIS = 300000
private const val CHECK_PREV_STATE_DELAY_MILLIS = 1000L

class HomeFragment : Fragment() {

    private var isRunning = false

    private lateinit var viewBinding: FragmentHomeBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationProvider: LocationProvider
    private lateinit var lineChartHandler: LineChartHandler

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

        lineChartHandler = LineChartHandler(viewBinding.lineChart)
        lineChartHandler.setupChart()

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

        viewLifecycleOwner.lifecycleScope.launch {
            checkAndContinuePrevState()
        }
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
            lineChartHandler.clear()
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
                locationPrevData.distance,
                locationPrevData.maxVelocity,
                locationPrevData.lastLatitude,
                locationPrevData.lastLongitude
            )
        }
    }

    private suspend fun checkAndContinuePrevState() {
        val rideId = dataDao.getRunningRide()?.id
        if (rideId == null) {
            viewBinding.startBtn.show()
            return
        }

        viewBinding.velocityValue.text = getString(R.string.loading)
        delay(CHECK_PREV_STATE_DELAY_MILLIS)

        val rideEntity = dataDao.getRunningRide()
        rideEntity!!

        if (System.currentTimeMillis() - rideEntity.endTime > FRESH_THRESHOLD_MILLIS) {
            viewBinding.startBtn.show()
            dataDao.stopRunningRide()
            return
        }

        val velocityList = dataDao.getVelocities(rideId)

        locationProvider.setPrevData(
            LocationInitData(
                rideId,
                rideEntity.startTime,
                rideEntity.distance.toDouble(),
                rideEntity.maxVelocity,
                velocityList.lastOrNull()?.latitude ?: 0.0,
                velocityList.lastOrNull()?.longitude ?: 0.0,
            )
        )
        onMaxVelocityChangeHandler(rideEntity.maxVelocity)

        velocityEntries.clear()
        velocityEntries.addAll(velocityList.map { velocityEntity ->
            Entry(
                (velocityEntity.timestamp - velocityList[0].timestamp).toFloat(),
                ConversionUtils.convertMeterSecToKmHr(velocityEntity.velocity).toFloat()
            )
        })
        lineChartHandler.setData(velocityEntries, "Velocity Data")

        startRide(false)
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
            lineChartHandler.setData(velocityEntries, "Velocity Data")
        }
    }

    private fun onMaxVelocityChangeHandler(maxVelocity: Double, shouldUpdateChart: Boolean = true) {
        viewBinding.maxVelocityValue.text = ConversionUtils.getVelocityKmHr(maxVelocity)

        if (shouldUpdateChart) {
            lineChartHandler.setMaxLeftAxis(
                ConversionUtils.convertMeterSecToKmHr(maxVelocity).toFloat()
            )
        }
    }
}