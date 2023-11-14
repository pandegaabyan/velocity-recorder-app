package com.example.velocity_recorder.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.velocity_recorder.R
import com.example.velocity_recorder.ui_model.RideItemData

class HistoryAdapter(
    private val itemClickCallback: (rideId: Long) -> Unit,
    private val deleteItemCallback: (rideId: Long) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val rideItemList = mutableListOf<RideItemData>()

    fun updateData(rideItemList: List<RideItemData>) {
        this.rideItemList.clear()
        this.rideItemList.addAll(rideItemList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ride, parent, false)
        return RideItemViewHolder(view, itemClickCallback, deleteItemCallback)
    }

    override fun getItemCount() = rideItemList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RideItemViewHolder) {
            val rideItemData = rideItemList[position]
            holder.renderData(rideItemData)
        }
    }
}