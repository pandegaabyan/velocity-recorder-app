package com.example.velocity_recorder.ui.history

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.velocity_recorder.utils.DialogUtils
import com.example.velocity_recorder.databinding.ItemRideBinding
import com.example.velocity_recorder.ui_model.RideItemData

class RideItemViewHolder(
    itemView: View,
    private val itemClickCallback: (rideId: Long) -> Unit,
    private val deleteItemCallback: (rideId: Long) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    fun renderData(rideItemData: RideItemData) {

        val viewBinding = ItemRideBinding.bind(itemView)

        viewBinding.tagText.text = rideItemData.getStartEndText()
        viewBinding.timeText.text = rideItemData.getTimeText()
        viewBinding.avgVelocityText.text = rideItemData.getAvgVelocity()
        viewBinding.totalTimeText.text = rideItemData.getTotalTime()
        viewBinding.distanceText.text = rideItemData.getTotalDistance()

        viewBinding.exportIcon.setOnClickListener {
            DialogUtils.createDialog(
                context = viewBinding.root.context,
                message = "Export Icon?",
                positiveAction = "Export",
                negativeAction = "Cancel",
                onSuccessAction = {},
                onNegativeAction = {}
            ).show()
        }

        viewBinding.deleteIcon.setOnClickListener {
            DialogUtils.createDialog(
                context = viewBinding.root.context,
                message = "Delete Ride?",
                positiveAction = "Delete",
                negativeAction = "Cancel",
                onSuccessAction = {
                    deleteItemCallback(rideItemData.getRideId())
                },
                onNegativeAction = {}
            ).show()
        }

        viewBinding.mainLayout.setOnClickListener {
            itemClickCallback(rideItemData.getRideId())
        }
    }

}