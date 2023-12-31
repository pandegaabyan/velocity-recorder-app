package com.paz.velocity_recorder.components

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import androidx.core.content.ContextCompat
import com.paz.velocity_recorder.db.AppDatabase
import com.paz.velocity_recorder.utils.NotificationUtils

private const val WAKELOCK_INTERVAL_MILLIS = 300000L
private const val CHECK_INTERVAL_MILLIS = 300000

class ForegroundService : Service() {

    private var prevCheckedElapsedTime: Long = 0
    private var prevCheckedDistance: Int = 0

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
            intent.getDoubleExtra("distance", 0.0),
            intent.getDoubleExtra("max_velocity", 0.0),
            intent.getDoubleExtra("last_latitude", 0.0),
            intent.getDoubleExtra("last_longitude", 0.0),
        )

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProvider = LocationProvider(locationManager, dataDao, ::onChangeHandler) {}

        locationProvider.setPrevData(locationInitData)
        locationProvider.subscribe()

        startForeground(
            NotificationUtils.VELOCITY_RECORDER_NOTIFICATION_ID,
            NotificationUtils.getNotification(this)
        )

        return START_STICKY
    }

    override fun onDestroy() {
        stopService(false)

        super.onDestroy()
    }

    private fun stopService(isDone: Boolean) {
        locationProvider.unsubscribe(isDone)

        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onChangeHandler(elapsedTime: Long, distance: Double, velocity: Double) {
        if (elapsedTime - prevCheckedElapsedTime > CHECK_INTERVAL_MILLIS) {
            if (prevCheckedDistance == distance.toInt()) {
                stopService(true)
                return
            }
            prevCheckedElapsedTime = elapsedTime
            prevCheckedDistance = distance.toInt()
        }
        checkAndUpdateCPUWake()
    }

    private fun checkAndUpdateCPUWake() {
        if (wakeLock.isHeld.not()) {
            wakeLock.acquire(WAKELOCK_INTERVAL_MILLIS)
        }
    }

    companion object {
        fun startService(
            context: Context,
            rideId: Long,
            startTime: Long,
            distance: Double,
            maxVelocity: Double,
            lastLatitude: Double,
            lastLongitude: Double
        ) {
            val startIntent = Intent(context, ForegroundService::class.java)
            startIntent.putExtra("ride_id", rideId)
            startIntent.putExtra("start_time", startTime)
            startIntent.putExtra("distance", distance)
            startIntent.putExtra("max_velocity", maxVelocity)
            startIntent.putExtra("last_latitude", lastLatitude)
            startIntent.putExtra("last_longitude", lastLongitude)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, ForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }
}