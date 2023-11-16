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
import com.example.velocity_recorder.databinding.FragmentHomeBinding
import com.example.velocity_recorder.db.AppDatabase
import com.example.velocity_recorder.ui.chart.LineChartView
import com.example.velocity_recorder.ui.permission.PermissionCheckActivity
import com.example.velocity_recorder.utils.DialogUtils
import com.example.velocity_recorder.utils.LocationPermissionUtils

class HomeFragment : Fragment() {

    private lateinit var viewBinding: FragmentHomeBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationProvider: LocationProvider
    private lateinit var lineChartView: LineChartView

    private val dataDao by lazy { AppDatabase.getDatabase(requireContext()).dataDao() }

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
        Log.d("AppLog", "home create view")

        viewBinding = FragmentHomeBinding.inflate(inflater, container, false)
        viewBinding.stopBtn.hide()
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("AppLog", "home view created")

        lineChartView = LineChartView(viewBinding.lineChart)
        lineChartView.setupChart()

        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProvider = LocationProvider(viewBinding, locationManager, lineChartView, dataDao)

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
                    stopRide()
                }).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AppLog", "home create")
    }

    override fun onResume() {
        super.onResume()
        Log.d("AppLog", "home resume")
    }

    override fun onStart() {
        super.onStart()
        Log.d("AppLog", "home start")
    }

    override fun onPause() {
        super.onPause()
        Log.d("AppLog", "home pause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("AppLog", "home stop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AppLog", "home destroy")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("AppLog", "home destroy view")
    }

    private fun startRide() {
        locationProvider.subscribe()
        viewBinding.velocityValue.text = "No motion"
        viewBinding.stopBtn.show()
        viewBinding.startBtn.hide()
    }

    private fun stopRide() {
        locationProvider.unsubscribe()
        viewBinding.stopBtn.hide()
        viewBinding.startBtn.show()
    }
}