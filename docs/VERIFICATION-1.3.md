# Stilltime 1.3 verification

## Changes

- Fourteenth theme: Kitten, drawn with resolution-independent vector paths inspired by the supplied reference. Eyes blink for 280 ms every 6.5–8.5 seconds; every third blink has a 1.4-second ear/head/tail gesture. There is no continuous frame loop between gestures. Hidden screens, previews and Night brightness stay static. The user can disable kitten motion; system motion scaling is respected.
- Accent, original colors, digit tint and clock typography are independent per theme. Migration preserves the formerly selected theme only; other faces retain original defaults. Preview cards read their own saved appearance.
- Removed menu taglines, setup paragraphs, power tips, quote-feed explanations, animation instructions, saved-state caption and startup hint. Kept actionable controls, errors, quote attribution and the permission consent dialog.
- The submitted screenshot's `i9` is `AuthenticationFailedException` in the exact 1.2 release mapping. Typed error handling now survives obfuscation and avoids displaying raw SDK payloads. The underlying Spotify authorization rejection cannot be resolved from the screenshot alone.
- Device player is the default unless the user previously explicitly selected a connection method. App Remote errors offer a direct Device player button. Android media-access consent is still required and never granted automatically. Resume/listener-ready events refresh the connection; revoking access detaches it.

## Checks

- JVM suite: 24 tests, including typed/redacted Spotify errors.
- Debug emulator suite: 22 tests, including per-theme isolation, persisted settings, one-time migration, kitten blinking and stopping when hidden, portrait/landscape renders, visible direct fallback action, consent UI, and existing regressions.
- Android lint and minified release build.
- Native smoke runner compiled against the exact release mapping checks retained Spotify reflection and actual non-debuggable Activity launch.

Live Spotify metadata/artwork/control on the user's phone and physical-device battery measurements remain unverified. No account authorization or permission success is inferred from fixture tests.

## Install

`outputs/Stilltime-1.3.0-preview.apk`, version code 4, 1,753,285 bytes, signed with the existing development certificate for in-place update. Not Play Store production signing.

SHA-256: `05aa4e98d13049b8ef48b450f96e55ece77e4cf3fc7ec708701239d10e9403fe`.

For local Spotify playback: select Spotify → Grant access → review and enable Stilltime media access in Android Settings → return after playing a song. If App Remote was explicitly selected earlier, use its new Device player button or change the method in Customize.

References: Spotify's [authorization flow](https://developer.spotify.com/documentation/android/tutorials/getting-started) and [exception types](https://spotify.github.io/android-sdk/app-remote-lib/docs/com/spotify/android/appremote/api/error/package-summary.html). The 1.2 mapping, rather than a guess at the obfuscated name, identified the submitted error.
