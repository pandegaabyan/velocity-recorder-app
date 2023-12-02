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

package com.paz.velocity_recorder.ui.ride_detail

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.paz.velocity_recorder.ui_model.VelocityNextItemData
import com.paz.velocity_recorder.ui_model.VelocitySimpleItemData
import com.paz.velocity_recorder.utils.ParallelUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MapPolylineCreator {

    suspend fun createPolylineOptions(
        velocities: List<VelocitySimpleItemData>
    ) = withContext(Dispatchers.Default) {

        val maxVelocity = velocities.maxByOrNull { p -> p.velocity }?.velocity ?: 0.0

        val polylineOptionList = mutableListOf<PolylineOptions>()

        val velocitiesWithNext = velocities.mapIndexed { index, entity ->
            VelocityNextItemData(
                timestamp = entity.timestamp,
                velocity = entity.velocity,
                latitude = entity.latitude,
                longitude = entity.longitude,
                nextLatitude = velocities.getOrNull(index + 1)?.latitude,
                nextLongitude = velocities.getOrNull(index + 1)?.longitude,
            )
        }

        ParallelUtils.forEach(velocitiesWithNext) {
            val polylineOptions = PolylineOptions()
            polylineOptions.width(10f)
            polylineOptions.add(LatLng(it.latitude, it.longitude))
            if (it.nextLatitude != null && it.nextLongitude != null) {
                polylineOptions.add(
                    LatLng(
                        it.nextLatitude,
                        it.nextLongitude
                    )
                )
            }

            polylineOptions.geodesic(false)
            polylineOptions.color(getColorBasedOnVelocity(maxVelocity, it.velocity))
            polylineOptionList.add(polylineOptions)
        }

        polylineOptionList
    }

    private fun getColorBasedOnVelocity(
        maxVelocity: Double,
        velocity: Double
    ): Int {
        val percentage =
            if (maxVelocity > 0) (velocity / maxVelocity) * 100 else 100.0
        return when {
            percentage < 33 -> {
                0xffc196fe.toInt()
            }

            percentage < 67 -> {
                0xff9d58fe.toInt()
            }

            else -> {
                0xff6a00ff.toInt()
            }
        }
    }
}