package com.ultraviolette.uvclusterhmi.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.ui.features.settings.wifi.WifiUiModel

/**
 * Adapter for displaying a list of [String] in a RecyclerView.
 * This adapter uses a [ListAdapter] to handle efficient updates using [DiffUtil].
 * Each item represents a device connection (e.g., Bluetooth, Wi-Fi).
 *
 * @property onDeviceSelected Callback invoked when an item is clicked.
 */
class WifiDeviceAdapter(private val onDeviceSelected: (String) -> Unit) :
    ListAdapter<WifiUiModel, WifiDeviceAdapter.ViewHolder>(DiffCallback) {
    private var selectedPosition = -1
    private var isItemClicked = false

    private var selectedSsid: String? = null

    fun updateSelected(ssid: String?) {
        selectedSsid = ssid
        notifyDataSetChanged()
    }

    /**
     * Resets the selection state when triggered from the fragment.
     * This ensures all items are updated with a non-clicked (default) appearance.
     */
    fun handleParentClick() {
        isItemClicked = false
        notifyDataSetChanged()
    }

    /**
     * ViewHolder for binding a single [String] to the layout.
     **/
    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        // private val ivSignal = itemView.findViewById<ImageView>(R.id.ivSignal)
        private val tvWifiDevice = itemView.findViewById<TextView>(R.id.tvWifiDevice)
        private val ivLock = itemView.findViewById<ImageView>(R.id.ivLock)

        /**
         * Binds the data from a [String] to the views.
         *
         * @param item The item to bind.
         */
        fun bind(item: WifiUiModel, position: Int) {
            tvWifiDevice.text = item.ssid

            val iconRes = when (item.level){
                0 -> R.drawable.ic_signal_wifi_0_bar
                1 -> R.drawable.ic_signal_wifi_1_bar
                2 -> R.drawable.ic_signal_wifi_2_bar
                3 -> R.drawable.ic_signal_wifi_3_bar
                4 -> R.drawable.ic_signal_wifi_4_bar
                5 -> R.drawable.ic_signal_wifi_full_bar

                else -> {R.drawable.ic_signal_wifi_full_bar}
            }

            // ivSignal.setImageResource(iconRes)

            ivLock.visibility = if (item.isSecured) View.VISIBLE else View.GONE

            // Set click listener for the item
            itemView.setOnSoundClickListener(itemView.context) {
                onDeviceSelected.invoke(item.ssid)
                isItemClicked = true
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

            }
            updateItemSelection(item.ssid == selectedSsid)
        }

        /**
         * Updates the visual state of the item based on selection and click origin.
         *
         * @param position Indicates whether the current item is selected.
         * @param context Used to access color and drawable resources.
         *
         * If the item was selected from the fragment (`isClicked` is false), it applies a white background.
         * If the item was selected via user interaction, it highlights in red or shows default unselected style.
         */
        private fun updateItemSelection(isSelected: Boolean) {
            if (isSelected) {
                tvWifiDevice.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.activeSelectionRed))
                tvWifiDevice.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                ivLock.drawable.setTint(ContextCompat.getColor(itemView.context, R.color.white))
            } else {
                tvWifiDevice.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.transparent))
                tvWifiDevice.setTextColor(ContextCompat.getColor(itemView.context, R.color.unSelected))
                ivLock.drawable.setTint(ContextCompat.getColor(itemView.context, R.color.unSelected))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_wifi_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    /**
     * [DiffUtil.ItemCallback] implementation to efficiently update the list.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<WifiUiModel>() {
        override fun areItemsTheSame(
            oldItem: WifiUiModel,
            newItem: WifiUiModel
        ): Boolean = oldItem .ssid== newItem.ssid

        override fun areContentsTheSame(
            oldItem: WifiUiModel,
            newItem: WifiUiModel
        ): Boolean = oldItem == newItem
    }
}


