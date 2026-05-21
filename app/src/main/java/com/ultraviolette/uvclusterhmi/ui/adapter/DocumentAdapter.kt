package com.ultraviolette.uvclusterhmi.ui.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.utils.Utilities.IMAGE_LANDSCAPE
import com.ultraviolette.uvclusterhmi.utils.Utilities.IMAGE_PORTRAIT

/**
 * Adapter for displaying a list of document images with zoom functionality.
 *
 * @param context The context used for resource access.
 * @param images A list of drawable resource IDs representing the images.
 * @param zoomedImageIndex A lambda to handle zooming when a specific gesture is detected.
 */
class DocumentAdapter(
    private val context: Context,
    @DrawableRes val images: List<Int>,
    val zoomedImageIndex: (Int) -> Unit
) : RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {

    /**
     * ViewHolder class for holding the image view.
     */
    inner class DocumentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.zoomImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_document_details, parent, false)
        return DocumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val imageResId = images[position]
        holder.imageView.setImageResource(imageResId)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, imageResId, options)
        val isPortrait = options.outHeight > options.outWidth

        holder.itemView.tag = if (isPortrait) IMAGE_PORTRAIT else IMAGE_LANDSCAPE
        /*holder.itemView.setOnClickListener {
            val index = position.getIndex(isPortrait)
            if (index in images.indices) {
                zoomedImageIndex.invoke(images[index])
            }
        }*/
        holder.itemView.setOnTouchListener { v, event ->
            if (event.pointerCount > 1 && event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                val index = position.getIndex(isPortrait)
                if (index in images.indices) zoomedImageIndex.invoke(images[index])
                v.performClick()
                return@setOnTouchListener true
            }
            false
        }
    }

    /** If the current image is in portrait orientation,
       we adjust the index by -1. This is because the ViewPager is set up with horizontal
       padding and a carousel-like layout, where portrait images are visually centered
       starting from the second adapter position. */
    private fun Int.getIndex(isPortrait: Boolean) : Int = if (isPortrait && this > 0) this - 1 else this

    override fun getItemCount(): Int = images.size
}
