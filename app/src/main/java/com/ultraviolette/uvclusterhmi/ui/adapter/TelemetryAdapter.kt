package com.ultraviolette.uvclusterhmi.ui.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.TelemetryItem

class TelemetryAdapter :
    ListAdapter<TelemetryItem, TelemetryAdapter.ViewHolder>(TelemetryDiffCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLabel: TextView = itemView.findViewById(R.id.tv_label)
        val tvValue: TextView = itemView.findViewById(R.id.tv_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_telemetry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.tvLabel.text = item.label
        holder.tvValue.text = item.value

        if (item.isBig) {
            holder.tvValue.textSize = 24f
            holder.tvValue.setTypeface(null, Typeface.BOLD)
        } else {
            holder.tvValue.textSize = 17f
            holder.tvValue.setTypeface(null, Typeface.NORMAL)
        }
    }
}

/**
 * DiffUtil callback — decides what changed between old and new lists
 */
class TelemetryDiffCallback : DiffUtil.ItemCallback<TelemetryItem>() {

    override fun areItemsTheSame(oldItem: TelemetryItem, newItem: TelemetryItem): Boolean {
        return oldItem.label == newItem.label   // label is unique (SPEED, RPM, etc.)
    }

    override fun areContentsTheSame(oldItem: TelemetryItem, newItem: TelemetryItem): Boolean {
        return oldItem == newItem   // works because TelemetryItem is a data class
    }
}
