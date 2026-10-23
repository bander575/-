# AGENTS.md — Codex work plan for Bandar TV

## Project goal
Build an **actual installable Android TV / Google TV app**, owned by the repository owner, based on the current interactive Arabic RTL prototype. Preserve the dark navy/cyan luxurious Riyadh-themed look of the approved second concept. Do NOT replace UI interactions with a static image, and do not blindly clone UrTV.

## Repository
- `index.html`: browser preview; all HTML/CSS/JS bundled in one page.
- `app/src/main/assets/index.html`: copy of the preview bundled in the APK, and the actual app UI. Keep both synchronized when changing the UI or refactor cleanly into shared source + build steps.
- `app/src/main/java/com/bandartv/launcher/MainActivity.java`: Android shell and JS bridge.
- `app/src/main/AndroidManifest.xml`: Android TV app and intent visibility.
- `.github/workflows/build-android-tv.yml`: GitHub Actions APK build.

## Mandatory acceptance criteria
1. `gradle :app:assembleDebug` succeeds with JDK 17, Android SDK 35, Gradle 8.9; output `app/build/outputs/apk/debug/app-debug.apk`. Verify in CI and download artifact if possible.
2. App opens on an Android TV or Google TV emulator, landscape 1920x1080, can navigate every actionable control with directional pad and OK. Preserve focus across navigation and Back. Implement graceful app exit behavior.
3. Display actual Android installed TV launchable apps through a safe, minimal native bridge (respect Android 11+ package visibility, Android policies), rather than only hard-coded demo icons. Provide favorites and working app launch. Show fallback if not installed.
4. Build a genuine Home, All Apps, media demo, children, games and Settings screen. Keep Arabic right-to-left.
5. Create a proper Android TV 320x180 launcher banner and icon, not just a flat background drawable.
6. Weather/prayer API network failures are nonfatal; time and local UI work offline. Verify prayer source against local method when possible.
7. No pirated streams, unauthorized IPTV lists, scraping subscriber-only content, or unlicensed third-party artwork. Media and channel sections must stay visibly marked demos until licensed sources are supplied.
8. Explain exactly how to install and test on the user's Android TV, ADB optional, after obtaining **device brand/model and Android version**. Do not claim that you can silently install remotely or force the Home launcher.
9. Never commit credentials, tokens, local private data, or signing keys. GitHub Actions debug APK is for testing, not Play Store release.
10. Record what works, what fails, and emulator/TV model tested in README and/or TESTING.md.

## Suggested implementation order
- First repair any failing GitHub Actions build and produce a real debug APK.
- Then run emulator tests; fix crashes and D-pad focus.
- Then native installed-app discovery/launch, system settings.
- Then UX polish and offline reliability.
- Finally device-specific installation and optional Home-selection guidance.

## Constraints
Do not overpromise live channels or licensed content. Do not require the user to buy services to compile the initial app. If changing to native Compose for TV or Leanback, preserve behavior and RTL style and verify before removing the existing interactive prototype.
