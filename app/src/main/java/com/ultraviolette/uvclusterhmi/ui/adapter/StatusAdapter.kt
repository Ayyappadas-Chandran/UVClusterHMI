package com.ultraviolette.uvclusterhmi.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.Severity
import com.ultraviolette.uvclusterhmi.domain.dataModel.StatusItem

class StatusAdapter :
    ListAdapter<StatusItem, StatusAdapter.ViewHolder>(StatusDiffCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Find the TextView once during ViewHolder creation
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layout manually (no binding)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_status, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.tvStatus.text = item.message

        // Set background based on severity
        when (item.severity) {
            Severity.ERROR -> holder.tvStatus.setBackgroundResource(R.drawable.bg_fault_critical)
            Severity.WARNING  -> holder.tvStatus.setBackgroundResource(R.drawable.bg_fault_warning)
	    Severity.INFO  -> holder.tvStatus.setBackgroundResource(R.drawable.bg_fault_info)
            else              -> holder.tvStatus.setBackgroundResource(R.drawable.bg_fault_critical)
        }
    }
}

/**
 * DiffUtil callback for efficient list updates
 */
class StatusDiffCallback : DiffUtil.ItemCallback<StatusItem>() {

    override fun areItemsTheSame(oldItem: StatusItem, newItem: StatusItem): Boolean {
        return oldItem.message == newItem.message
        // Alternative: if you have a statusCode/id → use oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: StatusItem, newItem: StatusItem): Boolean {
        return oldItem == newItem
    }
}
