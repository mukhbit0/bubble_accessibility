## 0.1.0

- Initial public release.
- **Dual permission modes**: `BubbleMode.accessibilityService`, `BubbleMode.foregroundService`, and `BubbleMode.auto` (uses whichever permission is available).
- Automatic show / hide based on app lifecycle — no host-app boilerplate required.
- Notification payload streaming via `BubbleAccessibility.onNotification`.
- Bubble tap event streaming via `BubbleAccessibility.onBubbleTap`.
- Fully customisable via `BubbleConfig`: size, background colour, icon, close-button colour, initial position, auto-open behaviour, and persistent notification text.
- Custom tap intents via `BubbleIntent` (action, extras, route).
- Manifest and Kotlin code auto-merges into any host app — zero manual Android setup.
