# WojThom Android

Native Android version of WojThom 6.0.

## Stack

- Kotlin 2.3.21
- Android Gradle Plugin 9.4.0
- compileSdk / targetSdk 37
- Jetpack Compose
- Material 3
- Compose BOM 2026.08.00
- JDK 17

## Current state

The first clean foundation already contains:

- Material 3 application shell
- bottom navigation: Work / History / Statistics / Settings
- work-log input
- parser for ranges (`08:00 - 16:00`), decimal hours (`7.5h`) and durations (`08:30`)
- full dates with a year; entries without a year use the current year
- validation of date/time values
- total work-time calculation
- entry cards and deletion

## Next modules

1. Room database and migration/import strategy for WojThom 5.x data
2. editing and manual entry form
3. report history
4. week/month statistics and 37.5 h target
5. PL / EN / NO resources
6. professional PDF export and Android share sheet
7. settings, themes and backup/export
8. release signing and CI build

Open this directory as the project root in Android Studio.
