package com.paz.velocity_recorder.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.paz.velocity_recorder.components.ExportDataActivity
import com.paz.velocity_recorder.components.LocalityInfoCollector
import com.paz.velocity_recorder.databinding.FragmentHistoryBinding
import com.paz.velocity_recorder.db.AppDatabase
import com.paz.velocity_recorder.ui.ride_detail.RideDetailActivity
import com.paz.velocity_recorder.ui_model.RideItemData

class HistoryFragment : Fragment() {

    private val dataDao by lazy { AppDatabase.getDatabase(requireContext()).dataDao() }
    private val localityCollector by lazy { LocalityInfoCollector(requireContext()) }

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(dataDao, localityCollector)
    }

    private lateinit var adapter: HistoryAdapter
    private lateinit var viewBinding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentHistoryBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HistoryAdapter(::itemClick, ::exportRide, ::deleteRide, ::updateLocality)
        viewBinding.recyclerHistory.adapter = adapter

        viewModel.getLiveRides().observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }
    }

    private fun exportRide(rideItemData: RideItemData) {
        ExportDataActivity.open(
            requireActivity(),
            rideItemData.getRideId(),
            rideItemData.getStartText(),
            rideItemData.getEndText(),
            rideItemData.isLocalityNull()
        )
    }

    private fun deleteRide(rideId: Long) {
        viewModel.deleteRide(rideId)
    }

    private fun updateLocality(rideId: Long) {
        viewModel.updateLocality(rideId)
    }

    private fun itemClick(rideItemData: RideItemData) {
        RideDetailActivity.open(
            requireActivity(),
            rideItemData.getRideId(),
            rideItemData.isLocalityNull(),
            rideItemData.getStartTime(),
            rideItemData.getEndTime(),
            rideItemData.getStartText(),
            rideItemData.getEndText(),
            rideItemData.getTotalTime(),
            rideItemData.getTotalDistance(),
            rideItemData.getAvgVelocity(),
            rideItemData.getMaxVelocity(),
            rideItemData.getMaxVelocityNumber()
        )
    }
}
