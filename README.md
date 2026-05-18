# Rakib offline call

A Kotlin Android 14+ starter application for offline nearby calling concepts without mobile data or internet. The app focuses on a smart, crash-safe user experience for Bluetooth / nearby-device discovery, direct and group push-to-talk flows, messages, vibration alerts, background reconnect, and a radar-style peer screen.

## Owner details

- Name: Rakib
- Location: Khulna
- Email: mr4425390@gmail.com

## Important offline range note

Android phones cannot guarantee unlimited offline voice range in software alone. Actual distance depends on phone hardware, Bluetooth radio quality, Android background restrictions, obstacles, battery optimization, and whether relay phones are nearby. This app provides the UI and service structure for offline mesh/reconnect behavior and is designed so real Bluetooth / Wi‑Fi Direct transport can be swapped into `OfflineMeshService`.

## Included features

- H+ loading screen with offline communication explanation.
- Sequential Android 14+ runtime permission flow.
- Foreground background service for reconnect/scanning state.
- Direct user cards with select, call/ring, hold-to-talk PTT, and direct messages.
- Group channel with group PTT and group message fan-out.
- Vibration alert support.
- Smart radar display showing estimated distance, side/direction, and red peer dots.
- Battery percentage and reconnect-safe peer snapshots.
- Boot receiver hook for reconnect after restart when Android allows it.

## Build

```bash
gradle :app:assembleDebug --no-daemon
```

The debug APK is expected at `app/build/outputs/apk/debug/app-debug.apk` after a successful Android SDK/Gradle plugin setup.
