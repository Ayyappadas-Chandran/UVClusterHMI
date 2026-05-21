package com.ultraviolette.uvclusterhmi.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R

class ThemePagerAdapter(private var themes: List<String>) :
    RecyclerView.Adapter<ThemePagerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTheme = itemView.findViewById<TextView>(R.id.tvTheme)
        fun bind(text: String) {
            tvTheme.text = text
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(themes[position])
    }

    override fun getItemCount(): Int = themes.size
}