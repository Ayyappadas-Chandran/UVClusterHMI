package com.ultraviolette.uvclusterhmi.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.cluster.aidl.BtScanResult
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener

/**
 * RecyclerView adapter for Bluetooth scan results received from ClusterDataBus.
 * Items are [BtScanResult] parcelables; device name is already resolved on the bus side.
 */
class BluetoothDeviceAdapter(private val onDeviceSelected: (BtScanResult) -> Unit) :
    ListAdapter<BtScanResult, BluetoothDeviceAdapter.ViewHolder>(DiffCallback) {

    private var selectedPosition = -1
    private var isItemClicked = false

    /** Resets the selection state when triggered from the parent fragment. */
    fun handleParentClick() {
        isItemClicked = false
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDevice = itemView.findViewById<TextView>(R.id.tvDevice)

        fun bind(item: BtScanResult, position: Int) {
            tvDevice.text = item.name.takeIf { it.isNotEmpty() }
                ?: itemView.context.getString(R.string.unknown)
            itemView.setOnSoundClickListener(itemView.context) {
                isItemClicked = true
                onDeviceSelected(item)
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }
            updateItemSelection(position, itemView.context)
        }

        private fun updateItemSelection(position: Int, context: Context) {
            if (isItemClicked && position == selectedPosition) {
                tvDevice.apply {
                    setBackgroundColor(ContextCompat.getColor(context, R.color.activeSelectionRed))
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                }
                return
            }
            if (!isItemClicked && position == selectedPosition) {
                tvDevice.apply {
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                    setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                }
                return
            }
            tvDevice.apply {
                setTextColor(ContextCompat.getColor(context, R.color.unSelected))
                setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<BtScanResult>() {
        override fun areItemsTheSame(oldItem: BtScanResult, newItem: BtScanResult): Boolean =
            oldItem.address == newItem.address
        override fun areContentsTheSame(oldItem: BtScanResult, newItem: BtScanResult): Boolean =
            oldItem == newItem
    }
}
