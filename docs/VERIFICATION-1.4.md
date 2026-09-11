# Stilltime 1.4 verification

## Changes

- Spotify's equal-width landscape layout now has a unified dark surface, an inset rounded player, and controls using the theme's independently saved accent and typography. Portrait remains stacked.
- Interactive seek bar previews the selected time while dragging and sends one command on release. Accessibility progress actions work. Capability checks, position clamping and track-ID validation reject unsupported or stale requests.
- Shuffle uses actual session state. App Remote uses its native shuffle API; Device player adds an AndroidX Media compatibility bridge while preserving the working native media-session connection. Unknown or unsupported shuffle controls remain disabled.
- Kitten animation previously stayed static at Night brightness and waited several seconds before its first blink. It now greets after 700 ms, has a more visible 430 ms blink and stronger head/ear/tail gestures, and works at Night brightness when enabled. Subsequent gestures remain intermittent; hidden screens, previews and system reduced-motion behavior are preserved.

## Checks

- JVM suite: **26 tests**, all passing, including valid, clamped, stale-track and unsupported seeks.
- API 26 emulator suite: **24 tests**, all passing. New coverage checks touch scrubbing without intermediate commands, accessibility seeking, actual shuffle selection, disabled controls, accent rendering, and visible kitten blinking at Night brightness followed by no motion when inactive.
- Android lint: no issues found.
- Debug and minified release builds: successful.
- Signed non-debuggable release smoke runner: successful Activity launch and retained Spotify reflection constructor/interface.
- Landscape and portrait player screenshots visually checked. The committed player image is explicitly labeled as a fixture, not live music.

The user confirmed Spotify was working before this update. Live seeking/shuffle on the user's phone and physical-device power measurements remain unverified. Spotify sessions that do not expose shuffle capability cannot be controlled through that protocol; the UI does not fake success.

## Install

`outputs/Stilltime-1.4.0-preview.apk`, version code 5, **1,786,136 bytes**. Signed with the existing development certificate for in-place update; not Play Store production signing.

SHA-256: `02ea147052f8deff0253b3098c2b69f7c25876abf0a25de3a163eb2ad6bb806d`.

## Protocol references

- [AndroidX Media release notes](https://developer.android.com/jetpack/androidx/releases/media): 1.8.0 compatibility library; deprecated in favor of Media3 for new media applications. This app uses only its existing-session shuffle bridge, not an audio engine.
- [MediaControllerCompat transport controls](https://developer.android.com/reference/android/support/v4/media/session/MediaControllerCompat.TransportControls): shuffle mode control.
- [Spotify App Remote Player API](https://spotify.github.io/android-sdk/app-remote-lib/docs/com/spotify/android/appremote/api/PlayerApi.html): seeking and shuffle. Signatures and restrictions were also checked against the bundled official SDK artifact.
