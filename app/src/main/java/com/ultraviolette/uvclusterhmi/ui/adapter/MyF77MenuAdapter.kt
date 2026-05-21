package com.ultraviolette.uvclusterhmi.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener

/**
 * Adapter class for displaying a list of [String] in a RecyclerView.
 *
 * This adapter is used to show control or Controls options in the UI using a [ListAdapter]
 * for optimized diff-based updates via [DiffUtil].
 */
class MyF77MenuAdapter(private val onSelectedPosition: (Int) -> Unit) :
    ListAdapter<String, MyF77MenuAdapter.ViewHolder>(DiffCallback) {
    private var selectedPosition: Int = 0
    private var isClicked = false

    /**
     * Updates the selected position and notifies item changes.
     */
    fun updateSelectedPosition(newPosition: Int) {
        val previousPosition = selectedPosition
        selectedPosition = newPosition
        notifyItemChanged(selectedPosition)
        notifyItemChanged(previousPosition)
    }

    /**
     * ViewHolder class responsible for binding a [String] to the layout.
     *
     * @property itemView View binding for a single control item.
     */
    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val tvControlsTitle: TextView = itemView.findViewById(R.id.tvControlsTitle)
        val icArrow: ImageView = itemView.findViewById(R.id.icArrow)
        val ivSelectionLeft: ImageView = itemView.findViewById(R.id.ivSelectionLeft)
        val ivSelectionRight: ImageView = itemView.findViewById(R.id.ivSelectionRight)
        val ivControls: ImageView = itemView.findViewById(R.id.ivControls)

        /**
         * Binds a control item to the view holder and updates UI based on the selection state.
         *
         * @param item The control title text.
         * @param position The adapter position of the item.
         */
        fun bind(item: String, position: Int) {
            val context = itemView.context
            tvControlsTitle.text = item
            itemView.setOnSoundClickListener(context) {
                isClicked = true
                selectedPosition = position
                notifyDataSetChanged()
            }
            val isSelected = selectedPosition == position
            updateSelectionState(isSelected, position, context)
        }

        /**
         * Updates the UI elements to reflect selected or unselected state.
         *
         * @param isSelected True if the current item is selected.
         * @param position The position of the current item.
         * @param context The context used for accessing resources.
         */
        private fun updateSelectionState(isSelected: Boolean, position: Int, context: Context) {
            icArrow.isVisible = isSelected
            if (isSelected) {
                handleItemSelection(context)
                ivSelectionLeft.isVisible = position != 0
                ivSelectionRight.isVisible = position != itemCount - 1
                val scaleAnim = AnimationUtils.loadAnimation(
                    itemView.context,
                    R.anim.press_scale
                )
                scaleAnim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {

                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        isClicked = false
                        onSelectedPosition(position)
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                    }
                })
                if (isClicked) {
                    itemView.startAnimation(scaleAnim)
                }
            } else {
                ivSelectionLeft.isVisible = false
                ivSelectionRight.isVisible = false
                handleItemUnselection(context)
                val fadeAnim = AnimationUtils.loadAnimation(itemView.context, R.anim.fade)
                if (isClicked) {
                    itemView.startAnimation(fadeAnim)
                }
            }
        }

        private fun handleItemUnselection(context: Context) {
            tvControlsTitle.setBackgroundColor(
                ContextCompat.getColor(context, R.color.grey)
            )
            ivControls.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.image_controls)
            )
        }

        private fun handleItemSelection(context: Context) {
            ivControls.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.image_control_selection)
            )
            tvControlsTitle.setBackgroundColor(
                ContextCompat.getColor(context, R.color.activeSelectionRed)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_my_f77_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    /**
     * [DiffUtil.ItemCallback] implementation for efficiently detecting item changes.
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
