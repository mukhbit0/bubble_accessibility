package com.ionicerrrrscode.bubble_accessibility

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Shared bubble drawing logic used by both [BubbleAccessibilityService]
 * (TYPE_ACCESSIBILITY_OVERLAY) and [BubbleForegroundService]
 * (TYPE_APPLICATION_OVERLAY).
 */
class BubbleController(
    private val context: Context,
    private val windowType: Int,
) {

    private var windowManager: WindowManager? = null
    private var bubbleView: View? = null

    private val handler = Handler(Looper.getMainLooper())
    private var autoOpenRunnable: Runnable? = null

    /** Called (on main thread) when the user taps the bubble. */
    var onTap: (() -> Unit)? = null

    /**
     * Called (on main thread) when a notification from the host app arrives
     * while the bubble is visible. Carries the notification title and text.
     */
    var onNotification: ((title: String?, text: String?) -> Unit)? = null

    // ── Public API ────────────────────────────────────────────────────────

    fun show() {
        handler.post {
            if (bubbleView != null) return@post

            val config = BubbleConfig.current
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            bubbleView = LayoutInflater.from(context)
                .inflate(R.layout.bubble_accessibility_layout, null)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = config.initialX
                y = config.initialY
            }

            applyConfig(config)
            setupInteractions(params, config)

            windowManager?.addView(bubbleView, params)
        }
    }

    fun hide() {
        handler.post { close() }
    }

    /**
     * Called by the service when a notification from the host app arrives.
     * Fires [onNotification], then auto-opens the app (if enabled in config).
     */
    fun handleNotification(title: String?, text: String?) {
        handler.post {
            onNotification?.invoke(title, text)

            if (bubbleView == null) return@post
            if (!BubbleConfig.current.autoOpenOnNotification) return@post

            cancelAutoOpen()
            autoOpenRunnable = Runnable { launchApp(title, text) }
            handler.postDelayed(autoOpenRunnable!!, BubbleConfig.current.autoOpenDelayMs)
        }
    }

    // ── Private ───────────────────────────────────────────────────────────

    private fun applyConfig(config: BubbleConfig) {
        val v = bubbleView ?: return
        val density = context.resources.displayMetrics.density
        val sizePx = (config.sizeDp * density).roundToInt()

        val bubbleMain = v.findViewById<FrameLayout>(R.id.bubble_main)
        val iconView   = v.findViewById<ImageView>(R.id.bubble_icon)
        val closeBtn   = v.findViewById<ImageView>(R.id.bubble_close)

        // Size
        bubbleMain.layoutParams = bubbleMain.layoutParams.also {
            it.width = sizePx; it.height = sizePx
        }

        // Colors
        bubbleMain.background?.setTint(config.backgroundColor)
        closeBtn.background?.setTint(config.closeButtonColor)
        closeBtn.visibility = if (config.showCloseButton) View.VISIBLE else View.GONE

        // Icon
        if (config.iconDrawableName != null) {
            val resId = context.resources.getIdentifier(
                config.iconDrawableName, "drawable", context.packageName
            )
            if (resId != 0) iconView.setImageResource(resId)
        } else {
            try {
                iconView.setImageDrawable(
                    context.packageManager.getApplicationIcon(context.packageName)
                )
            } catch (_: Exception) { /* keep XML default */ }
        }
    }

    private fun setupInteractions(params: WindowManager.LayoutParams, config: BubbleConfig) {
        val v = bubbleView ?: return
        val bubbleMain = v.findViewById<FrameLayout>(R.id.bubble_main)
        val closeBtn   = v.findViewById<ImageView>(R.id.bubble_close)

        var ix = 0; var iy = 0; var tx = 0f; var ty = 0f

        bubbleMain.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    ix = params.x; iy = params.y
                    tx = event.rawX; ty = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = ix + (event.rawX - tx).toInt()
                    params.y = iy + (event.rawY - ty).toInt()
                    windowManager?.updateViewLayout(bubbleView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (abs(event.rawX - tx) < 10f && abs(event.rawY - ty) < 10f) {
                        cancelAutoOpen()
                        onTap?.invoke()
                        launchApp(null, null)
                    }
                    true
                }
                else -> false
            }
        }

        closeBtn.setOnClickListener { close() }
    }

    private fun launchApp(title: String?, text: String?) {
        val config = BubbleConfig.current

        val intent = if (config.tapIntentAction != null) {
            Intent(config.tapIntentAction).apply { `package` = context.packageName }
        } else {
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("notification_title", title)
            putExtra("notification_text", text)
            config.tapIntentExtras.forEach { (k, v) -> putExtra(k, v) }
            if (config.tapIntentRoute != null) putExtra("route", config.tapIntentRoute)
        }

        context.startActivity(intent)
        close()
    }

    private fun cancelAutoOpen() {
        autoOpenRunnable?.let { handler.removeCallbacks(it) }
        autoOpenRunnable = null
    }

    private fun close() {
        cancelAutoOpen()
        bubbleView?.let { v ->
            try { windowManager?.removeView(v) } catch (_: Exception) {}
        }
        bubbleView = null
    }
}
