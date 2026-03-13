/// Which mechanism the plugin uses to display the overlay bubble.
enum BubbleMode {
  /// Uses Android's AccessibilityService.
  /// Requires the user to enable the service in Accessibility Settings.
  /// Works on all API levels.
  accessibilityService,

  /// Uses a foreground Service + SYSTEM_ALERT_WINDOW overlay.
  /// Requires the user to grant the "Display over other apps" permission.
  /// Shows a persistent notification while active.
  foregroundService,

  /// Tries [accessibilityService] first; falls back to [foregroundService]
  /// if the accessibility service is not enabled but overlay permission is.
  auto,
}
