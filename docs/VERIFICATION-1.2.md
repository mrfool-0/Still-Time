# Stilltime 1.2 — fixes and verification

## What changed

- Muse rotates every 30 seconds while visible, with boundary-aligned updates instead of continuous polling. All feed categories cycle without immediate repetition.
- Added 100 clearly marked original series-inspired reflections (20 each for One Piece, Naruto, LOTM novel, Bleach, Attack on Titan), bringing the library to 303 entries. Official publisher links provide context, not fabricated canon attribution.
- Muse's aurora backdrop performs one 2.4-second light drift per thought, with no idle loop. Motion is disabled in menus, while hidden, at Night brightness, or with its off switch; system animation settings are respected.
- Twenty accents and six clock type choices are available across the faces. Original colors can be restored; optional digit tint uses darker ink on light backgrounds.
- All eight supplied wallpapers are exposed in a two-column gallery. Asset hashes were checked against the supplied files and match byte-for-byte.
- The screensaver has explicit lifecycle and saved-state owners for its Compose view, starts/stops clock work with the dream lifecycle, and hides system bars.
- Spotify's reflection provider and constructor survive full-mode R8. Installed-but-unsupported Spotify packages no longer incorrectly show an installation prompt.
- Optional Device player mode uses Android media-session access with an explicit permission explanation. It does not grant itself access, read notification messages, or retain listening history.

## Regression evidence

The previous optimized APK's R8 `usage.txt` contained `ReleaseSpotifyLocator: public void <init>()`, proving the reflection constructor was removed. Its screensaver was launched through Android Settings and crashed with `IllegalStateException: ViewTreeLifecycleOwner not found`. Both paths are now covered by focused checks.

- 23 JVM unit tests passed, including exact 29/30/59/60-second quote boundaries, complete rotation through each feed, and five complete original-reflection packs.
- 18 debug emulator tests passed: reflection, dream-owner lifecycle, device-session metadata/restrictions, consent UI, appearance changes across faces, flip animation, menu persistence, wallpaper selection, reference faces, Spotify layout fixtures, all five series packs in both orientations, finite aurora motion/cancellation/idle stability, and zero-duration reduced motion.
- Android lint: no issues found. Debug and optimized release builds succeeded.
- The **actual signed, non-debuggable release APK** passed the native smoke runner: Spotify reflection constructor/interface present; main Activity launches.
- Android Settings → Screen saver → Stilltime clock → Start now launches the real DreamService and keeps it bound. Muse continued to show different quotes while the dream remained running. The original missing-lifecycle crash no longer occurs.

The broad AndroidX instrumentation suite is run against debug. It cannot be substituted unchanged onto a separately optimized APK: shared-library stripping and mismatched obfuscation mappings can break the test harness. A dependency-light native runner is compiled against the **same release mapping** for release smoke verification.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest assembleRelease
# Install debug APK and debug test APK, then:
adb shell am instrument -w com.mrfool.stilltime.test/androidx.test.runner.AndroidJUnitRunner

./gradlew -PtestBuildType=release assembleReleaseAndroidTest
# Sign both release APKs with the same certificate, install them, then:
adb shell am instrument -w com.mrfool.stilltime.test/com.mrfool.stilltime.ReleaseSmokeRunner
```

## Preview artifact

`outputs/Stilltime-1.2.0-preview.apk`: version code 3, min SDK 26, target SDK 37, 1,753,285 bytes. Minified and resource-shrunk; signed with the same development certificate as previous previews so it can update them. Not a Play Store production signing setup.

SHA-256: `347c2148b8a5980be707893de9da3a97b6c372a57977857ef62a1fb917107973`.

Live Spotify authentication/playback on the user's phone and physical-device power profiling still require on-device validation. Fixture tests do not claim a live Spotify session.

## Supplied-image inventory

| Upload suffix | In-app wallpaper |
|---|---|
| 23.01.46.jpeg | Neon gaze |
| 23.01.44.jpeg | Ink eyes |
| 23.01.42.jpeg | Ronin |
| 23.01.40.jpeg | Ember |
| 23.01.37.jpeg | Silent blade |
| 23.01.35.jpeg | Dragon mist |
| 23.01.29.jpeg | Moon gate |
| 23.01.19.jpeg | Crimson gaze |

The remaining four uploads are clock-design references: 23.01.33 → Panorama; 23.01.23 → Redline; 23.01.26 → Night calendar; 23.01.10 → Chroma. They are implemented as working clocks, not embedded screenshots.

See [the complete gallery](screenshots/v1.2/wallpaper_gallery_menu.png) and [Spotify setup](SPOTIFY.md).

Implementation references: Android [DreamService](https://developer.android.com/reference/android/service/dreams/DreamService), [view-tree saved-state owners](https://developer.android.com/reference/androidx/savedstate/ViewTreeSavedStateRegistryOwner), [media-session access requirements](https://developer.android.com/reference/android/media/session/MediaSessionManager), and Compose [MotionDurationScale](https://developer.android.com/reference/kotlin/androidx/compose/ui/MotionDurationScale).
