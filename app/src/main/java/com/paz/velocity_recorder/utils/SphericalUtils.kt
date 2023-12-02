/*
 * Copyright 2013 Google Inc.
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

package com.paz.velocity_recorder.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object SphericalUtils {
    /**
     * The earth's radius, in meters.
     * Mean radius as defined by IUGG.
     */
    private const val EARTH_RADIUS = 6371009.0

    /**
     * Returns the angle between two LatitudeLongitudes, in radians.
     */
    private fun computeAngleBetween(from: LatLng, to: LatLng): Double {
        // Haversine's formula
        val fromLat = Math.toRadians(from.latitude)
        val fromLng = Math.toRadians(from.longitude)
        val toLat = Math.toRadians(to.latitude)
        val toLng = Math.toRadians(to.longitude)
        val dLat = fromLat - toLat
        val dLng = fromLng - toLng
        return 2 * asin(
            sqrt(
                sin(dLat / 2).pow(2.0) +
                        cos(fromLat) * cos(toLat) * sin(dLng / 2).pow(2.0)
            )
        )
    }

    /**
     * Returns the distance between two LatitudeLongitudes, in meters.
     */
    fun computeDistanceBetween(from: LatLng, to: LatLng): Double {
        return computeAngleBetween(from, to) * EARTH_RADIUS
    }
}