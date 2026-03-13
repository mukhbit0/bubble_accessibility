package com.ionicerrrrscode.bubble_accessibility

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

class BubbleAccessibilityService : AccessibilityService() {

    companion object {
        var instance: BubbleAccessibilityService? = null
    }

    private val controller by lazy {
        BubbleController(this, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY).also {
            it.onTap          = { BubbleAccessibilityPlugin.instance?.onBubbleTap() }
            it.onNotification = { title, text ->
                BubbleAccessibilityPlugin.instance?.onNotificationReceived(title, text)
            }
        }
    }

    override fun onServiceConnected() { super.onServiceConnected(); instance = this }
    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
        controller.hide()
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) return
        if (event.packageName?.toString() != packageName) return

        val notification = event.parcelableData as? Notification ?: return
        val extras = notification.extras
        controller.handleNotification(
            extras?.getString(Notification.EXTRA_TITLE),
            extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        )
    }

    fun showBubble() = controller.show()
    fun hideBubble() = controller.hide()
}
