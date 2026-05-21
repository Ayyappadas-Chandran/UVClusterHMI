package com.ultraviolette.uvclusterhmi.ui.features.myF77

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R

class ImagePagerAdapter(
    private val images: List<Int>,
    private val onImageClick: () -> Unit
) : RecyclerView.Adapter<ImagePagerAdapter.ImageHolder>() {

    inner class ImageHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imageView)

        init {
            image.setOnClickListener { onImageClick() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dummy_image, parent, false)
        return ImageHolder(view)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.image.setImageResource(images[position])
    }

    override fun getItemCount() = images.size
}

