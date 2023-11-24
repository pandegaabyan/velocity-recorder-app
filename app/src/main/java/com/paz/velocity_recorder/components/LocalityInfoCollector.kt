/*
 * This file includes other work with modifications related to data type
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
import android.location.Address
import android.location.Geocoder
import java.util.Locale

class LocalityInfoCollector(
    private val context: Context,
) {

    fun getLocalityInfo(lat: Double, lon: Double): String? {
        return getLocationInfo(lat, lon)?.locality
    }

    private fun getLocationInfo(lat: Double, lon: Double): LocalityInfoData? {

        if (!Geocoder.isPresent()) {
            return null
        }

        val gcd = Geocoder(context, Locale.getDefault())

        return try {
            val addressList = gcd.getFromLocation(lat, lon, 1)
            if (!addressList.isNullOrEmpty()) {
                parseLocationInfoFromAddressList(addressList)
            } else {
                null
            }
        } catch (ignore: Exception) {
            null
        }
    }

    private fun parseLocationInfoFromAddressList(addressList: List<Address>): LocalityInfoData? {
        var fetchedLocality: String? = null
        var fetchedCity: String? = null
        var fetchedCountry: String? = null
        var fetchedCountryCode: String? = null

        for (addressData in addressList) {
            val street = addressData.getAddressLine(0)
            val locality = addressData.locality
            val subLocality = addressData.subLocality
            fetchedCity = addressData.adminArea
            fetchedCountry = addressData.countryName
            fetchedCountryCode = addressData.countryCode

            if ((locality != null && locality.trim()
                    .lowercase() != "null" && locality.trim()
                    .isNotBlank()) || (subLocality != null && subLocality.trim()
                    .lowercase() != "null" && subLocality.trim().isNotBlank())
            ) {

                if (locality != null && locality.trim()
                        .isNotBlank() && subLocality != null && subLocality.trim().isNotBlank()
                ) {
                    fetchedLocality = "${subLocality.trim()}, ${locality.trim()}"
                    break
                } else if (locality != null && locality.trim().isNotBlank()) {
                    fetchedLocality = locality.trim()
                    break
                } else if (subLocality != null && subLocality.trim().isNotBlank()) {
                    fetchedLocality = subLocality.trim()
                    break
                }
            } else if (street != null && street.trim().isNotBlank() && street.trim()
                    .lowercase() != "null"
            ) {
                fetchedLocality = street
            }
        }

        return if (fetchedLocality != null && fetchedCountry != null && fetchedCountryCode != null) {
            LocalityInfoData(fetchedLocality, fetchedCity, fetchedCountry, fetchedCountryCode)
        } else {
            null
        }
    }

}