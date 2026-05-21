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
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.TimeZoneItem
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener

/**
 * Adapter class for displaying a list of [TimeZoneItem] in a RecyclerView.
 *
 * This adapter is used to show general setting items with title and content,
 * and provides efficient list diffing using [DiffUtil].
 *
 * @param onItemClicked A lambda that will be called when an item is clicked.
 */
class TimeZoneAdapter(private val onItemClicked: (TimeZoneItem) -> Unit) :
    ListAdapter<TimeZoneItem, TimeZoneAdapter.ViewHolder>(DiffCallback) {
    private var selectedPosition = 0

    /**
     * ViewHolder class for binding [TimeZoneItem] data to the UI components.
     */
    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
            private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
            private val tvZone = itemView.findViewById<TextView>(R.id.tvZone)
            private val clTimeZone = itemView.findViewById<View>(R.id.clTimeZone)

        /**
         * Binds a [TimeZoneItem] to the layout and sets click listener.
         *
         * @param item The item to be displayed in the RecyclerView.
         */
        fun bind(item: TimeZoneItem, position: Int) {
            tvTime.text = item.time
            tvZone.text = item.timeZone
            itemView.setOnSoundClickListener(itemView.context) {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onItemClicked(item)
            }
            val isSelected = selectedPosition == position
            updateSelectionState(isSelected, itemView.context)
        }

        private fun updateSelectionState(isSelected: Boolean, context: Context) {
            if (isSelected) {
                clTimeZone.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.activeSelectionRed
                    )
                )
                tvZone.setTextColor(ContextCompat.getColor(context, R.color.white))
                tvTime.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                clTimeZone.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.transparent)
                )
                tvZone.setTextColor(ContextCompat.getColor(context, R.color.unSelected))
                tvTime.setTextColor(ContextCompat.getColor(context, R.color.unSelected))
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_zone,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    /**
     * [DiffUtil.ItemCallback] implementation for optimizing item updates in the list.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<TimeZoneItem>() {

        override fun areItemsTheSame(oldItem: TimeZoneItem, newItem: TimeZoneItem): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: TimeZoneItem, newItem: TimeZoneItem): Boolean =
            oldItem.timeZone == newItem.timeZone
    }
}

