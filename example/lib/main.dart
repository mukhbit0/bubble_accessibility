import 'dart:async';

import 'package:bubble_accessibility/bubble_accessibility.dart';
import 'package:flutter/material.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Configure the bubble before runApp.
  // All fields are optional — defaults work out of the box.
  await BubbleAccessibility.configure(
    const BubbleConfig(
      // ── Style ─────────────────────────────────────────
      sizeDp: 62,
      backgroundColor: 0xFF2196F3, // blue
      closeButtonColor: 0xFFE53935, // red ×
      showCloseButton: true,
      initialPosition: BubblePosition(x: 20, y: 300),

      // ── Behaviour ──────────────────────────────────────
      // Use accessibility service if enabled, else fall back
      // to foreground service (overlay permission).
      mode: BubbleMode.auto,
      autoOpenOnNotification: false, // we handle it ourselves below

      // ── Foreground-service notification (shown in status bar) ──
      persistentNotificationTitle: 'My App',
      persistentNotificationText: 'Tap the bubble to return',
      persistentNotificationChannelId: 'my_app_bubble',
      persistentNotificationChannelName: 'Bubble Overlay',

      // ── Custom tap intent ──────────────────────────────
      // Uncomment to deep-link to a route when the bubble is tapped:
      // tapIntent: BubbleIntent(route: '/home'),
    ),
  );

  runApp(const ExampleApp());
}

class ExampleApp extends StatelessWidget {
  const ExampleApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      title: 'Bubble Accessibility Demo',
      home: HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  bool _accessibilityEnabled = false;
  bool _overlayGranted = false;
  final List<String> _log = [];
  StreamSubscription<BubbleNotification>? _notifSub;
  StreamSubscription<void>? _tapSub;

  @override
  void initState() {
    super.initState();
    _checkPermissions();
    _startStreams();
  }

  @override
  void dispose() {
    _notifSub?.cancel();
    _tapSub?.cancel();
    super.dispose();
  }

  Future<void> _checkPermissions() async {
    final acc = await BubbleAccessibility.isAccessibilityEnabled;
    final ov = await BubbleAccessibility.isOverlayPermissionGranted;
    if (mounted) setState(() { _accessibilityEnabled = acc; _overlayGranted = ov; });
  }

  void _startStreams() {
    // Listen to notifications from your app → decide what to do
    _notifSub = BubbleAccessibility.onNotification.listen((n) {
      final entry = '[NOTIF] ${n.title}: ${n.text}';
      setState(() => _log.insert(0, entry));
      // Example: auto-open only for specific notification types
      if (n.extras['type'] == 'chat') BubbleAccessibility.show();
    });

    // Know exactly when the user taps the bubble
    _tapSub = BubbleAccessibility.onBubbleTap.listen((_) {
      setState(() => _log.insert(0, '[TAP] Bubble tapped'));
    });
  }

  Widget _permissionTile({
    required String title,
    required String subtitle,
    required bool granted,
    required VoidCallback onGrant,
  }) {
    return ListTile(
      leading: Icon(
        granted ? Icons.check_circle : Icons.error_outline,
        color: granted ? Colors.green : Colors.orange,
      ),
      title: Text(title),
      subtitle: Text(subtitle),
      trailing: granted
          ? null
          : FilledButton(onPressed: onGrant, child: const Text('Grant')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Bubble Demo')),
      body: ListView(
        children: [
          // ── Permissions ────────────────────────────────────────────────────
          const Padding(
            padding: EdgeInsets.fromLTRB(16, 16, 16, 0),
            child: Text('Permissions', style: TextStyle(fontWeight: FontWeight.bold)),
          ),

          _permissionTile(
            title: 'Accessibility Service',
            subtitle: 'Required for BubbleMode.accessibilityService / auto',
            granted: _accessibilityEnabled,
            onGrant: () {
              BubbleAccessibility.openAccessibilitySettings();
            },
          ),

          _permissionTile(
            title: 'Display over other apps',
            subtitle: 'Required for BubbleMode.foregroundService / auto',
            granted: _overlayGranted,
            onGrant: () {
              BubbleAccessibility.openOverlaySettings();
            },
          ),

          // ── Manual control ─────────────────────────────────────────────────
          const Padding(
            padding: EdgeInsets.fromLTRB(16, 16, 16, 0),
            child: Text('Manual control', style: TextStyle(fontWeight: FontWeight.bold)),
          ),

          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: Row(
              children: [
                const FilledButton(
                  onPressed: BubbleAccessibility.show,
                  child: Text('Show bubble'),
                ),
                const SizedBox(width: 12),
                const OutlinedButton(
                  onPressed: BubbleAccessibility.hide,
                  child: Text('Hide bubble'),
                ),
                const SizedBox(width: 12),
                OutlinedButton(
                  onPressed: _checkPermissions,
                  child: const Text('Refresh'),
                ),
              ],
            ),
          ),

          // ── Event log ─────────────────────────────────────────────────────
          const Padding(
            padding: EdgeInsets.fromLTRB(16, 16, 16, 0),
            child: Text('Event log', style: TextStyle(fontWeight: FontWeight.bold)),
          ),

          if (_log.isEmpty)
            const Padding(
              padding: EdgeInsets.all(16),
              child: Text(
                'Background the app — the bubble will appear.\n'
                'Pull down notifications or tap the bubble to see events here.',
                style: TextStyle(color: Colors.grey),
              ),
            ),

          ..._log.map(
            (e) => Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 3),
              child: Text(e, style: const TextStyle(fontFamily: 'monospace', fontSize: 12)),
            ),
          ),
        ],
      ),
    );
  }
}
