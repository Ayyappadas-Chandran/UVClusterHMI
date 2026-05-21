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
 * [CurvedProgressBarRight] is a custom [View] that draws a curved vertical progress bar
 * in the shape of an arc starting from the bottom-right and progressing upward.
 *
 * It is visually similar to [CurvedProgressBarLeft], but its arc is drawn in the
 * opposite direction (counterclockwise, from ~40°).
 *
 * @constructor Creates an instance of the progress bar view with optional XML attributes.
 *
 * @param context The view's context.
 * @param attrs The attribute set from the XML layout (optional).
 * @param defStyleAttr Optional style resource ID.
 */
class CurvedProgressBarRight @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Width of the arc stroke
    private val strokeWidthValue = 12f

    // Radius of the circular arc
    private val arcRadius = 80f

    // Paint for the background arc (static full arc)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.DKGRAY
        strokeWidth = strokeWidthValue
        strokeCap = Paint.Cap.BUTT
    }

    // Paint for the progress arc (dynamic partial arc)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = "#00FFAA".toColorInt() // Teal color
        strokeWidth = strokeWidthValue
        strokeCap = Paint.Cap.BUTT
    }

    // Current progress value (0 to 100)
    private var progress = 80

    /**
     * Updates the progress value and requests a redraw.
     *
     * @param value Progress value in the range 0 to 100.
     */
    fun setProgress(value: Int) {
        progress = value.coerceIn(0, 100)
        invalidate()
    }

    fun setProgressTint(color: Int) {
        progressPaint.color = color
        invalidate()
    }


    /**
     * Draws the background arc and progress arc on the canvas.
     * The arc starts at 40° and moves counterclockwise.
     *
     * @param canvas The canvas on which to draw.
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

        val totalAngle = 80f       // Total arc length
        val startAngle = 40f       // Starting angle in degrees

        // Draw the full background arc (counterclockwise)
        canvas.drawArc(rect, startAngle, -totalAngle, false, backgroundPaint)

        // Draw progress arc
        val sweepAngle = -totalAngle * progress / 100f
        canvas.drawArc(rect, startAngle, sweepAngle, false, progressPaint)

        // ---- Draw 80% marker line ----
        // For counterclockwise, 80% means moving negatively along the arc
        val markerAngle = Math.toRadians((startAngle - (totalAngle * 0.8f)).toDouble())

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
