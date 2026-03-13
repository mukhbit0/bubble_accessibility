import 'bubble_intent.dart';
import 'bubble_mode.dart';
import 'bubble_position.dart';

/// Full configuration for the floating bubble.
///
/// All fields are optional — the defaults work out of the box.
class BubbleConfig {
  const BubbleConfig({
    // ── Style ──────────────────────────────────────────────────────────────
    this.sizeDp = 62,
    this.backgroundColor = 0xFFFFFFFF,
    this.iconDrawableName,
    this.closeButtonColor = 0xFFE53935,
    this.showCloseButton = true,
    this.initialPosition = const BubblePosition(),

    // ── Behaviour ──────────────────────────────────────────────────────────
    this.autoOpenOnNotification = true,
    this.autoOpenDelayMs = 1500,
    this.tapIntent,

    // ── Mode ───────────────────────────────────────────────────────────────
    this.mode = BubbleMode.accessibilityService,

    // ── Foreground-service notification (mode = foregroundService / auto) ─
    this.persistentNotificationTitle = 'App is running in background',
    this.persistentNotificationText = 'Tap the bubble to return',
    this.persistentNotificationChannelId = 'bubble_overlay_channel',
    this.persistentNotificationChannelName = 'Bubble Overlay',
  });

  // ── Style ────────────────────────────────────────────────────────────────

  /// Diameter of the bubble in density-independent pixels.
  final int sizeDp;

  /// Fill color of the bubble as ARGB. e.g. `0xFF2196F3` for blue.
  final int backgroundColor;

  /// Name of a drawable resource in your app to use as the icon.
  /// e.g. `'my_logo'` → `R.drawable.my_logo` in the host app.
  /// `null` (default) uses the app's launcher icon.
  final String? iconDrawableName;

  /// Color of the × close button background as ARGB.
  final int closeButtonColor;

  /// Whether to show the × close button. Defaults to `true`.
  final bool showCloseButton;

  /// Where the bubble first appears on screen.
  final BubblePosition initialPosition;

  // ── Behaviour ────────────────────────────────────────────────────────────

  /// If `true` (default), the app opens automatically [autoOpenDelayMs] ms
  /// after a notification arrives while the bubble is on screen.
  /// Set to `false` to handle opens yourself via [BubbleAccessibility.onNotification].
  final bool autoOpenOnNotification;

  /// Milliseconds to wait before auto-opening when a notification arrives.
  final int autoOpenDelayMs;

  /// Intent fired when the bubble is tapped or a notification auto-opens.
  /// `null` uses the system launch intent for the package.
  final BubbleIntent? tapIntent;

  // ── Mode ─────────────────────────────────────────────────────────────────

  /// Which mechanism to use for the overlay. See [BubbleMode].
  final BubbleMode mode;

  // ── Foreground-service notification ──────────────────────────────────────

  /// Title of the persistent notification shown in foreground-service mode.
  final String persistentNotificationTitle;

  /// Body text of the persistent notification.
  final String persistentNotificationText;

  /// Android notification channel ID used in foreground-service mode.
  final String persistentNotificationChannelId;

  /// Android notification channel name shown in system settings.
  final String persistentNotificationChannelName;

  Map<String, dynamic> toMap() => {
        'sizeDp': sizeDp,
        'backgroundColor': backgroundColor,
        if (iconDrawableName != null) 'iconDrawableName': iconDrawableName,
        'closeButtonColor': closeButtonColor,
        'showCloseButton': showCloseButton,
        'initialX': initialPosition.x,
        'initialY': initialPosition.y,
        'autoOpenOnNotification': autoOpenOnNotification,
        'autoOpenDelayMs': autoOpenDelayMs,
        if (tapIntent != null) 'tapIntent': tapIntent!.toMap(),
        'mode': mode.index,
        'persistentNotificationTitle': persistentNotificationTitle,
        'persistentNotificationText': persistentNotificationText,
        'persistentNotificationChannelId': persistentNotificationChannelId,
        'persistentNotificationChannelName': persistentNotificationChannelName,
      };
}
