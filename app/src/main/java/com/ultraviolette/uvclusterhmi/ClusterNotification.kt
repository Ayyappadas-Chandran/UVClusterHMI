package com.ultraviolette.uvclusterhmi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import com.google.android.material.card.MaterialCardView
import java.util.ArrayDeque

@SuppressLint("StaticFieldLeak")
object ClusterNotification {

    private const val TAG = "VCU_ALERT_SYSTEM"
    enum class Priority { PENDING, IMMEDIATE }
    enum class Result { SHOWN, QUEUED, SUPPRESSED, IGNORED }
    private enum class State { IDLE, SHOWING, DISMISSING }

    data class Params(
        val heading: String,
        val subtext: String,
        val dismissTimeMs: Long = 5000,
        val priority: Priority = Priority.PENDING,
        val windowType: Int = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        val suppressAboveSpeed: Int = 60
    )

    private var windowManager: WindowManager? = null
    private var rootView: View? = null
    private var appContext: Context? = null
    private val handler = Handler(Looper.getMainLooper())
    private var dismissRunnable: Runnable? = null
    private var state = State.IDLE
    private var currentSpeed = 0
    private val queue: ArrayDeque<Params> = ArrayDeque()

    fun show(context: Context?, params: Params): Result {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post { show(context, params) }
            return Result.SHOWN
        }

        appContext = context?.applicationContext
        val ctx = appContext ?: run {
            Log.e(TAG, "[UI] show() FAILED: Context is NULL")
            return Result.IGNORED
        }

        if (!Settings.canDrawOverlays(ctx)) {
            Log.e(TAG, "[UI] show() FAILED: No Overlay Permission")
            return Result.IGNORED
        }

        if (currentSpeed > params.suppressAboveSpeed && params.priority != Priority.IMMEDIATE) {
            Log.e(TAG, "[UI] SUPPRESSED: Speed $currentSpeed > ${params.suppressAboveSpeed}. Adding to Queue.")
            queue.add(params)
            return Result.SUPPRESSED
        }

        return when (state) {
            State.IDLE -> {
                Log.e(TAG, "[UI] STATE_IDLE -> Executing showInternal for: ${params.heading}")
                showInternal(ctx, params)
                Result.SHOWN
            }
            else -> {
                Log.e(TAG, "[UI] STATE_BUSY ($state) -> Adding to Queue (Total: ${queue.size + 1})")
                if (params.priority == Priority.IMMEDIATE) {
                    queue.addFirst(params)
                    dismiss()
                } else {
                    queue.add(params)
                }
                Result.QUEUED
            }
        }
    }

    fun dismiss() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post { dismiss() }
            return
        }

        Log.e(TAG, "[UI] dismiss() called. State: $state")
        dismissRunnable?.let {
            Log.e(TAG, "[UI] Timer cancelled.")
            handler.removeCallbacks(it)
        }

        if (state == State.SHOWING && rootView != null) {
            animateOut { cleanup() }
        }
    }

    private fun showInternal(context: Context, params: Params) {
        state = State.SHOWING
        Log.e(TAG, "[UI] WindowManager: Adding View for [${params.heading}]")
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val card = buildLayout(context)
        rootView = card

        card.findViewWithTag<TextView>("heading").text = params.heading
        card.findViewWithTag<TextView>("subtext").text = params.subtext

        val bg = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(Color.parseColor("#4B0000"), Color.parseColor("#120000"), Color.BLACK)
        ).apply { cornerRadius = dpToPx(context, 12f).toFloat() }

        card.background = bg
        card.findViewWithTag<View>("dismiss_btn").setOnClickListener {
            Log.e(TAG, "[UI] User manual dismiss clicked")
            dismiss()
        }

        val lp = WindowManager.LayoutParams(
            dpToPx(context, 600f), dpToPx(context, 120f),
            params.windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dpToPx(context, 70f)
        }

        try {
            windowManager?.addView(card, lp)
            animateIn()
            scheduleDismiss(params.dismissTimeMs)
        } catch (e: Exception) {
            Log.e(TAG, "[UI] CRITICAL: WindowManager addView failed", e)
            state = State.IDLE
        }
    }

    private fun buildLayout(ctx: Context): MaterialCardView {
        val themedCtx = ContextThemeWrapper(ctx, com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar)
        val card = MaterialCardView(themedCtx).apply {
            radius = dpToPx(ctx, 10f).toFloat()
            strokeWidth = dpToPx(ctx, 1f)
            strokeColor = Color.parseColor("#33FF0000")
            cardElevation = 0f
        }

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(ctx, 28f), dpToPx(ctx, 20f), dpToPx(ctx, 28f), dpToPx(ctx, 20f))
        }

        val textLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        textLayout.addView(TextView(ctx).apply {
            tag = "heading"; textSize = 24f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD)
        })

        textLayout.addView(TextView(ctx).apply {
            tag = "subtext"; textSize = 18f; setTextColor(Color.LTGRAY)
        })

        val dismissBtn = LinearLayout(ctx).apply {
            tag = "dismiss_btn"
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(ctx, 12f), 0, dpToPx(ctx, 4f), 0)

            // Fixed width so it is never squeezed by textLayout's weight
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(ctx, 64f),
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            addView(ImageView(ctx).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                setColorFilter(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(dpToPx(ctx, 24f), dpToPx(ctx, 24f)).also {
                    it.gravity = Gravity.CENTER_HORIZONTAL
                }
            })
            addView(TextView(ctx).apply {
                text = "Dismiss"
                textSize = 12f
                setTextColor(Color.WHITE)
                maxLines = 1
                gravity = Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })
        }

        root.addView(textLayout)
        root.addView(dismissBtn)
        card.addView(root)
        return card
    }
    

    private fun cleanup() {
        Log.e(TAG, "[UI] Cleaning up View.")
        try {
            rootView?.let { windowManager?.removeViewImmediate(it) }
        } catch (e: Exception) { Log.e(TAG, "[UI] Cleanup removal failed", e) }

        rootView = null
        state = State.IDLE

        if (queue.isNotEmpty() && appContext != null) {
            val next = queue.removeFirst()
            Log.e(TAG, "[UI] Popping from queue: ${next.heading}. Remaining: ${queue.size}")
            showInternal(appContext!!, next)
        }
    }

    private fun animateIn() {
        rootView?.apply {
            alpha = 0f; translationY = -40f
            animate().alpha(1f).translationY(0f).setDuration(250).start()
        }
    }

    private fun animateOut(onEnd: () -> Unit) {
        state = State.DISMISSING
        rootView?.animate()?.alpha(0f)?.translationY(-40f)?.setDuration(200)
            ?.withEndAction { onEnd() }?.start()
    }

    private fun scheduleDismiss(delay: Long) {
        Log.e(TAG, "[UI] Auto-dismiss scheduled in ${delay}ms")
        dismissRunnable?.let { handler.removeCallbacks(it) }
        dismissRunnable = Runnable {
            Log.e(TAG, "[UI] Auto-dismiss timer expired.")
            dismiss()
        }
        handler.postDelayed(dismissRunnable!!, delay)
    }

    private fun dpToPx(c: Context, dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.resources.displayMetrics).toInt()
}
