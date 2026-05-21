package com.ultraviolette.uvclusterhmi.ui.customWidget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.ultraviolette.uvclusterhmi.R


class SairaRegularTextView : AppCompatTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        try {
            val sairaTypeFace = ResourcesCompat.getFont(context,R.font.saira_regular)
            typeface = sairaTypeFace
        } catch (e: Exception) {
            e.printStackTrace()
            typeface = Typeface.DEFAULT
        }
    }
}