package com.example.velocity_recorder.ui.permission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.velocity_recorder.utils.LocationPermissionUtils

class PermissionCheckActivity : AppCompatActivity() {

    private val reqForLocation = 21

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        if (LocationPermissionUtils.isBasicPermissionGranted(this)) {
            LocationPermissionUtils.askEnableLocationRequest(this, ::locationEnabled)
        } else {
            askPermission()
        }
    }

    private fun askPermission() {
        val permissions = LocationPermissionUtils.getBasicPermissions()

        requestPermissions(permissions, reqForLocation)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == reqForLocation) {
            if (LocationPermissionUtils.isBasicPermissionGranted(this)) {
                if (LocationPermissionUtils.isLocationEnabled(this)) {
                    proceed()
                } else {
                    LocationPermissionUtils.askEnableLocationRequest(this, ::locationEnabled)
                }
            } else {
                cancel()
            }
        }
    }

    private fun locationEnabled(status: Boolean) {
        if (status) {
            checkAndProceed()
        } else {
            cancel()
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndProceed()
    }

    private fun checkAndProceed() {
        if (LocationPermissionUtils.isBasicPermissionGranted(this) &&
            LocationPermissionUtils.isLocationEnabled(this)) {
            proceed()
        }
    }

    private fun proceed() {
        LocationPermissionUtils.compute(this)
        setResult(RESULT_OK)
        finish()
    }

    private fun cancel() {
        finish()
    }

    companion object {

        fun getOpenIntent(
            context: Context
        ): Intent {
            return Intent(context, PermissionCheckActivity::class.java)
        }
    }
}
