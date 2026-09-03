# OpenTapper

**v1.00**

OpenTapper is a free, open-source auto-tapper (連打ツール) for Android. It uses the
Accessibility API to tap one or more points on screen automatically — no root
required.

## Features

- Multiple tap points, dragged into place directly on screen
- Adjustable interval (as low as 10ms) and hold duration, with both a slider
  and a cursor-editable text field for exact values
- Round-robin or simultaneous multi-point tapping
- Auto-stop by tap count or elapsed time
- Auto-pause when you leave the target app
- Keep-screen-awake while running
- Volume-down key to start/stop
- Force-stop button right in the persistent notification
- Three overlay display styles — Full, Compact, and Minimal — switchable
  from the settings sheet
- Home-screen widget for one-tap launch
- Export/import settings as text via the clipboard
- 17 languages, auto-detected from the device locale
- No network access, no analytics, no ads

## Support development

OpenTapper is free and ad-free. If it's useful to you, consider buying the
developer a coffee: **https://buymeacoffee.com/akunJP** (also linked at the
bottom of the app's home screen).

## How it works

The whole app is a single `AccessibilityService` (`TapService`) that draws a
floating control panel and draggable markers using `WindowManager` overlays
(`TYPE_ACCESSIBILITY_OVERLAY`, so no "draw over other apps" permission prompt
is needed), and dispatches `GestureDescription`s to tap the marked points.
There are no XML layouts — the UI is built entirely in code
(`OverlayUI.java`), which keeps the app small and easy to audit.

Key files:

- `TapService.java` — the accessibility service: gesture dispatch loop,
  auto-pause, notifications, wake lock, volume-key toggle
- `OverlayUI.java` — the floating panel, markers, and settings sheet
- `MainActivity.java` — the launcher screen (permission setup, language
  picker)
- `Config.java` — settings persistence (SharedPreferences) and
  export/import
- `L.java` — the (hand-rolled) multi-language string table
- `TapWidget.java` / `QuickActivity.java` — the home-screen widget

## Building

This is a standard Gradle Android project (no wrapper jar is checked in —
use a local Gradle install, or open the project in Android Studio, which
will offer to set one up).

```
gradle assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

A GitHub Actions workflow (`.github/workflows/build.yml`) also builds a debug
and a signed release APK on every push to `main`, and publishes them as a
release — no local Android SDK setup required if you just want an installable
APK from the "Actions" or "Releases" tab.

## Installing

1. Download an APK from the [Releases](../../releases) page or the latest
   Actions run's artifacts.
2. Allow installation from unknown sources when prompted.
3. Open OpenTapper, enable it in Accessibility settings, and grant
   notification permission if asked.

## Privacy

OpenTapper collects no data, requires no network permission, and does not
contain analytics or ads. All settings and tap points stay on the device.

## License

MIT — see [LICENSE](LICENSE).
