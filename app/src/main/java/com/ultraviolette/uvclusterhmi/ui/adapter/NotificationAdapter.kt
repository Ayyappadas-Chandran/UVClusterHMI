package com.ultraviolette.uvclusterhmi.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.domain.dataModel.Notification
import com.ultraviolette.uvclusterhmi.R

class NotificationAdapter :
    ListAdapter<Notification, NotificationAdapter.ViewHolder>(diffCallback) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNotificationTitle = itemView.findViewById<TextView>(R.id.tvNotificationTitle)
        private val tvNotificationMessage =
            itemView.findViewById<TextView>(R.id.tvNotificationMessage)
        private val tvNotificationTime = itemView.findViewById<TextView>(R.id.tvNotificationTime)
        fun bind(item: Notification) {
            tvNotificationTitle.text = item.title
            tvNotificationMessage.text = item.message
            tvNotificationTime.text = item.time
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))

    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Notification>() {
            override fun areItemsTheSame(
                oldItem: Notification,
                newItem: Notification
            ): Boolean = oldItem.time == newItem.time


            override fun areContentsTheSame(
                oldItem: Notification,
                newItem: Notification
            ): Boolean = oldItem == newItem
        }
    }
}