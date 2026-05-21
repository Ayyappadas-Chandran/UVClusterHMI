package com.ultraviolette.uvclusterhmi.ui.features.myF77

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView

object ImageLoader {

    fun loadFromRaw(context: Context, resId: Int, imageView: ImageView) {
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
        }

        val input = context.resources.openRawResource(resId)
        val bitmap = BitmapFactory.decodeStream(input, null, options)
        input.close()

        imageView.setImageBitmap(bitmap)
    }
}
