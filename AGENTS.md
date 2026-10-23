# AGENTS.md — Bander TV

## CRITICAL: Ask the user first

At the start of a new Codex setup conversation **ask these two questions before making personal selections**:
1. **وش النادي اللي تشجعه؟** (Which football team do you support?)
2. **وش مدينتك أو منطقتك؟** (Which city/region should be used for weather and prayer times?)

Do NOT assume **Al-Hilal**, **Riyadh**, or any other team/city. A user may pick any club and any city worldwide. The launcher itself must ask the same two questions on first opening (the current HTML implements an onboarding dialog); save the user's choices to persistent **device-local settings** and let them change them under Settings. The Codex conversation must also offer to set the user's choices in a test setup, without hard-coding them as defaults for everyone.

City input must be geocoded into a clear chosen locality with name, country, latitude, longitude and IANA timezone. If matching locations are ambiguous, show multiple options and let the user choose. Never silently choose a wrong location. Do not request an exact home address or precise GPS location: city-level coordinates are adequate.

## Product specification
- **Bander TV is an actual TV HOME launcher**, not a normal entertainment app or a static image. The Android manifest registers MAIN/HOME/DEFAULT and also provides LEANBACK_LAUNCHER for safe preview.
- When supported by firmware, the user can select it as the system default Home. Never silently disable, delete or overwrite the TV maker's launcher. Offer device-appropriate and reversible setup only.
- **Pure Arabic RTL lightweight interface**, with "Bander TV" as the only English brand text. No games, kids sections, fake movies, subscriptions, extra entertainment panels, or fake streaming. Keep only الرئيسية / التطبيقات / الإعدادات (plus optional relevant search).
- First-run club and city/region questions, clearly editable after setup.
- Home contains installed TV apps; weather and accurate local prayer times for **user-selected city**, and a compact sports area for **user-selected club**. Never hard-code scores or ranking; show factual verified current sports data when a legitimate licensed/reliable live source is configured, and otherwise show a clear **Google Search results link** with a notice that in-app live scores are not yet connected. There is no unrestricted official public Google sports results API, and scraping search HTML as a live feed is brittle; do not mislabel Google links as integrated live results.
- For weather, use Open-Meteo forecast with chosen coordinates. For prayer, use AlAdhan API, with appropriate selectable calculation method, particularly Umm al-Qura method 4 for Saudi localities. Display unavailable status for failures rather than inventing data. Time display must use selected IANA timezone, not the device owner's presumed city.
- Discover and launch actually installed Android TV apps with the native bridge; do not show fake installable apps. Provide a sensible empty-state when no apps are found.
- Fully operable with D-pad up/down/left/right, OK, Back, stable focus and appropriate accessibility labels; TV 1080p layout.
- No credentials, API secrets or precise location data in public GitHub; preferences remain on device.
- Prevent the Android privileged JS bridge from being callable by untrusted remote content.

## Repository
- `index.html` browser preview. Keep synchronized with `app/src/main/assets/index.html` bundled in APK.
- `app/src/main/java/com/bandartv/launcher/MainActivity.java`: Android HOME launcher shell, installed app discovery, TV preferences/system settings.
- `app/src/main/AndroidManifest.xml`: launcher registrations and permissions.
- `.github/workflows/build-android-tv.yml`: GitHub Actions debug APK build.

## Validation required
1. Build via GitHub Actions or `gradle :app:assembleDebug` and verify artifact exists.
2. Test first-use setup for arbitrary club and city, including duplicate place names, and confirm preferences survive restart.
3. Test weather and prayer rendering with the selected location, failure and offline cases, and same timezone.
4. Test the launcher on emulator using remote D-pad focus and launch of an installed application.
5. Test optional Home selection and recovery on the actual TV model only after the user supplies brand/model and Android TV/Google TV version.
6. Mark scores as **external search** until a real, verified scores source is implemented.
7. Clearly distinguish code build success from an actually tested TV default launcher.

## Safety
Do not inject counterfeit subscriptions, pirated stream lists or use copyrighted visuals without permission. Do not write signing keys or secrets to GitHub.