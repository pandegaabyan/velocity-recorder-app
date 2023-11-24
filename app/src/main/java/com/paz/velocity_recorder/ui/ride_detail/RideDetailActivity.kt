package com.paz.velocity_recorder.ui.ride_detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.paz.velocity_recorder.R
import com.paz.velocity_recorder.components.ExportDataActivity
import com.paz.velocity_recorder.components.LocalityInfoCollector
import com.paz.velocity_recorder.databinding.ActivityRideDetailBinding
import com.paz.velocity_recorder.db.AppDatabase
import com.paz.velocity_recorder.ui.chart.LineChartView
import com.paz.velocity_recorder.ui_model.RideMapData
import com.paz.velocity_recorder.utils.ConversionUtils
import com.paz.velocity_recorder.utils.DialogUtils
import com.paz.velocity_recorder.utils.observeOnce
import kotlinx.coroutines.launch

class RideDetailActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityRideDetailBinding
    private lateinit var lineChartView: LineChartView

    private var googleMap: GoogleMap? = null

    private var rideId: Long = -1L
    private var startText: String = ""
    private var endText: String = ""
    private var isMapHybrid = false

    private val dataDao by lazy { AppDatabase.getDatabase(this).dataDao() }
    private val localityCollector by lazy { LocalityInfoCollector(this) }

    private val viewModel: RideDetailViewModel by viewModels {
        RideDetailViewModel.Factory(dataDao)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityRideDetailBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.mapView.onCreate(savedInstanceState)

        rideId = intent.getLongExtra("ride_id", -1L)

        if (!intent.getBooleanExtra("is_locality_null", false)) {
            viewBinding.updateLocalityIcon.visibility = View.GONE
        }

        // set ride data ui based on selected item in history
        var maxVelocityNumber = 0.0
        try {
            startText = intent.getStringExtra("start_text")!!
            endText = intent.getStringExtra("end_text")!!
            viewBinding.startText.text = startText
            viewBinding.endText.text = endText
            viewBinding.timeValue.text = intent.getStringExtra("time_value")!!
            viewBinding.distanceValue.text = intent.getStringExtra("distance_value")!!
            viewBinding.avgVelocityValue.text = intent.getStringExtra("avg_velocity_value")!!
            viewBinding.maxVelocityValue.text = intent.getStringExtra("max_velocity_value")!!

            maxVelocityNumber = intent.getDoubleExtra("max_velocity_number", 0.0)
        } catch (e: NullPointerException) {
            Log.d("AppLog", "failed to get ride data, NullPointerException: ${e.stackTrace}")
        }

        viewBinding.backIcon.setOnClickListener {
            finish()
        }
        viewBinding.exportIcon.setOnClickListener {
            exportRide()
        }
        viewBinding.deleteIcon.setOnClickListener {
            deleteRide()
        }
        viewBinding.updateLocalityIcon.setOnClickListener {
            updateLocality()
        }
        viewBinding.mapTypeButton.setOnClickListener {
            isMapHybrid = !isMapHybrid
            setMapType()
        }

        lineChartView = LineChartView(viewBinding.lineChart)
        lineChartView.setupChart()
        lineChartView.setMaxLeftAxis(
            ConversionUtils.convertMeterSecToKmHr(maxVelocityNumber).toFloat()
        )
    }

    override fun onStart() {
        super.onStart()

        setChartData()

        viewBinding.loadingMapLayout.visibility = View.VISIBLE
        viewBinding.mapView.getMapAsync { googleMap ->
            this.googleMap = googleMap
            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_normal)
            )
            viewModel.getLiveRideMapData(rideId).observeOnce(this) {
                viewBinding.loadingMapLayout.visibility = View.GONE
                handleMapOperations(it)
            }
        }
    }

    private fun setChartData() {
        if (rideId == -1L) {
            return
        }
        viewModel.getLiveVelocities(rideId).observe(this) {
            lineChartView.setData(it, "Velocity Data")
        }
    }

    private fun exportRide() {
        ExportDataActivity.open(this, rideId, startText, endText)
    }

    private fun deleteRide() {
        if (rideId == -1L) {
            return
        }

        DialogUtils.createDialog(
            context = viewBinding.root.context,
            message = "Delete Ride?",
            positiveAction = "Delete",
            negativeAction = "Cancel",
            onSuccessAction = {
                viewModel.deleteRide(rideId)
                finish()
            },
            onNegativeAction = {}
        ).show()
    }

    private fun updateLocality() {
        lifecycleScope.launch {
            viewBinding.updateLocalityIcon.visibility = View.GONE
            viewBinding.loadingSign.visibility = View.VISIBLE
            val velocityEntities = dataDao.getStartEndVelocities(rideId)
            val firstEntity = velocityEntities.firstOrNull()
            val lastEntity = velocityEntities.lastOrNull()
            var startLocality: String? = null
            var endLocality: String? = null
            if (firstEntity != null && lastEntity != null) {
                startLocality =
                    localityCollector.getLocalityInfo(firstEntity.latitude, firstEntity.longitude)
                endLocality =
                    localityCollector.getLocalityInfo(lastEntity.latitude, lastEntity.longitude)
            }
            if (startLocality != null && endLocality != null) {
                dataDao.updateRideLocality(rideId, startLocality, endLocality)
                viewBinding.loadingSign.visibility = View.GONE
                viewBinding.startText.text = startLocality
                viewBinding.endText.text = endLocality
            } else {
                viewBinding.updateLocalityIcon.visibility = View.VISIBLE
                viewBinding.loadingSign.visibility = View.GONE
            }
        }
    }

    private fun handleMapOperations(rideMapData: RideMapData) {
        googleMap?.clear()
        plotRoute(rideMapData.getMapPolyLineOptionList())
        plotMarkers(
            rideMapData.getStartPointMarker(),
            rideMapData.getEndPointMarker(),
            rideMapData.getMaxVelocityPointMarker()
        )
        moveGoogleMap(
            rideMapData.getLatLngBounds(),
            rideMapData.getLatLngToZoom(),
            rideMapData.getHeading()
        )
        setMapType()
    }

    private fun setMapType() {
        if (isMapHybrid) {
            googleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
        } else {
            googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }

    private fun plotMarkers(
        startPointMarkerOptions: MarkerOptions?,
        endPointMarkerOptions: MarkerOptions?,
        maxVelocityMarkerOptions: MarkerOptions?
    ) {

        if (startPointMarkerOptions != null) {
            googleMap?.addMarker(startPointMarkerOptions)
        }

        if (endPointMarkerOptions != null) {
            googleMap?.addMarker(endPointMarkerOptions)
        }

        if (maxVelocityMarkerOptions != null) {
            googleMap?.addMarker(maxVelocityMarkerOptions)
        }
    }

    private fun plotRoute(polylineOptionsList: List<PolylineOptions>) {
        try {
            for (polylineOptions in polylineOptionsList) {
                googleMap?.addPolyline(polylineOptions)
            }
        } catch (e: Exception) {
            Log.d("AppLog", "failed to plot route in map, ${e}: ${e.stackTrace}")
        }
    }

    private fun moveGoogleMap(latLngBounds: LatLngBounds?, latLngToZoom: LatLng?, heading: Double) {
        try {
            latLngBounds?.let {
                val cameraUpdate =
                    CameraUpdateFactory.newLatLngBounds(latLngBounds, 180)
                googleMap?.moveCamera(cameraUpdate)
            } ?: let {
                latLngToZoom?.let {
                    val cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(latLngToZoom, 16f)
                    googleMap?.moveCamera(cameraUpdateFactory)
                }
            }
            googleMap?.cameraPosition?.let {
                val cameraPosition =
                    CameraPosition.builder(it)
                        .bearing(heading.toFloat()).build()
                googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        } catch (e: Exception) {
            Log.d("AppLog", "failed to move map camera, ${e}: ${e.stackTrace}")
        }
    }

    override fun onResume() {
        super.onResume()
        viewBinding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewBinding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        viewBinding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        viewBinding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding.mapView.onDestroy()
    }

    companion object {

        fun open(
            context: Context,
            rideId: Long,
            isLocalityNull: Boolean,
            startText: String,
            endText: String,
            timeValue: String,
            distanceValue: String,
            avgVelocityValue: String,
            maxVelocityValue: String,
            maxVelocityNumber: Double
        ) {
            val intent = Intent(context, RideDetailActivity::class.java).also {
                it.putExtra("ride_id", rideId)
                it.putExtra("is_locality_null", isLocalityNull)
                it.putExtra("start_text", startText)
                it.putExtra("end_text", endText)
                it.putExtra("time_value", timeValue)
                it.putExtra("distance_value", distanceValue)
                it.putExtra("avg_velocity_value", avgVelocityValue)
                it.putExtra("max_velocity_value", maxVelocityValue)
                it.putExtra("max_velocity_number", maxVelocityNumber)
            }
            context.startActivity(intent)
        }
    }
}