# WojThom README verification notes

This file records the evidence used for the SWIR README PRO v2 documentation migration. It is not a product roadmap and does not change application readiness.

## Evidence reviewed

- Root `README.md` and repository tree on `main`.
- `android/app/build.gradle.kts` for current Android SDK, Java and application-version configuration.
- `android/app/src/main/java/com/wojthom/app/MainActivity.kt` for the current Work / History / Statistics / Settings flow, local persistence integration, editing/search and PDF export entry point.
- `android/app/src/main/java/com/wojthom/app/i18n/AppLanguage.kt` for Polish, English and Norwegian UI support.
- `web/README.md`, `web/manifest.webmanifest` and the current web application files.
- `.github/workflows/android-build.yml` and `.github/workflows/web-check.yml` for the repository's existing CI scope.
- GitHub Releases metadata: no public GitHub Release existed when this migration was prepared.
- Open pull requests: none existed before the migration branch was created.

## Important documentation decisions

- Android configuration is taken from the current Gradle build file: `compileSdk = 36`, `minSdk = 26`, `targetSdk = 36`, Java 17 and application version `6.0.4-dev`.
- The older `android/README.md` contains stale dependency/version and "next module" statements, so the root README does not repeat those stale values as current facts.
- The repository does not contain a canonical measurable product roadmap. SWIR Progress SVG PRO therefore reports **N/A** for product readiness instead of inventing a percentage.
- The existing project identity in `web/icon.svg` is retained and displayed; no replacement runtime/app icon is introduced by this documentation change.
- A web manifest exists, but this migration does not claim complete offline/PWA behavior because no service-worker/offline-runtime verification was performed.

## Verification scope

The migration changes documentation, README artwork and deterministic documentation tooling only. It does not change Android/web application behavior, dependencies, versions, build workflows, signing, release tags or licensing.

The existing source workflows are path-filtered to `android/**` and `web/**`. A documentation-only pull request therefore may have no GitHub Actions run. In that case, documentation verification consists of:

1. deterministic `tools/generate_readme_progress.py --check`;
2. XML parsing of the local SVG assets;
3. checking README relative asset links and required v2/Search Keywords markers;
4. confirming the progress card, compact card and textual status all agree on **N/A**;
5. reading the final README and generated assets back from the default branch after merge.

A passing documentation check is not a claim that the Android APK or every browser path was runtime-tested during this migration.
