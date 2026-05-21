package com.ultraviolette.uvclusterhmi.ui.customWidget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.ultraviolette.uvclusterhmi.R

class DiagonalProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val strokeWidth = 6f

    private val bgPaint = Paint().apply {
        val typedValue = TypedValue()
        val resolved = context.theme.resolveAttribute(R.attr.bdpowerbar, typedValue, true)

        color = if (resolved) {
            ContextCompat.getColor(context, typedValue.resourceId)
        } else {
            ContextCompat.getColor(context, R.color.greyDarkMedium)
        }
        this.strokeWidth = this@DiagonalProgressView.strokeWidth
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val progressPaint = Paint().apply {
        val typedValue = TypedValue()
        val resolved = context.theme.resolveAttribute(R.attr.modeColor, typedValue, true)
        color = if (resolved) {
            ContextCompat.getColor(context, typedValue.resourceId)
        } else {
            ContextCompat.getColor(context, R.color.glideGreen)
        }
        this.strokeWidth = this@DiagonalProgressView.strokeWidth
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    var progress = 0f // 0f to 1f
        set(value) {
            field = value.coerceIn(0f,1f)
            invalidate()
        }

    /*
        fun setProgress(value: Float) {
            progress = value.coerceIn(0f, 1f)
            invalidate()
        }
    */

    var currentColor: Int = progressPaint.color
        private set

    fun setModeColor(color: Int) {
        if (currentColor == color) return
        currentColor = color
        progressPaint.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = width.coerceAtMost(height).toFloat()
        val offset = strokeWidth / 2

        val startX = offset
        val startY = offset
        val endX = size - offset
        val endY = size - offset

        // Background diagonal line
        canvas.drawLine(startX, startY, endX, endY, bgPaint)

        // Progress line
        val progressX = startX + (endX - startX) * progress
        val progressY = startY + (endY - startY) * progress
        canvas.drawLine(startX, startY, progressX, progressY, progressPaint)
    }
}
