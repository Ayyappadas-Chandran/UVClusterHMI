package com.ultraviolette.uvclusterhmi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class CircularGradientProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 100f)  // optional limit
            invalidate()
        }
    private val strokeWidth = 34f

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@CircularGradientProgress.strokeWidth
        color = Color.parseColor("#BF2E2E2E")
        strokeCap = Paint.Cap.BUTT   // 🔥 Flat edges
    }

    val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidth
        color = Color.RED
        strokeCap = Paint.Cap.BUTT
    }

    val redLineAngle = 4f


    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@CircularGradientProgress.strokeWidth
        strokeCap = Paint.Cap.BUTT   // 🔥 Flat edges
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)



        val size = min(width, height)
        val radius = size / 2f - strokeWidth
        val centerX = width / 2f
        val centerY = height / 2f

        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )


        // Background arc (not full 360 to create gap)
        val totalAngle = 300f
        canvas.drawArc(rect, -90f, totalAngle, false, backgroundPaint)




        // Gradient
        val sweepGradient = SweepGradient(
            centerX,
            centerY,
            intArrayOf(
                Color.parseColor("#2E3FB983"),
                Color.parseColor("#10A362"),
            ),
            null
        )

        progressPaint.shader = sweepGradient

        val sweepAngle = totalAngle * (progress / 100f)

        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)


    }

}

