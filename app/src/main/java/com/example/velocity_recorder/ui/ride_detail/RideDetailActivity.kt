package com.example.velocity_recorder.ui.ride_detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.velocity_recorder.components.ExportDataActivity
import com.example.velocity_recorder.databinding.ActivityRideDetailBinding
import com.example.velocity_recorder.db.AppDatabase
import com.example.velocity_recorder.ui.chart.LineChartView
import com.example.velocity_recorder.utils.ConversionUtils
import com.example.velocity_recorder.utils.DialogUtils

class RideDetailActivity: AppCompatActivity() {

    private lateinit var viewBinding: ActivityRideDetailBinding
    private lateinit var lineChartView: LineChartView

    private var rideId: Long = -1L
    private var startText: String = ""
    private var endText: String = ""

    private val dataDao by lazy { AppDatabase.getDatabase(this).dataDao() }

    private val viewModel: RideDetailViewModel by viewModels {
        RideDetailViewModel.Factory(dataDao)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityRideDetailBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        rideId = intent.getLongExtra("ride_id", -1L)

        // set ride data ui based on selected item in history
        var maxVelocityNumber: Double = 0.0
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
        } catch (npe: NullPointerException) {
            Log.d("AppLog", "failed to get ride data, npe: ${npe.toString()}")
        }

        viewBinding.backIcon.setOnClickListener {
            onBackPressed()
        }
        viewBinding.exportIcon.setOnClickListener {
            exportRide()
        }
        viewBinding.deleteIcon.setOnClickListener {
            deleteRide()
        }

        lineChartView = LineChartView(viewBinding.lineChart)
        lineChartView.setupChart()
        lineChartView.setMaxLeftAxis(ConversionUtils.convertMeterSecToKmHr(maxVelocityNumber).toFloat() * 1.2f)

    }

    override fun onStart() {
        super.onStart()

        setChartData()
    }

    override fun onBackPressed() {
        finish()
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
                onBackPressed()
            },
            onNegativeAction = {}
        ).show()
    }

    companion object {

        fun open(context: Context,
                 rideId: Long,
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