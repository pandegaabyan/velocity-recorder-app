package com.paz.velocity_recorder.ui.history

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.paz.velocity_recorder.databinding.ItemRideBinding
import com.paz.velocity_recorder.ui_model.RideItemData
import com.paz.velocity_recorder.utils.DialogUtils

class RideItemViewHolder(
    itemView: View,
    private val itemClickCallback: (rideItemData: RideItemData) -> Unit,
    private val exportRideCallback: (rideItemData: RideItemData) -> Unit,
    private val deleteRideCallback: (rideId: Long) -> Unit,
    private val updateLocalityCallback: (rideId: Long) -> Unit,
) : RecyclerView.ViewHolder(itemView) {

    fun renderData(rideItemData: RideItemData) {

        val viewBinding = ItemRideBinding.bind(itemView)

        if (rideItemData.isRunning()) {
            viewBinding.exportIcon.visibility = View.GONE
            viewBinding.deleteIcon.visibility = View.GONE
        }

        viewBinding.loadingSign.visibility = if (rideItemData.isRunning()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        viewBinding.updateLocalityIcon.visibility = if (!rideItemData.isLocalityNull() || rideItemData.isRunning()) {
            View.GONE
        } else {
            View.VISIBLE
        }

        viewBinding.tagText.text = rideItemData.getStartEndText()
        viewBinding.timeText.text = rideItemData.getTimeText()
        viewBinding.maxVelocityText.text = rideItemData.getMaxVelocity()
        viewBinding.totalTimeText.text = rideItemData.getTotalTime()
        viewBinding.distanceText.text = rideItemData.getTotalDistance()

        viewBinding.exportIcon.setOnClickListener {
            exportRideCallback(rideItemData)
        }

        viewBinding.deleteIcon.setOnClickListener {
            DialogUtils.createDialog(
                context = viewBinding.root.context,
                message = "Delete Ride?",
                positiveAction = "Delete",
                negativeAction = "Cancel",
                onSuccessAction = {
                    deleteRideCallback(rideItemData.getRideId())
                },
                onNegativeAction = {}
            ).show()
        }

        viewBinding.updateLocalityIcon.setOnClickListener {
            viewBinding.updateLocalityIcon.visibility = View.GONE
            viewBinding.loadingSign.visibility = View.VISIBLE
            updateLocalityCallback(rideItemData.getRideId())
        }

        viewBinding.mainLayout.setOnClickListener {
            if (!rideItemData.isRunning()) {
                itemClickCallback(rideItemData)
            }
        }
    }

}