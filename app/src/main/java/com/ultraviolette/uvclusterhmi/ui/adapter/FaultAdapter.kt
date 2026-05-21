package com.ultraviolette.uvclusterhmi.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.FaultItem
import com.ultraviolette.uvclusterhmi.domain.dataModel.Severity

class FaultAdapter :
    ListAdapter<FaultItem, FaultAdapter.ViewHolder>(FaultDiffCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Find TextView once during ViewHolder creation
        val tvFault: TextView = itemView.findViewById(R.id.tv_fault)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layout manually (no binding)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fault, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.tvFault.text = item.message

        // Set background based on severity
        when (item.severity) {
            Severity.ERROR -> holder.tvFault.setBackgroundResource(R.drawable.bg_fault_critical)
            Severity.WARNING  -> holder.tvFault.setBackgroundResource(R.drawable.bg_fault_warning)
            else              -> holder.tvFault.setBackgroundResource(R.drawable.bg_fault_critical)
        }
    }
}

/**
 * DiffUtil callback for efficient list diffing
 */
class FaultDiffCallback : DiffUtil.ItemCallback<FaultItem>() {

    override fun areItemsTheSame(oldItem: FaultItem, newItem: FaultItem): Boolean {
        return oldItem.message == newItem.message
        // Alternative: if you have faultCode/id → use oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FaultItem, newItem: FaultItem): Boolean {
        return oldItem == newItem
    }
}
