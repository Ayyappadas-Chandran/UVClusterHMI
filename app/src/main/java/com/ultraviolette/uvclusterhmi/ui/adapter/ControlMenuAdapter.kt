package com.ultraviolette.uvclusterhmi.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
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
class ControlMenuAdapter(private val onSelectedPosition: (Int) -> Unit) :
    ListAdapter<String, ControlMenuAdapter.ViewHolder>(DiffCallback) {
    private var selectedPosition: Int = 0
    private var isClicked = false
    fun updateSelectedPosition(newPosition: Int) {
        notifyItemChanged(selectedPosition)
        selectedPosition = newPosition
        notifyItemChanged(selectedPosition)
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

        val selectionLeft: ImageView = itemView.findViewById(R.id.selectionLeft)
        val ivSelectionRight: ImageView = itemView.findViewById(R.id.ivSelectionRight)

        val selectionRight: ImageView = itemView.findViewById(R.id.selectionRight)
        val ivControls: ImageView = itemView.findViewById(R.id.ivControls)

        /**
         * Binds a control item to the view holder and updates UI based on the selection state.
         *
         * @param item The control title text.
         * @param position The adapter position of the item.
         */
        fun bind(item: String, position: Int) {
            itemView.clearAnimation()
            ivSelectionLeft.clearAnimation()
            selectionLeft.clearAnimation()
            ivSelectionRight.clearAnimation()
            selectionRight.clearAnimation()
            val context = itemView.context
            tvControlsTitle.text = item
            itemView.setOnSoundClickListener(context) {
                isClicked = true
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }
            val isSelected = selectedPosition == position
            ivSelectionLeft.isVisible = false
            selectionLeft.isVisible = false
            ivSelectionRight.isVisible = false
            selectionRight.isVisible = false
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
                handleItemSelection()
                ivSelectionLeft.isVisible = position != 0
                selectionLeft.isVisible = position != 0
                ivSelectionRight.isVisible = position != itemCount - 1
                selectionRight.isVisible = position != itemCount - 1
                if (position != 0) {
                    val leftIvAnimation = loadAnimation(itemView.context, R.anim.slide_in_left)
                    leftIvAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            if (isClicked) {
                                isClicked = false
                                onSelectedPosition(position)
                            }
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                        }
                    })
                    val leftAnimation = loadAnimation(itemView.context, R.anim.enlarge_fade_in)
                    leftAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {
                            ivSelectionLeft.startAnimation(leftIvAnimation)

                        }

                        override fun onAnimationEnd(animation: Animation?) {
                        }

                        override fun onAnimationRepeat(animation: Animation?) {

                        }
                    })
                    selectionLeft.startAnimation(leftAnimation)
                }
                if (position != itemCount - 1) {
                    val rightIvAnimation = loadAnimation(itemView.context, R.anim.slide_in_right)
                    rightIvAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            if (isClicked && position == 0) {
                                isClicked = false
                                onSelectedPosition(position)
                            }
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                        }
                    })
                    val rightAnimation = loadAnimation(itemView.context, R.anim.enlarge_fade_in)
                    rightAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {
                            ivSelectionRight.startAnimation(rightIvAnimation)
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                        }

                        override fun onAnimationRepeat(animation: Animation?) {

                        }
                    })
                    selectionRight.startAnimation(rightAnimation)
                }
            } else {
                handleItemUnSelection(context)
                if (!isClicked) return
                ivSelectionLeft.isVisible = position != 0
                selectionLeft.isVisible =  position != 0
                ivSelectionRight.isVisible = position != itemCount - 1
                selectionRight.isVisible = position != itemCount - 1
                if (position != 0) {
                    val leftIvAnimation = loadAnimation(itemView.context, R.anim.slide_out_left)
                    leftIvAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {
                            // ignore
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            selectionLeft.isVisible = false
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                            //ignore
                        }
                    })
                    val leftAnimation = loadAnimation(itemView.context, R.anim.shrink_fade_out)
                    leftAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {
                            ivSelectionLeft.startAnimation(leftIvAnimation)
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            //ignore
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                            //ignore
                        }
                    })
                    selectionLeft.startAnimation(leftAnimation)
                }
                if (position != itemCount - 1) {
                    val rightIvAnimation = loadAnimation(itemView.context, R.anim.slide_out_right)
                    rightIvAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {
                            //ignore
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            selectionRight.isVisible = false
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                            //ignore
                        }
                    })
                    val rightAnimation = loadAnimation(itemView.context, R.anim.shrink_fade_out)
                    rightAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {
                            ivSelectionRight.startAnimation(rightIvAnimation)
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            //ignore
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                            //ignore
                        }
                    })
                    selectionRight.startAnimation(rightAnimation)
                }
            }
        }

        private fun handleItemUnSelection(context: Context) {
            tvControlsTitle.setBackgroundColor(
                ContextCompat.getColor(context, R.color.grey)
            )
            ivControls.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.image_controls)
            )
        }

        private fun handleItemSelection() {
            tvControlsTitle.setBackgroundColor(
                ContextCompat.getColor(itemView.context, R.color.activeSelectionRed)
            )
            ivControls.setImageDrawable(
                ContextCompat.getDrawable(itemView.context, R.drawable.image_control_selection)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_control, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.itemView.clearAnimation()
        holder.ivSelectionLeft.clearAnimation()
        holder.selectionLeft.clearAnimation()
        holder.ivSelectionRight.clearAnimation()
        holder.selectionRight.clearAnimation()
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
