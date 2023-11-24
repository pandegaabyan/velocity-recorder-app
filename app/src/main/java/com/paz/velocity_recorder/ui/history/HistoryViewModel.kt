package com.paz.velocity_recorder.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.paz.velocity_recorder.components.LocalityInfoCollector
import com.paz.velocity_recorder.db.DataDao
import com.paz.velocity_recorder.ui_model.RideItemData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val dataDao: DataDao,
    private val localityCollector: LocalityInfoCollector
) : ViewModel() {

    @Synchronized
    fun getLiveRides(): LiveData<List<RideItemData>> {
        return dataDao.getLiveRides().map { rideList ->
            rideList.map {
                RideItemData(
                    rideId = it.id ?: 0,
                    startLocality = it.startLocality,
                    endLocality = it.endLocality,
                    maxVelocity = it.maxVelocity,
                    distance = it.distance,
                    startTime = it.startTime,
                    endTime = it.endTime,
                    isRunning = it.isRunning
                )
            }
        }

    }

    fun deleteRide(rideId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dataDao.deleteRide(rideId)
            dataDao.deleteVelocities(rideId)
        }
    }

    fun updateLocality(rideId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val velocityEntities = dataDao.getStartEndVelocities(rideId)
            val firstEntity = velocityEntities.firstOrNull()
            val lastEntity = velocityEntities.lastOrNull()
            if (firstEntity != null && lastEntity != null) {
                val startLocality = localityCollector.getLocalityInfo(firstEntity.latitude, firstEntity.longitude)
                val endLocality = localityCollector.getLocalityInfo(lastEntity.latitude, lastEntity.longitude)
                if (startLocality != null && endLocality != null) {
                    dataDao.updateRideLocality(rideId, startLocality, endLocality)
                }
            }
        }
    }

    class Factory(private val dataDao: DataDao, private val localityCollector: LocalityInfoCollector) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(dataDao, localityCollector) as T
        }
    }
}