package com.ultraviolette.uvclusterhmi.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.SettingMenuItem
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener

/**
 * Adapter for displaying a list of [SettingMenuItem] in a settings menu RecyclerView.
 *
 * This adapter supports item selection and highlights the currently selected item.
 * It uses [DiffUtil] for efficient list updates.
 *
 * @param onNewSelection Callback triggered when a menu item is selected,
 * providing the selected position.
 */
class VerticalMenuAdapter(
    private var currentSelected : Int = 0,
    private val onNewSelection: (Int, Int) -> Unit
) :
    ListAdapter<SettingMenuItem, VerticalMenuAdapter.ViewHolder>(DiffCallback) {
    var isShowingChildAdapter = false

    /** Holds the currently selected item's position. */

    private var isChildClicked = false

    fun updateSelectedPosition(newPosition: Int) {
        notifyItemChanged(currentSelected)
        currentSelected = newPosition
        notifyItemChanged(currentSelected)
    }

    fun handleChildClick(isChildClicked: Boolean) {
        this.isChildClicked = isChildClicked
        if (isChildClicked) notifyDataSetChanged() // change all the items in the recycler view
    }

    /**
     * ViewHolder responsible for binding a [SettingMenuItem] to the layout.
     *
     * @property  View binding for a single settings menu item.
     */
    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val cvMenu = itemView.findViewById<View>(R.id.cvMenu)
        private val tvMenuTitle = itemView.findViewById<TextView>(R.id.tvMenuTitle)

        /**
         * Binds a [SettingMenuItem] to the view, applying selected state styling.
         *
         * @param item The setting menu item to bind.
         * @param position The position of the item in the adapter.
         */
        fun bind(item: SettingMenuItem, position: Int) {
            val context = itemView.context
            val isSelected = currentSelected == position
            val bgColor = when {
                isSelected && !isChildClicked -> R.color.activeSelectionRed
                isSelected && isChildClicked -> R.color.white
                else -> R.color.transparent
            }
            val textColor = when {
                isSelected && !isChildClicked -> R.color.white
                isSelected && isChildClicked -> R.color.black
                else -> R.color.unSelected
            }
            cvMenu.setBackgroundColor(ContextCompat.getColor(context, bgColor))
            tvMenuTitle.setTextColor(ContextCompat.getColor(context, textColor))
            tvMenuTitle.text = item.title
            itemView.setOnSoundClickListener(itemView.context) {
                isChildClicked = true
                if (isShowingChildAdapter) return@setOnSoundClickListener
                val earlierPosition = currentSelected
                onNewSelection(item.destination, position)
                currentSelected = position
                notifyItemChanged(earlierPosition)
                notifyItemChanged(currentSelected)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_setting_menu,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    /**
     * [DiffUtil.ItemCallback] for efficient comparison of [SettingMenuItem] objects.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<SettingMenuItem>() {
        override fun areItemsTheSame(
            oldItem: SettingMenuItem,
            newItem: SettingMenuItem
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: SettingMenuItem,
            newItem: SettingMenuItem
        ): Boolean = oldItem.title == newItem.title
    }
}

