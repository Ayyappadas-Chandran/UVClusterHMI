package com.ultraviolette.uvclusterhmi.ui.customWidget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.ultraviolette.uvclusterhmi.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random
import androidx.core.content.withStyledAttributes

class DonutProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    /** Number of particles and size range */
    private var particleCount = 200
    private var particleMinSize = 0.75f
    private var particleMaxSize = 1.75f

    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.greenParticle)
        style = Paint.Style.FILL
        alpha = 180
    }

    private val eraserPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val particles = mutableListOf<Particle>()
    private var maxRadius = 0f
    private var innerRadius = 0f

    private var progress: Float = 0f
    private var animatedProgress: Float = 0f

    /**
     * Represents a particle in the circular progress view.
     */
    private data class Particle(
        val angle: Float,
        var radius: Float,
        val minRadius: Float,
        val maxRadius: Float,
        var direction: Int,
        val size: Float
    )

    init {
        // read XML attributes if provided
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.CircularProgressView) {
                particleCount =
                    getInt(R.styleable.CircularProgressView_particleCount, particleCount)
                particleMinSize =
                    getFloat(R.styleable.CircularProgressView_particleMinSize, particleMinSize)
                particleMaxSize =
                    getFloat(R.styleable.CircularProgressView_particleMaxSize, particleMaxSize)
            }
        }

        setLayerType(LAYER_TYPE_HARDWARE, null)
        startAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cx = w / 2f
        val cy = h / 2f
        maxRadius = min(cx, cy) * 0.9f
        innerRadius = maxRadius * 0.4f
        generateParticles()
    }

    /** Generates particles randomly between inner and outer radius. */
    private fun generateParticles() {
        particles.clear()
        repeat(particleCount) {
            val angle = Random.nextFloat() * 360f
            val radius = innerRadius + Random.nextFloat() * (maxRadius - innerRadius)
            val direction = if (Random.nextBoolean()) 1 else -1
            val size = Random.nextFloat() * (particleMaxSize - particleMinSize) + particleMinSize
            particles.add(Particle(angle, radius, innerRadius, maxRadius, direction, size))
        }
    }

    /**
     * Draws the circular progress view.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        // base donut
        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#151e17")
        }
        canvas.drawCircle(cx, cy, maxRadius, basePaint)
        canvas.drawCircle(cx, cy, innerRadius, eraserPaint)

        // progress arc
        if (animatedProgress > 0f) {
            val progressAngle = animatedProgress * 360f - 90f
            val gradient = RadialGradient(
                cx, cy, maxRadius,
                intArrayOf(
                    Color.parseColor("#030603"),
                    Color.parseColor("#145228"),
                    Color.parseColor("#44b87f")
                ),
                floatArrayOf(0.5f, 0.85f, 1f),
                Shader.TileMode.CLAMP
            )
            val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = gradient
                style = Paint.Style.FILL
            }
            val rect = RectF(cx - maxRadius, cy - maxRadius, cx + maxRadius, cy + maxRadius)
            canvas.drawArc(rect, -90f, progressAngle + 90f, true, progressPaint)
            canvas.drawCircle(cx, cy, innerRadius, eraserPaint)
        }

        // draw particles
        particlePaint.alpha = 180
        for (p in particles) {
            val x = cx + p.radius * cos(Math.toRadians(p.angle.toDouble())).toFloat()
            val y = cy + p.radius * sin(Math.toRadians(p.angle.toDouble())).toFloat()
            canvas.drawCircle(x, y, p.size, particlePaint)
        }
    }

    /** Updates particle positions for animation. */
    private fun updateParticles() {
        particles.forEach { p ->
            p.radius += p.direction * (Random.nextFloat() * 0.8f + 0.3f)
            if (p.radius >= p.maxRadius) p.direction = -1
            if (p.radius <= p.minRadius) p.direction = 1
        }
    }

    /** Starts continuous particle animation. */
    private fun startAnimation() {
        post(object : Runnable {
            override fun run() {
                updateParticles()
                invalidate()
                postDelayed(this, 16)
            }
        })
    }

    /**
     * Sets the progress value (0f–1f) and animates the arc.
     * @param value Progress fraction between 0 and 1.
     */
    fun setProgress(value: Float) {
        val targetProgress = value.coerceIn(0f, 1f)
        ValueAnimator.ofFloat(animatedProgress, targetProgress).apply {
            duration = 800
            addUpdateListener { animation ->
                animatedProgress = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
        progress = targetProgress
    }
}