# Spotify theme and connection help

## Version 1.2 fixes

The 1.1 optimized APK could report “Install Spotify” even when Spotify was installed. Its R8 usage report showed that `ReleaseSpotifyLocator`'s public constructor was removed: the SDK loads it using `Class.forName(...).getConstructor().newInstance()`, while its bundled rules kept the class without its constructor. Version 1.2 explicitly retains the provider, constructor, and interface. A native instrumentation smoke runner validates reflection on the actual non-debuggable APK.

Installation and SDK support are now distinguished. If a Spotify package exists but App Remote cannot use it, the UI says “Spotify found · SDK unavailable” and suggests Device player instead of repeatedly directing the user to install Spotify. Spotify's signature validation has not been bypassed.

### Optional Device player

In Customize → Connection method select **Device player**. Tap **Grant access**, read the explanation, then optionally approve Stilltime media access in Android Settings. Return to Stilltime and play a song in Spotify on the same phone. This uses Android's media-session metadata and controls, not Spotify developer authorization. It only follows supported Spotify-package sessions and does not read notification messages. Android's permission is broad and is never granted automatically. Revoke it in Android Settings at any time. If Spotify supplies no artwork or skip action, Stilltime shows a placeholder or disables that action.

The user confirmed working Spotify playback integration before the 1.4 redesign. The new seek/shuffle controls are verified with local tests and fixtures, not a live Spotify session on the user's phone.

## Version 1.3 authorization recovery

The user's 1.2 screenshot showed `Could not connect (i9)`. The exact 1.2 R8 mapping identifies `i9` as `com.spotify.android.appremote.api.error.AuthenticationFailedException`. This is an authorization rejection, not missing Spotify. The screenshot alone does not identify whether the service rejected the account, signing registration, or another authorization condition. The app now uses typed exception checks rather than obfuscated class-name matching, and never displays raw SDK payloads.

Device player is now the default without an explicitly saved connection method. An App Remote error exposes **Use device player** directly. This local Android media-session connection does not require Spotify developer authorization, but does require the user's notification/media-access consent. Returning from Settings and notification-listener readiness both trigger a refresh; no polling was added. A blank session retains **Open Spotify** as an action.

## Version 1.4 player controls

The player now shares the Spotify theme's saved accent and typography, with a rounded inset panel and matching transport controls. Drag or tap the seek bar to choose a position; one seek command is sent when the gesture finishes. Accessibility progress actions also work. A track change or loss of control availability cancels a pending gesture, and controllers reject stale track IDs.

Shuffle displays the session's actual state rather than guessing success. App Remote uses `PlayerApi.setShuffle` and playback restrictions. Device player keeps its existing native Android metadata and transport path, with a small AndroidX Media compatibility bridge for the shuffle protocol. The button is enabled only when the session advertises shuffle control and a known shuffle state. Some Spotify sessions do not expose this capability; their shuffle button stays disabled. Seeking likewise requires an advertised seek action and a known duration.

AndroidX Media 1.8.0 is a legacy, deprecated API. It is used narrowly here to communicate with an existing external media session, not to add an audio player or playback service. References: [AndroidX Media release notes](https://developer.android.com/jetpack/androidx/releases/media) and [compatibility transport controls](https://developer.android.com/reference/android/support/v4/media/session/MediaControllerCompat.TransportControls).

## Registered application

- App: Stilltime, owned by the user's signed-in Spotify developer account.
- Public Client ID: `ce2acb9fb0664990ae2e4dc4564ced0d`.
- Redirect URI: `stilltime://spotify-callback`.
- Android package: `com.mrfool.stilltime`.
- Registered development signing SHA-1: `B4:B1:15:88:B2:E7:64:9C:78:E6:7C:88:BE:18:2B:2D:2A:D7:1D:2C`.
- [Developer dashboard](https://developer.spotify.com/dashboard/ce2acb9fb0664990ae2e4dc4564ced0d).

The Client ID is a public application identifier. The client secret was not viewed, copied, or embedded. The dashboard visibly confirmed the saved redirect and Android package/fingerprint after registration. The owner explicitly approved the developer terms before submission.

## Install and connect

1. Install the supplied Stilltime 1.4 preview APK on an Android 8.0+ device. It can update the original Stilltime APK because both use the same development certificate.
2. Install/open the official Spotify app and sign in to the intended account.
3. In Stilltime, choose **Spotify** from Customize and rotate the device to landscape.
4. For Device player, tap **Grant access** and approve Android media access. For App Remote, select that method in Customize, then tap **Connect** and approve Spotify's playback-control consent.
5. Start a song in Spotify. Stilltime will display its artwork and metadata; the transport buttons control that session. Stilltime does not automatically choose or start music.

Spotify's [Android getting-started guide](https://developer.spotify.com/documentation/android/tutorials/getting-started) documents the built-in App Remote authorization flow and `app-remote-control` scope. The separate auth library is unnecessary for this flow. Initial authorization requires a usable Spotify session and connectivity; the SDK documents limited offline reauthorization after a recent successful connection.

## Build configuration

`app/build.gradle.kts` includes the official App Remote 0.8.0 AAR and Gson. `SPOTIFY_CLIENT_ID` and `SPOTIFY_REDIRECT_URI` are Gradle properties emitted into `BuildConfig`. To use another registered app:

```bash
./gradlew assembleDebug -PSPOTIFY_CLIENT_ID=your_public_client_id -PSPOTIFY_REDIRECT_URI=your_registered_uri
```

Register the SHA-1 for the certificate that actually signs your APK. A different machine's debug keystore has a different fingerprint. Register your production/Play app-signing fingerprint before distributing a production build. Do not put a client secret in an Android APK.

SDK provenance: [official 0.8.0 release](https://github.com/spotify/android-sdk/releases/tag/v0.8.0-appremote_v2.1.0-auth). The AAR has no native ABI requirement. Its optional Jackson adapters are unused; narrowly scoped R8 warning rules cover those absent adapters and compile-time nullability annotations. Protocol model keep rules ship in the vendor AAR.

## Rendering and power behavior

Landscape uses two equal-weight panels in a left-to-right row. The left panel displays 24-hour `HH:mm`, the current date, and selected typography. The right displays square artwork, title/artist, shuffle, previous/play-pause/next, and an interactive elapsed/duration seek bar. Portrait stacks the panels. Missing Spotify, missing configuration, authorization failure, connection timeout, unavailable track, and disconnected states are handled without fake playback data.

The connection exists only while the Spotify theme's Activity is started or its screensaver is attached. Authorization prompts are only requested after a user taps Connect; a screensaver can only attempt a silent connection. An exiting theme cancels its player subscription, timeout, and connection. A generation counter discards late connection, track, and artwork callbacks. Artwork is requested at medium size only when its URI changes. Track events supply the authoritative position; a monotonic local clock updates progress once per second during playback, then stops when paused, hidden, or at track end. Marquee runs only while visible and playing, at most three cycles per track, and can be disabled. No Web API polling, background worker, wake lock, or audio service was added.

The official [App Remote README](https://github.com/spotify/android-sdk/blob/master/app-remote-lib/README.md) explains that audio playback, caching, and Spotify's network work remain in the Spotify app. This does not make music playback energy-free; measure the complete device workload when comparing battery use.

## Validation and limits

Unit tests cover elapsed-time interpolation, pause, track-end clamping, playback speed, invalid duration, leap-year calendar alignment, locale week starts, and six-row months. Emulator tests cover all reference faces, all eight wallpaper assets, all three wallpaper layouts, portrait fallbacks, equal Spotify panel widths, transport callbacks, disabled skip controls, time labels, and the missing-app state. Screenshots under `screenshots/v1.1` are emulator renders. `spotify_fixture.png` uses a clearly labelled test fixture and supplied wallpaper as sample art; it is not evidence of a live Spotify connection.

The user confirmed working integration before 1.4. Actual seeking and shuffle in the new version still need a phone check; the emulator does not have Spotify installed. The [1.4 verification notes](VERIFICATION-1.4.md) distinguish fixture checks from live playback. Physical-device battery profiling is also outstanding. Spotify [Development Mode restrictions](https://developer.spotify.com/blog/2026-02-06-update-on-developer-access-and-platform-security) include a Premium requirement and limits on developer apps/users; service-side eligibility and policy changes are outside the APK's control.
