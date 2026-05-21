package com.ultraviolette.uvclusterhmi.ui.customWidget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import kotlin.math.cos
import kotlin.math.sin

/**
 * [CurvedProgressBarLeft] is a custom view that displays a partial circular progress bar.
 *
 * It draws a fixed-angle arc (80 degrees) with a background stroke and overlays a progress stroke
 * based on the percentage value set via [setProgress].
 *
 * The arc starts at 135° (bottom-left direction) and sweeps clockwise.
 *
 * @constructor Creates a new instance of [CurvedProgressBarLeft] with optional XML attributes and style.
 *
 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
 * @param attrs The attribute set from the XML layout (optional).
 * @param defStyleAttr An optional style resource ID.
 */
class CurvedProgressBarLeft @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Stroke width for both background and progress arcs
    private val strokeWidthValue = 12f

    // Radius of the arc circle
    private val arcRadius = 80f

    private val defaultProgressColor = "#00FFAA".toColorInt()



    // Paint for the background arc (gray arc)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.DKGRAY
        strokeWidth = strokeWidthValue
        strokeCap = Paint.Cap.BUTT
    }

    // Paint for the progress arc (colored arc)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = "#00FFAA".toColorInt()  // Custom turquoise color
        strokeWidth = strokeWidthValue
        strokeCap = Paint.Cap.BUTT
    }

    fun resetProgressTint() {
        progressPaint.color = defaultProgressColor
        invalidate()
    }

    // Progress value (0 to 100)
    private var progress = 80

    /**
     * Updates the progress value and redraws the view.
     *
     * @param value An integer between 0 and 100 representing the progress percentage.
     */
    fun setProgress(value: Int) {
        progress = value.coerceIn(0, 100)
        invalidate() // Request re-draw
    }

    fun setProgressTint(color: Int) {
        progressPaint.color = color
        invalidate()
    }




    /**
     * Called when the view should render its content.
     *
     * @param canvas The canvas on which the background and progress arcs are drawn.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        // Define the bounding rectangle for the arc
        val rect = RectF(
            centerX - arcRadius,
            centerY - arcRadius,
            centerX + arcRadius,
            centerY + arcRadius
        )

        val totalAngle = 80f
        val startAngle = 135f

        // Draw background arc
        canvas.drawArc(rect, startAngle, totalAngle, false, backgroundPaint)

        // Calculate and draw progress arc
        val sweepAngle = totalAngle * progress / 100
        canvas.drawArc(rect, startAngle, sweepAngle, false, progressPaint)

        // ---- Draw 80% marker line ----
        val markerAngle = Math.toRadians((startAngle + (totalAngle * 0.8f)).toDouble())

        val markerOuterRadius = arcRadius + strokeWidthValue / 2
        val markerInnerRadius = arcRadius - strokeWidthValue / 2

        val startX = centerX + markerInnerRadius * cos(markerAngle).toFloat()
        val startY = centerY + markerInnerRadius * sin(markerAngle).toFloat()
        val endX = centerX + markerOuterRadius * cos(markerAngle).toFloat()
        val endY = centerY + markerOuterRadius * sin(markerAngle).toFloat()

        val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        canvas.drawLine(startX, startY, endX, endY, markerPaint)
    }

}
