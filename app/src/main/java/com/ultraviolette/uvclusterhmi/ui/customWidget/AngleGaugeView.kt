package com.ultraviolette.uvclusterhmi.ui.customWidget

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.ultraviolette.uvclusterhmi.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class AngleGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val arcPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.greyDarkExtraMedium)
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val limitPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.ballisticRed)
        style = Paint.Style.STROKE
        strokeWidth = 2f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val progressIndicator = Paint().apply {
        color = ContextCompat.getColor(context, R.color.arcProgress)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    // Arc with gradient from top to bottom
    private val indicatorPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 16f
        isAntiAlias = true
    }

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(-1f, 1f)
            invalidate()
        }
    var direction = 0
        set(value) {
            field = value.coerceIn(0, 1)
            invalidate()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sizeInPx = (160.53f * resources.displayMetrics.density).toInt()
        setMeasuredDimension(sizeInPx, sizeInPx)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val progressLineLength = 19f
        val radius = min(width, height) / 2f - 6f - progressLineLength / 2
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        // Draw semicircle background
        canvas.drawArc(rect, 180f, 180f, false, arcPaint)

        // Set linear gradient from top to bottom of arc stroke
        val startColor = ContextCompat.getColor(context, R.color.arcProgress)
        val endColor = ContextCompat.getColor(context, R.color.black)
        val linearGradient = LinearGradient(
            0f, cy - radius,
            0f, cy + radius,
            startColor,
            endColor,
            Shader.TileMode.CLAMP
        )
        indicatorPaint.shader = linearGradient

        // Draw the progress arc using this gradient
        val startAngle = 270f
        val sweepAngle = - 90f * progress
        if (progress != 0f) {
            canvas.drawArc(rect, startAngle, sweepAngle, false, indicatorPaint)
        }
        // Draw limit marks
        val markLen = 16f
        drawMarkCenteredOnStroke(canvas, rect, 235f, markLen, limitPaint)
        drawMarkCenteredOnStroke(canvas, rect, 315f, markLen, limitPaint)

        // Draw progress indicator line at top-center
        drawMarkCenteredOnStroke(canvas, rect, 270f, progressLineLength, progressIndicator)


    }

    private fun drawMarkCenteredOnStroke(
        canvas: Canvas,
        rect: RectF,
        angle: Float,
        length: Float,
        paint: Paint
    ) {
        val radius = rect.width() / 2
        val cx = rect.centerX()
        val cy = rect.centerY()
        val rad = Math.toRadians(angle.toDouble())
        val halfLen = length / 2

        val startX = (cx + (radius - halfLen) * cos(rad)).toFloat()
        val startY = (cy + (radius - halfLen) * sin(rad)).toFloat()
        val endX = (cx + (radius + halfLen) * cos(rad)).toFloat()
        val endY = (cy + (radius + halfLen) * sin(rad)).toFloat()

        canvas.drawLine(startX, startY, endX, endY, paint)
    }
}
