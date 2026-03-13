package com.ionicerrrrscode.bubble_accessibility

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.view.WindowManager
import androidx.core.app.NotificationCompat

/**
 * Foreground service that displays the bubble overlay using
 * TYPE_APPLICATION_OVERLAY (requires SYSTEM_ALERT_WINDOW permission).
 *
 * Use this mode when users prefer not to grant Accessibility permission.
 * A persistent notification is shown while the service runs — this is
 * an Android requirement for foreground services.
 */
class BubbleForegroundService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 7891

        var instance: BubbleForegroundService? = null

        fun start(context: Context) {
            val intent = Intent(context, BubbleForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BubbleForegroundService::class.java))
        }
    }

    private lateinit var controller: BubbleController

    // ── Service lifecycle ─────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        instance = this

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        @Suppress("DEPRECATION")
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        controller = BubbleController(this, overlayType).also {
            it.onTap          = { BubbleAccessibilityPlugin.instance?.onBubbleTap() }
            it.onNotification = { title, text ->
                BubbleAccessibilityPlugin.instance?.onNotificationReceived(title, text)
            }
        }
        controller.show()
    }

    override fun onDestroy() {
        instance = null
        controller.hide()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Notification ───────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val config = BubbleConfig.current
            val channel = NotificationChannel(
                config.persistentNotificationChannelId,
                config.persistentNotificationChannelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val config = BubbleConfig.current
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pi = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, config.persistentNotificationChannelId)
            .setContentTitle(config.persistentNotificationTitle)
            .setContentText(config.persistentNotificationText)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
}
