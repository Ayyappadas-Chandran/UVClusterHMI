package com.ultraviolette.uvclusterhmi.ui.adapter

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener

class BluetoothDeviceAdapter(private val onDeviceSelected: (BluetoothDevice) -> Unit) :
    ListAdapter<BluetoothDevice, BluetoothDeviceAdapter.ViewHolder>(DiffCallback) {
    private var selectedPosition = -1
    private var isItemClicked = false

    /**
     * Resets the selection state when triggered from the fragment.
     * This ensures all items are updated with a non-clicked (default) appearance.
     */
    fun handleParentClick() {
        isItemClicked = false
        notifyDataSetChanged()
    }

    /**
     * ViewHolder for binding a single [BluetoothDevice] to the layout.
     **/
    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val tvDevice = itemView.findViewById<TextView>(R.id.tvDevice)

        /**
         * Binds the data from a [BluetoothDevice] to the views.
         *
         * @param item The item to bind.
         */
        fun bind(item: BluetoothDevice, position: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        itemView.context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
            }

            tvDevice.text = item.name?.takeIf { it.isNotEmpty() }
                ?: itemView.context.getString(R.string.unknown)
            itemView.setOnSoundClickListener(itemView.context) {
                isItemClicked = true
                onDeviceSelected.invoke(item)
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }
            updateItemSelection(position, itemView.context)
        }

        /**
         * Updates the selection state of the item.
         */
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    /**
     * [DiffUtil.ItemCallback] implementation to efficiently update the list.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<BluetoothDevice>() {
        override fun areItemsTheSame(
            oldItem: BluetoothDevice,
            newItem: BluetoothDevice
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: BluetoothDevice,
            newItem: BluetoothDevice
        ): Boolean = oldItem.address == newItem.address
    }
}
