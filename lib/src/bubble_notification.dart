/// Notification payload delivered via [BubbleAccessibility.onNotification].
class BubbleNotification {
  const BubbleNotification({this.title, this.text, this.extras = const {}});

  /// Notification title.
  final String? title;

  /// Notification body text.
  final String? text;

  /// Additional key/value pairs from the notification extras.
  final Map<String, String> extras;

  factory BubbleNotification.fromMap(dynamic raw) {
    final map = Map<String, dynamic>.from(raw as Map);
    final rawExtras = map['extras'];
    final extras = rawExtras is Map
        ? Map<String, String>.from(
            rawExtras.map((k, v) => MapEntry(k.toString(), v.toString())))
        : const <String, String>{};
    return BubbleNotification(
      title: map['title'] as String?,
      text: map['text'] as String?,
      extras: extras,
    );
  }

  @override
  String toString() =>
      'BubbleNotification(title: $title, text: $text, extras: $extras)';
}
