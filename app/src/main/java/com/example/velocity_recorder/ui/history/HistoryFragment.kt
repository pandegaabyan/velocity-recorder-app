package com.example.velocity_recorder.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.velocity_recorder.databinding.FragmentHistoryBinding
import com.example.velocity_recorder.db.AppDatabase
import com.example.velocity_recorder.ui.ride_detail.RideDetailActivity
import com.example.velocity_recorder.ui_model.RideItemData

class HistoryFragment : Fragment() {

    private val dataDao by lazy { AppDatabase.getDatabase(requireContext()).dataDao() }

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(dataDao)
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

        adapter = HistoryAdapter(::itemClick, ::deleteRide)
        viewBinding.recyclerHistory.adapter = adapter

        viewModel.getRides().observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }
    }

    private fun deleteRide(rideId: Long) {
        viewModel.deleteRide(rideId)
    }

    private fun itemClick(rideItemData: RideItemData) {
        RideDetailActivity.open(
            requireActivity(),
            rideItemData.getRideId(),
            rideItemData.getStartText(),
            rideItemData.getEndText(),
            rideItemData.getTotalTime(),
            rideItemData.getTotalDistance(),
            rideItemData.getAvgVelocity(),
            rideItemData.getMaxVelocity()
        )
    }
}
