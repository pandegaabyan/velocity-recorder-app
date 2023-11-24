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

    /**
     * Wraps the given value into the inclusive-exclusive interval between min and max.
     *
     * @param n   The value to wrap.
     * @param min The minimum.
     * @param max The maximum.
     */
    fun wrap(n: Double, min: Double, max: Double): Double {
        return if (n >= min && n < max) n else mod(n - min, max - min) + min
    }

    /**
     * Returns the non-negative remainder of x / m.
     *
     * @param x The operand.
     * @param m The modulus.
     */
    fun mod(x: Double, m: Double): Double {
        return (x % m + m) % m
    }

    /**
     * Returns the heading from one LatLng to another LatLng. Headings are
     * expressed in degrees clockwise from North within the range [-180,180).
     *
     * @return The heading in degrees clockwise from north.
     */
    fun computeHeading(from: LatLng, to: LatLng): Double {
        // http://williams.best.vwh.net/avform.htm#Crs
        val fromLat = Math.toRadians(from.latitude)
        val fromLng = Math.toRadians(from.longitude)
        val toLat = Math.toRadians(to.latitude)
        val toLng = Math.toRadians(to.longitude)
        val dLng = toLng - fromLng
        val heading = Math.atan2(
            Math.sin(dLng) * Math.cos(toLat),
            Math.cos(fromLat) * Math.sin(toLat) - Math.sin(fromLat) * Math.cos(toLat) * Math.cos(
                dLng
            )
        )
        return wrap(Math.toDegrees(heading), -180.0, 180.0)
    }
}