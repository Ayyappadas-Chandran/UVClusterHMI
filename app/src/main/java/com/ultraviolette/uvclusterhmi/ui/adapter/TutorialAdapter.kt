package com.ultraviolette.uvclusterhmi.ui.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.TutorialVideoInfo
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener

class TutorialAdapter(
    private val onSelectVideoPosition: (Int) -> Unit
) : ListAdapter<TutorialVideoInfo, TutorialAdapter.ViewHolder>(DiffCallback) {
    private var selectedVideoPosition: Int = 0
    private var isClicked = false

    /**
     * Updates the selected video position and notifies item changes.
     */
    fun updateSelectedPosition(newPosition: Int) {
        if(!isClicked){
            val previousPosition = selectedVideoPosition
            selectedVideoPosition = newPosition
            notifyItemChanged(selectedVideoPosition)
            notifyItemChanged(previousPosition)
        }
    }


    /**
     * [ViewHolder] holds and binds the views defined in [R.layout.item_tutorial].
     * It handles the click listener and selection UI logic.
     *
     * @param  itemView for the tutorial item layout.
     */
    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
            private val tvVideoName = itemView.findViewById<TextView>(R.id.tvVideoName)
            private val tvVideoTime = itemView.findViewById<TextView>(R.id.tvVideoTime)
            private val cvVideo = itemView.findViewById<MaterialCardView>(R.id.cvVideo)
            private val ivSelectionLeft = itemView.findViewById<ImageView>(R.id.ivSelectionLeft)
            private val ivSelectionRight = itemView.findViewById<ImageView>(R.id.ivSelectionRight)
            private val ivPlay = itemView.findViewById<ImageView>(R.id.ivPlay)


        /**
         * Binds a [TutorialVideoInfo] to the view and manages selection state.
         *
         * @param item The tutorial item data.
         * @param position The position of the item in the list.
         */
        fun bind(item: TutorialVideoInfo, position: Int) {
            val context = itemView.context
            tvVideoName.text = item.videoName
            tvVideoTime.text = item.videoDuration
            /*itemView.setOnSoundClickListener(context) {
                isClicked = true
                val previousPosition = selectedVideoPosition
                selectedVideoPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedVideoPosition)
            }*/
            val isSelected = selectedVideoPosition == position
            updateSelectionState(isSelected, position, context)
        }

        /**
         * Updates the visual UI elements of the item based on selection state.
         *
         * @param isSelected Whether the current item is selected.
         * @param position The position of the item.
         * @param context Context for accessing resources.
         */
        private fun updateSelectionState(isSelected: Boolean, position: Int, context: Context) {
            if (isSelected) {
                val isLeftEdge = position % 3 == 0
                d("SelectedPosition", "Selected position: $position leftVisible:$isLeftEdge")
                val isRightEdge = (position + 1) % 3 == 0
                ivSelectionLeft.isVisible = position != 0 && !isLeftEdge
                ivSelectionRight.isVisible = position != itemCount - 1 && !isRightEdge
                cvVideo.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.activeSelectionRed)
                )
                tvVideoTime.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.activeSelectionRed
                    )
                )
                tvVideoName.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.activeSelectionRed
                    )
                )
                cvVideo.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT))
                ivPlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.ic_play_white
                    )
                )
                if(isClicked) onSelectVideoPosition(position)
            } else {
                ivSelectionLeft.isVisible = false
                ivSelectionRight.isVisible = false
                cvVideo.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.grey)
                )
                cvVideo.setStrokeColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            R.color.unSelected
                        )
                    )
                )
                tvVideoName.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.unSelected
                    )
                )
                tvVideoTime.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.unSelected
                    )
                )
                ivPlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.ic_play_music
                    )
                )
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tutorial, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position), position)
    }


    /**
     * [DiffCallback] is used by [ListAdapter] to efficiently update the list using [DiffUtil].
     */
    companion object DiffCallback : DiffUtil.ItemCallback<TutorialVideoInfo>() {
        override fun areItemsTheSame(
            oldItem: TutorialVideoInfo,
            newItem: TutorialVideoInfo
        ): Boolean = oldItem.videoName == newItem.videoName

        override fun areContentsTheSame(
            oldItem: TutorialVideoInfo,
            newItem: TutorialVideoInfo
        ): Boolean = oldItem == newItem

    }

}
