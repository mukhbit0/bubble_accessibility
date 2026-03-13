## 0.1.1


### Features

- **Dual permission modes** — `BubbleMode.accessibilityService`, `BubbleMode.foregroundService`, and `BubbleMode.auto` (uses whichever permission is available on the device).
- **Zero boilerplate** — manifest entries and Kotlin services merge automatically into any host app. No manual Android setup.
- **Automatic lifecycle management** — bubble shows when the app is backgrounded or killed; hides when the app returns to foreground.
- **Notification payload streaming** — `BubbleAccessibility.onNotification` delivers a `BubbleNotification` (title, text, extras) to Dart whenever a notification arrives while the bubble is on screen.
- **Bubble tap streaming** — `BubbleAccessibility.onBubbleTap` fires on every tap before the app is opened.
- **Full style customisation** via `BubbleConfig`: size, background colour, icon drawable, close-button colour, initial position.
- **Behaviour control** — `autoOpenOnNotification`, `autoOpenDelayMs`, and custom `BubbleIntent` (action, extras, Flutter route).
- **Foreground service notification** — persistent status-bar notification title, text, channel ID and channel name are all configurable.
- **Works on Android 5.0+** (API 21+).

### Classes

| Class | Purpose |
|---|---|
| `BubbleAccessibility` | Main static API — configure, show, hide, check permissions, open settings, streams |
| `BubbleConfig` | Immutable configuration passed to `configure()` |
| `BubbleMode` | Enum: `auto`, `accessibilityService`, `foregroundService` |
| `BubbleNotification` | Notification event with `title`, `text`, `extras` |
| `BubblePosition` | Initial bubble coordinates |
| `BubbleIntent` | Custom Android intent fired on bubble tap |


## 0.1.0

**Initial public release.**