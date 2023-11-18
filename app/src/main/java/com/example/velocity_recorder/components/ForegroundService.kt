package com.example.velocity_recorder.components

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.velocity_recorder.db.AppDatabase
import com.example.velocity_recorder.utils.NotificationUtils
import java.util.concurrent.TimeUnit

class ForegroundService : Service() {

    private lateinit var locationManager: LocationManager
    private lateinit var locationProvider: LocationProvider

    private val dataDao by lazy { AppDatabase.getDatabase(this).dataDao() }

    /**
     * This ensures locking Android to not go to sleep and it will help listening to GPS Signal even if the phone went to idle state.
     */
    private val wakeLock by lazy {
        (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
            PARTIAL_WAKE_LOCK,
            packageName
        ).also {
            it.setReferenceCounted(false)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent == null) {
            stopSelf()
            return START_STICKY
        }

        val locationInitData = LocationInitData(
            intent.getLongExtra("ride_id", -1),
            intent.getLongExtra("start_time", 0),
            intent.getDoubleExtra("max_velocity", 0.0),
            intent.getDoubleExtra("start_latitude", 0.0),
            intent.getDoubleExtra("start_longitude", 0.0)
        )

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProvider = LocationProvider(locationManager, dataDao, ::onChangeHandler, {})

        locationProvider.setPrevData(locationInitData)
        locationProvider.subscribe()

        startForeground(
            NotificationUtils.VELOCITY_RECORDER_NOTIFICATION_ID,
            NotificationUtils.getNotification(this)
        )

        return START_STICKY
    }

    override fun onDestroy() {

        locationProvider.unsubscribe(false)

        if (wakeLock.isHeld) {
            wakeLock.release()
        }

        super.onDestroy()
    }

    private fun onChangeHandler(elapsedTime: Long, distance: Double, velocity: Double) {
        checkAndUpdateCPUWake()
    }

    private fun checkAndUpdateCPUWake() {
        if (wakeLock.isHeld.not()) {
            wakeLock.acquire(TimeUnit.HOURS.toMillis(1))
        }
    }

    companion object {
        fun startService(context: Context, rideId: Long, startTime: Long, maxVelocity: Double, startLatitude: Double, startLongitude: Double) {
            val startIntent = Intent(context, ForegroundService::class.java)
            startIntent.putExtra("ride_id", rideId)
            startIntent.putExtra("start_time", startTime)
            startIntent.putExtra("max_velocity", maxVelocity)
            startIntent.putExtra("start_latitude", startLatitude)
            startIntent.putExtra("start_longitude", startLongitude)
            ContextCompat.startForegroundService(context, startIntent)
        }
        fun stopService(context: Context) {
            val stopIntent = Intent(context, ForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }
}