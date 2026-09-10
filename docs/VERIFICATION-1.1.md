# Stilltime 1.1 verification

Verified on 10 September 2026 using the bundled Android Studio JBR, compile/target SDK 37, and an Android API 26 emulator.

## Automated checks

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.mrfool.stilltime.test/androidx.test.runner.AndroidJUnitRunner
```

- Build: successful for debug, minified release, and instrumentation APKs.
- Unit tests: 20 passed, zero failures, errors, or skips.
- Android lint: no issues found.
- Instrumentation: 9 passed. Covers actual split-flap intermediate frames, settled/idle pixels, cancellation, menu persistence and landscape navigation, all new reference and wallpaper faces, portrait fallbacks, and Spotify fixture layout/control states.
- Visual review: portrait and landscape Flip/menu, reference clocks, wallpaper compositions, and Spotify fixture. The fixture is not a live Spotify session.

Screenshots are in [screenshots/v1.1](screenshots/v1.1). Clock-face examples use a fixed date/time; menu screenshots use the emulator's current clock.

## Installable preview

`outputs/Stilltime-1.1.0-preview.apk` is 1,712,269 bytes, minified, resource-shrunk, and non-debuggable. It is signed with the existing development certificate, not a production/Play signing key. APK v2/v3 signatures verify and its SHA-1 matches the registered Spotify Android app. Installing it over the development build succeeded; the main Activity launched and remained running with the saved Muse preference intact.

SHA-256:

```text
cca55715f12bad286cf8fb2e2660532b55a306699650521006c7098afb00077d
```

## Remaining device checks

- Live Spotify consent, connection, artwork, and playback controls require the official Spotify Android app and an eligible signed-in account. The test emulator lacks Spotify.
- No physical battery benchmark has been performed. The app avoids idle animation and background work, but screen brightness/content and Spotify playback affect whole-device power.
- Test additional manufacturers, display sizes, accessibility font sizes, and production signing before a public store release.

See [SPOTIFY.md](SPOTIFY.md) for registration and connection instructions.
