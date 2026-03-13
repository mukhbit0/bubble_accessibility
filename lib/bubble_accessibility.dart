import 'package:flutter/services.dart';

import 'src/bubble_config.dart';
import 'src/bubble_mode.dart';
import 'src/bubble_notification.dart';

export 'src/bubble_config.dart';
export 'src/bubble_intent.dart';
export 'src/bubble_mode.dart';
export 'src/bubble_notification.dart';
export 'src/bubble_position.dart';

/// Controls the floating bubble overlay shown when your app is backgrounded.
///
/// **Minimal setup — just add the dependency, nothing else is required.**
/// The bubble appears/disappears automatically based on the app lifecycle.
///
/// ### Quick start
/// ```dart
/// void main() async {
///   WidgetsFlutterBinding.ensureInitialized();
///
///   await BubbleAccessibility.configure(BubbleConfig(
///     sizeDp: 62,
///     backgroundColor: 0xFF2196F3,
///     mode: BubbleMode.accessibilityService,
///     autoOpenOnNotification: false,  // handle opens yourself
///   ));
///
///   // Listen to incoming notifications
///   BubbleAccessibility.onNotification.listen((n) {
///     print('Got notification: ${n.title} — ${n.text}');
///   });
///
///   runApp(MyApp());
/// }
/// ```
class BubbleAccessibility {
  static const MethodChannel _channel = MethodChannel('bubble_accessibility');

  static Stream<BubbleNotification>? _notificationStream;
  static Stream<void>? _tapStream;

  BubbleAccessibility._();

  // ── Configuration ─────────────────────────────────────────────────────────

  /// Applies [config] to the bubble. Call once before [runApp].
  /// Safe to call multiple times — the latest config is always used.
  static Future<void> configure([BubbleConfig config = const BubbleConfig()]) =>
      _channel.invokeMethod('configure', config.toMap());

  // ── Permission helpers ────────────────────────────────────────────────────

  /// `true` if the user has enabled the Accessibility service.
  static Future<bool> get isAccessibilityEnabled async =>
      await _channel.invokeMethod<bool>('isAccessibilityEnabled') ?? false;

  /// `true` if the "Display over other apps" (SYSTEM_ALERT_WINDOW) permission
  /// has been granted — required for [BubbleMode.foregroundService].
  static Future<bool> get isOverlayPermissionGranted async =>
      await _channel.invokeMethod<bool>('isOverlayPermissionGranted') ?? false;

  /// `true` if the current [BubbleMode] has the required permission granted.
  static Future<bool> get isEnabled async =>
      await _channel.invokeMethod<bool>('isEnabled') ?? false;

  /// Opens the relevant settings screen based on the configured [BubbleMode].
  static Future<void> openSettings() => _channel.invokeMethod('openSettings');

  /// Opens Android Accessibility Settings directly.
  static Future<void> openAccessibilitySettings() =>
      _channel.invokeMethod('openAccessibilitySettings');

  /// Opens the "Display over other apps" settings screen.
  static Future<void> openOverlaySettings() =>
      _channel.invokeMethod('openOverlaySettings');

  // ── Manual control ────────────────────────────────────────────────────────

  /// Manually show the bubble (respects the configured [BubbleMode]).
  static Future<void> show() => _channel.invokeMethod('show');

  /// Manually hide the bubble.
  static Future<void> hide() => _channel.invokeMethod('hide');

  // ── Streams ───────────────────────────────────────────────────────────────

  /// Fires whenever a notification from your app arrives while the bubble
  /// is on screen. Contains the notification title, text and extras.
  ///
  /// Use this together with `autoOpenOnNotification: false` to decide
  /// yourself whether and how to open the app.
  ///
  /// ```dart
  /// BubbleAccessibility.onNotification.listen((n) {
  ///   if (n.extras['type'] == 'chat') navigateTo('/chat');
  /// });
  /// ```
  static Stream<BubbleNotification> get onNotification {
    _notificationStream ??=
        const EventChannel('bubble_accessibility/notifications')
            .receiveBroadcastStream()
            .map(BubbleNotification.fromMap);
    return _notificationStream!;
  }

  /// Fires whenever the user taps the bubble (before the app is opened).
  static Stream<void> get onBubbleTap {
    _tapStream ??=
        const EventChannel('bubble_accessibility/tap')
            .receiveBroadcastStream()
            .map((_) {});
    return _tapStream!;
  }
}
