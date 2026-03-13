package com.example.bubble_accessibility

import android.graphics.Color

data class BubbleConfig(
    // ── Style ──────────────────────────────────────────────────────────────
    val sizeDp: Int = 62,
    val backgroundColor: Int = Color.WHITE,
    val iconDrawableName: String? = null,
    val closeButtonColor: Int = Color.parseColor("#E53935"),
    val showCloseButton: Boolean = true,
    val initialX: Int = 100,
    val initialY: Int = 300,

    // ── Behaviour ──────────────────────────────────────────────────────────
    val autoOpenOnNotification: Boolean = true,
    val autoOpenDelayMs: Long = 1500L,
    val tapIntentAction: String? = null,
    val tapIntentExtras: Map<String, String> = emptyMap(),
    val tapIntentRoute: String? = null,

    // ── Mode ───────────────────────────────────────────────────────────────
    val mode: BubbleMode = BubbleMode.ACCESSIBILITY_SERVICE,

    // ── Foreground-service notification ────────────────────────────────────
    val persistentNotificationTitle: String = "App is running in background",
    val persistentNotificationText: String = "Tap the bubble to return",
    val persistentNotificationChannelId: String = "bubble_overlay_channel",
    val persistentNotificationChannelName: String = "Bubble Overlay",
) {
    companion object {
        var current = BubbleConfig()
    }
}
