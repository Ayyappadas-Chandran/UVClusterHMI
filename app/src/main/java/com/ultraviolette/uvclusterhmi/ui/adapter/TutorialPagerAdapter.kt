package com.ultraviolette.uvclusterhmi.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R

class TutorialPagerAdapter(
    private val images: List<Int>
) : RecyclerView.Adapter<TutorialPagerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tutorial_page, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.setImageResource(images[position])
    }

    override fun getItemCount() = images.size
}

