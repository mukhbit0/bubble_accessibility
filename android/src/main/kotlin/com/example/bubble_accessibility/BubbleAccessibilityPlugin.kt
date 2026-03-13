package com.example.bubble_accessibility

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class BubbleAccessibilityPlugin : FlutterPlugin, MethodChannel.MethodCallHandler {

    companion object {
        var instance: BubbleAccessibilityPlugin? = null
    }

    private lateinit var context: Context
    private lateinit var channel: MethodChannel

    // Streams
    private var notificationSink: EventChannel.EventSink? = null
    private var tapSink: EventChannel.EventSink? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var activeActivities = 0

    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityStarted(activity: Activity) {
            activeActivities++
            if (activeActivities == 1) hideForMode()
        }
        override fun onActivityStopped(activity: Activity) {
            activeActivities--
            if (activeActivities == 0) showForMode()
        }
        override fun onActivityCreated(a: Activity, b: Bundle?) {}
        override fun onActivityResumed(a: Activity) {}
        override fun onActivityPaused(a: Activity) {}
        override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {}
        override fun onActivityDestroyed(a: Activity) {}
    }

    // ── FlutterPlugin ─────────────────────────────────────────────────────

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        instance = this
        context  = binding.applicationContext

        channel = MethodChannel(binding.binaryMessenger, "bubble_accessibility")
        channel.setMethodCallHandler(this)

        EventChannel(binding.binaryMessenger, "bubble_accessibility/notifications")
            .setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(args: Any?, sink: EventChannel.EventSink?) { notificationSink = sink }
                override fun onCancel(args: Any?) { notificationSink = null }
            })

        EventChannel(binding.binaryMessenger, "bubble_accessibility/tap")
            .setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(args: Any?, sink: EventChannel.EventSink?) { tapSink = sink }
                override fun onCancel(args: Any?) { tapSink = null }
            })

        (context as? Application)?.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        instance = null
        channel.setMethodCallHandler(null)
        (context as? Application)?.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    // ── Called by services ────────────────────────────────────────────────

    fun onNotificationReceived(title: String?, text: String?) {
        mainHandler.post {
            notificationSink?.success(mapOf("title" to title, "text" to text, "extras" to emptyMap<String, String>()))
        }
    }

    fun onBubbleTap() {
        mainHandler.post { tapSink?.success(null) }
    }

    // ── MethodChannel ─────────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "configure"                  -> { configure(call.arguments as? Map<*, *>); result.success(null) }
            "isAccessibilityEnabled"     -> result.success(isAccessibilityEnabled())
            "isOverlayPermissionGranted" -> result.success(Settings.canDrawOverlays(context))
            "isEnabled"                  -> result.success(isEnabled())
            "openSettings"               -> { openSettings(); result.success(null) }
            "openAccessibilitySettings"  -> { openAccessibilitySettings(); result.success(null) }
            "openOverlaySettings"        -> { openOverlaySettings(); result.success(null) }
            "show"                       -> { showForMode(); result.success(null) }
            "hide"                       -> { hideForMode(); result.success(null) }
            else                         -> result.notImplemented()
        }
    }

    // ── Configure ─────────────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private fun configure(map: Map<*, *>?) {
        if (map == null) return
        val intentMap  = map["tapIntent"] as? Map<*, *>
        val extrasMap  = intentMap?.get("extras") as? Map<*, *>

        BubbleConfig.current = BubbleConfig(
            sizeDp                       = (map["sizeDp"] as? Int) ?: 62,
            backgroundColor              = (map["backgroundColor"] as? Int) ?: android.graphics.Color.WHITE,
            iconDrawableName             = map["iconDrawableName"] as? String,
            closeButtonColor             = (map["closeButtonColor"] as? Int) ?: Color.parseColor("#E53935"),
            showCloseButton              = (map["showCloseButton"] as? Boolean) ?: true,
            initialX                     = (map["initialX"] as? Int) ?: 100,
            initialY                     = (map["initialY"] as? Int) ?: 300,
            autoOpenOnNotification       = (map["autoOpenOnNotification"] as? Boolean) ?: true,
            autoOpenDelayMs              = ((map["autoOpenDelayMs"] as? Int)?.toLong()) ?: 1500L,
            tapIntentAction              = intentMap?.get("action") as? String,
            tapIntentExtras              = extrasMap
                ?.entries
                ?.mapNotNull { (k, v) -> if (k is String && v is String) k to v else null }
                ?.toMap() ?: emptyMap(),
            tapIntentRoute               = intentMap?.get("route") as? String,
            mode                         = BubbleMode.fromIndex((map["mode"] as? Int) ?: 0),
            persistentNotificationTitle  = (map["persistentNotificationTitle"] as? String)
                ?: "App is running in background",
            persistentNotificationText   = (map["persistentNotificationText"] as? String)
                ?: "Tap the bubble to return",
            persistentNotificationChannelId   = (map["persistentNotificationChannelId"] as? String)
                ?: "bubble_overlay_channel",
            persistentNotificationChannelName = (map["persistentNotificationChannelName"] as? String)
                ?: "Bubble Overlay",
        )
    }

    // ── Show / hide by mode ───────────────────────────────────────────────

    private fun showForMode() {
        when (BubbleConfig.current.mode) {
            BubbleMode.ACCESSIBILITY_SERVICE ->
                BubbleAccessibilityService.instance?.showBubble()

            BubbleMode.FOREGROUND_SERVICE ->
                if (Settings.canDrawOverlays(context)) BubbleForegroundService.start(context)

            BubbleMode.AUTO -> when {
                BubbleAccessibilityService.instance != null ->
                    BubbleAccessibilityService.instance?.showBubble()
                Settings.canDrawOverlays(context) ->
                    BubbleForegroundService.start(context)
            }
        }
    }

    private fun hideForMode() {
        BubbleAccessibilityService.instance?.hideBubble()
        BubbleForegroundService.stop(context)
    }

    // ── Permission helpers ────────────────────────────────────────────────

    private fun isEnabled(): Boolean = when (BubbleConfig.current.mode) {
        BubbleMode.ACCESSIBILITY_SERVICE -> isAccessibilityEnabled()
        BubbleMode.FOREGROUND_SERVICE    -> Settings.canDrawOverlays(context)
        BubbleMode.AUTO ->
            isAccessibilityEnabled() || Settings.canDrawOverlays(context)
    }

    private fun isAccessibilityEnabled(): Boolean {
        val name    = "${context.packageName}/${BubbleAccessibilityService::class.java.canonicalName}"
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return TextUtils.SimpleStringSplitter(':').apply { setString(enabled) }
            .any { it.equals(name, ignoreCase = true) }
    }

    private fun openSettings() {
        when (BubbleConfig.current.mode) {
            BubbleMode.ACCESSIBILITY_SERVICE -> openAccessibilitySettings()
            BubbleMode.FOREGROUND_SERVICE    -> openOverlaySettings()
            BubbleMode.AUTO ->
                if (!isAccessibilityEnabled()) openAccessibilitySettings()
                else openOverlaySettings()
        }
    }

    private fun openAccessibilitySettings() {
        context.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun openOverlaySettings() {
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
