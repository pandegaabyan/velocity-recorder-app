package com.example.velocity_recorder.ui.ride_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.velocity_recorder.db.DataDao
import com.example.velocity_recorder.utils.ConversionUtils
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RideDetailViewModel(
    private val dataDao: DataDao
) : ViewModel() {

    @Synchronized
    fun getVelocities(rideId: Long): LiveData<List<Entry>> {
        return dataDao.getVelocities(rideId).map { velocityList ->
            velocityList.map {
                Entry(
                    (it.timestamp - velocityList[0].timestamp).toFloat(),
                    ConversionUtils.convertMeterSecToKmHr(it.velocity).toFloat()
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

    class Factory(private val dataDao: DataDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RideDetailViewModel(dataDao) as T
        }
    }
}