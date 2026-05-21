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
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener

class LanguageAdapter(private val onLanguageClicked: (String) -> Unit) :
    ListAdapter<String, LanguageAdapter.ViewHolder>(DiffCallback) {
    private var selectedPosition = 0

    /**
     * ViewHolder for binding a single [String] to the layout.
     */
    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)

        /**
         * Binds the data from a [String] to the views.
         *
         * @param item The item to bind.
         */
        fun bind(item: String, position: Int) {
            tvTitle.text = item
            itemView.setOnSoundClickListener(itemView.context) {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onLanguageClicked.invoke(item)
            }
            val isSelected = selectedPosition == position
            updateSelectionState(isSelected, itemView.context)
        }

        private fun updateSelectionState(isSelected: Boolean, context: Context) {
            if (isSelected) {
                tvTitle.apply {
                    setBackgroundColor(ContextCompat.getColor(context, R.color.activeSelectionRed))
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                }
            } else {
                tvTitle.apply {
                    setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                    setTextColor(ContextCompat.getColor(context, R.color.unSelected))
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    /**
     * [DiffUtil.ItemCallback] implementation to efficiently update the list.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean = oldItem == newItem
    }
}
