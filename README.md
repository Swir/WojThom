# WojThom 6.0

WojThom is a work-time list generator rebuilt from the 5.x Android application as a clean two-platform project.

## Structure

- `android/` — native Android app in Kotlin + Jetpack Compose + Material 3
- `web/` — responsive browser version using HTML/CSS/JavaScript

## Goals

Both versions should share the same behaviour and data model:

- parse work-time logs such as `15.02.2026 Firma A 08:00 - 16:00`, `16.02.2026 Firma B 7.5h`, `17.02.2026 Firma C 08:30`
- calculate total working time
- edit and remove entries
- history and statistics
- PL / EN / NO languages
- light / dark themes
- professional PDF export
- local/offline data storage

## Status

WojThom 6.0 is being rebuilt from scratch. The old 5.x code and APK are treated as the functional reference while the architecture and UI are being modernised.

### Android

Open the `android` directory in Android Studio.

### Web

Open `web/index.html` directly in a browser or serve the `web` directory with any static web server.

---

Created for the WojThom project.
