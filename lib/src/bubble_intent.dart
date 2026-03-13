/// Describes the Android Intent fired when the bubble is tapped or
/// a notification auto-opens the app.
class BubbleIntent {
  const BubbleIntent({
    this.action,
    this.extras = const {},
    this.route,
  });

  /// Android Intent action string.
  /// `null` (default) uses `getLaunchIntentForPackage` — i.e. the normal
  /// app launch intent.
  final String? action;

  /// Arbitrary String key/value extras added to the intent.
  final Map<String, String> extras;

  /// Shorthand for passing a Flutter route: added as the `'route'` intent
  /// extra so your app can navigate on launch.
  /// e.g. `route: '/chat'` → `intent.putExtra("route", "/chat")`
  final String? route;

  Map<String, dynamic> toMap() => {
        if (action != null) 'action': action,
        'extras': extras,
        if (route != null) 'route': route,
      };
}
