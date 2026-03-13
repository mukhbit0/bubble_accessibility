# bubble_accessibility

[![pub version](https://img.shields.io/pub/v/bubble_accessibility.svg)](https://pub.dev/packages/bubble_accessibility)
[![license: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![platform: android](https://img.shields.io/badge/platform-android-green.svg)](https://developer.android.com)

A Flutter plugin that shows a **draggable floating bubble** when your app is backgrounded or killed.
Tap the bubble to re-open the app. Incoming notifications stream through to Dart so you can decide exactly what to do.

Works on **all Android versions ≥ 5.0** (API 21+) via two independent permission modes:

| Mode | Permission needed | Works without user granting the other |
|---|---|---|
| `accessibilityService` | Accessibility Service | ✅ |
| `foregroundService` | Display over other apps | ✅ |
| `auto` *(default)* | Either one | ✅ uses whichever is available |

---

## Features

- 🫧 Draggable, styled bubble with configurable size, colour, and icon
- ⚡ Zero host-app boilerplate — manifest and Kotlin merge automatically
- 🔔 Stream notification payloads (title, text, extras) to Dart
- 🖱️ Stream bubble tap events to Dart
- 🛣️ Custom tap intents: open a specific route or send any Android intent
- ⏱️ Optional auto-open with configurable delay on notification arrival
- 🔧 Two independent permission modes (accessibility or foreground service)
- 📦 Works on Android 5.0+ (API 21+)

---

## Installation

```yaml
dependencies:
  bubble_accessibility: ^0.1.1
```

That's it. No Android file edits, no `AndroidManifest.xml` changes, no Kotlin.

---

## Quick Start

```dart
void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await BubbleAccessibility.configure(); // defaults are fine

  runApp(MyApp());
}
```

Then ask the user to enable the permission (once):

```dart
// Check and prompt
final enabled = await BubbleAccessibility.isEnabled;
if (!enabled) {
  await BubbleAccessibility.openSettings(); // opens the right screen automatically
}
```

The bubble appears as soon as the user presses Home or switches apps, and disappears when they return.

---

## Configuration

Call `configure()` once before `runApp` (calling it again at any time updates live config):

```dart
await BubbleAccessibility.configure(
  const BubbleConfig(
    // ── Permission mode ──────────────────────────────────────────────
    mode: BubbleMode.auto,           // auto | accessibilityService | foregroundService

    // ── Style ────────────────────────────────────────────────────────
    sizeDp: 62,
    backgroundColor: 0xFF2196F3,     // ARGB colour int
    iconDrawableName: null,          // null = app launcher icon; or "my_drawable"
    closeButtonColor: 0xFFE53935,
    showCloseButton: true,
    initialPosition: BubblePosition(x: 20, y: 300),

    // ── Notification auto-open ────────────────────────────────────────
    autoOpenOnNotification: true,    // open app when notification arrives
    autoOpenDelayMs: 1500,           // delay before opening (ms)

    // ── Custom tap intent ─────────────────────────────────────────────
    tapIntent: BubbleIntent(
      route: '/home',                // passed as 'route' extra
      extras: {'tab': 'chat'},       // any additional String extras
    ),

    // ── Foreground-service status-bar notification ────────────────────
    persistentNotificationTitle: 'My App',
    persistentNotificationText: 'Running in background',
    persistentNotificationChannelId: 'my_bubble_channel',
    persistentNotificationChannelName: 'Bubble Overlay',
  ),
);
```

---

## Receiving Notifications

Listen to `BubbleAccessibility.onNotification` to receive notification payloads while your app is backgrounded:

```dart
BubbleAccessibility.onNotification.listen((BubbleNotification n) {
  print('${n.title} — ${n.text}');

  // Route based on data
  if (n.extras['type'] == 'chat') {
    openChatScreen();
  }
});
```

Set `autoOpenOnNotification: false` to handle navigation yourself instead of auto-opening.

---

## Bubble Tap Events

```dart
BubbleAccessibility.onBubbleTap.listen((_) {
  print('User tapped the bubble');
});
```

---

## Manual Control

```dart
await BubbleAccessibility.show(); // show manually
await BubbleAccessibility.hide(); // hide manually
```

---

## Permissions

### Mode: `accessibilityService` (no overlay needed)

1. User opens **Settings → Accessibility → Downloaded apps → Your App**.
2. Toggles the service on.

```dart
await BubbleAccessibility.openAccessibilitySettings();
```

### Mode: `foregroundService` (overlay permission)

1. User opens **Settings → Apps → Your App → Display over other apps**.
2. Toggles it on.

```dart
await BubbleAccessibility.openOverlaySettings();
```

### Mode: `auto` (recommended)

Opens the appropriate screen automatically:

```dart
await BubbleAccessibility.openSettings();
```

### Check permissions at runtime

```dart
final accOk     = await BubbleAccessibility.isAccessibilityEnabled;
final overlayOk = await BubbleAccessibility.isOverlayPermissionGranted;
final anyOk     = await BubbleAccessibility.isEnabled; // respects current mode
```

---

## API Reference

### `BubbleAccessibility`

| Method / Property | Description |
|---|---|
| `configure([BubbleConfig])` | Apply configuration. Safe to call multiple times. |
| `isEnabled` | `true` if the required permission for `mode` is granted. |
| `isAccessibilityEnabled` | `true` if Accessibility Service is enabled. |
| `isOverlayPermissionGranted` | `true` if SYSTEM_ALERT_WINDOW is granted. |
| `openSettings()` | Open the correct settings screen for the current mode. |
| `openAccessibilitySettings()` | Open Accessibility Settings directly. |
| `openOverlaySettings()` | Open "Display over other apps" directly. |
| `show()` | Manually show the bubble. |
| `hide()` | Manually hide the bubble. |
| `onNotification` | `Stream<BubbleNotification>` — notification payloads. |
| `onBubbleTap` | `Stream<void>` — fires on each bubble tap. |

### `BubbleConfig`

| Field | Type | Default | Description |
|---|---|---|---|
| `mode` | `BubbleMode` | `auto` | Permission/service mode. |
| `sizeDp` | `int` | `62` | Bubble diameter in dp. |
| `backgroundColor` | `int` | `0xFFFFFFFF` | Bubble background ARGB colour. |
| `iconDrawableName` | `String?` | `null` | Drawable name in host app; `null` = launcher icon. |
| `closeButtonColor` | `int` | `0xFFE53935` | Close-button background colour. |
| `showCloseButton` | `bool` | `true` | Show / hide the × button. |
| `initialPosition` | `BubblePosition` | `BubblePosition(x:20, y:300)` | Starting position. |
| `autoOpenOnNotification` | `bool` | `true` | Auto-open app when notification arrives. |
| `autoOpenDelayMs` | `int` | `1500` | Delay before auto-open (ms). |
| `tapIntent` | `BubbleIntent?` | `null` | Custom intent on bubble tap; `null` = default launch. |
| `persistentNotificationTitle` | `String` | `'App is running in background'` | Foreground service notification title. |
| `persistentNotificationText` | `String` | `'Tap the bubble to return'` | Foreground service notification text. |
| `persistentNotificationChannelId` | `String` | `'bubble_overlay_channel'` | Notification channel ID. |
| `persistentNotificationChannelName` | `String` | `'Bubble Overlay'` | Notification channel name shown in settings. |

### `BubbleMode`

```dart
enum BubbleMode {
  auto,               // use whichever permission is available
  accessibilityService,
  foregroundService,
}
```

### `BubbleIntent`

```dart
const BubbleIntent({
  String? action,           // Android intent action; null = getLaunchIntentForPackage
  Map<String, String> extras = const {},
  String? route,            // passed as 'route' extra — use with onGenerateRoute
})
```

### `BubbleNotification`

```dart
class BubbleNotification {
  final String? title;
  final String? text;
  final Map<String, dynamic> extras;
}
```

### `BubblePosition`

```dart
const BubblePosition({int x = 20, int y = 300})
```

---

## Mode Comparison

|  | `accessibilityService` | `foregroundService` |
|---|---|---|
| Permission | Accessibility Service toggle | "Display over other apps" |
| Setup friction | Moderate (buried in Settings) | Low (one-tap permission) |
| Visible to user | Named service in Accessibility | Status-bar persistent notification |
| Notification interception | ✅ via accessibility events | ✅ via `NotificationListenerService` (future) |
| Works when app is killed | ✅ | ✅ |
| Requires `SYSTEM_ALERT_WINDOW` | ❌ | ✅ |
| Recommended for | Apps already using accessibility | General-purpose apps |

---

## Android Permissions (auto-added by plugin)

The following are declared in the plugin's manifest and merge automatically:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

`POST_NOTIFICATIONS` and `SYSTEM_ALERT_WINDOW` are runtime-prompted only when the foreground service mode is used.

---

## Receiving the route extra in Flutter

When `BubbleIntent(route: '/chat')` is set, the `route` extra is included in the launch intent. Handle it in your `MaterialApp`:

```dart
MaterialApp(
  onGenerateRoute: (settings) {
    // The route name is set from the intent extra when the app is re-opened
    switch (settings.name) {
      case '/chat': return MaterialPageRoute(builder: (_) => ChatScreen());
      default:      return MaterialPageRoute(builder: (_) => HomeScreen());
    }
  },
);
```

> **Tip:** Use `flutter_local_notifications` or `firebase_messaging` alongside this plugin. Deliver a notification with a `type` extra, listen on `BubbleAccessibility.onNotification`, and navigate to the right screen.

---

## License

MIT — see [LICENSE](LICENSE).
