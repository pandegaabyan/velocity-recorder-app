/*
 * This file includes other work with modifications to make it simpler
 * That work covered by the following copyright:
 *
 * Copyright 2022 Prasanna Anbazhagan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paz.velocity_recorder.components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.paz.velocity_recorder.utils.LocationPermissionUtils

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
            LocationPermissionUtils.isLocationEnabled(this)
        ) {
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
