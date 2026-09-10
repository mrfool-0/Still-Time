# Privacy policy

Stilltime has no analytics or developer-operated server. It does not sell data or retain a listening history.

- The app requests internet permission for the optional Spotify SDK integration. Clock faces, wallpapers, preferences, and the entire Muse library work locally.
- It contains no advertising, analytics, telemetry, account system, or tracking SDK.
- Preferences are stored only on the device in Android app-private storage; app backup is disabled.
- Battery percentage and charging state are read locally for display and are not retained.
- Panorama and Redline read the next system alarm time for display. Stilltime does not create or change alarms.
- Selecting the Spotify theme connects to the installed Spotify app. After authorization, Stilltime holds current track metadata, album artwork, playback position, and playback restrictions in memory, and sends requested playback commands to Spotify. This state is discarded on disconnect; it is not written to a listening-history database.
- Spotify handles account authentication, authorization, playback, and its own networking under its policies. No Spotify client secret is shipped in the app. Stilltime does not implement its own token store.
- Version 1.2 adds an optional Device player mode. Android requires notification-listener access to expose media sessions. A disclosure appears before opening Android Settings; the user must explicitly grant this broad system permission. Stilltime subscribes only to supported Spotify-package media sessions while the Spotify face is visible. It reads song metadata, supplied artwork, position and playback actions, not notification messages, contacts, or other apps' media. It does not upload or retain that session data. Revoke access at any time under Android Settings → Notification access. The listener service does not parse or store notification payloads.
- The Spotify connection and subscriptions stop when its screen is no longer started or its screensaver detaches. Leaving Stilltime does not stop Spotify music.
- Tapping a Muse source opens the chosen web browser. Any data handling after that point is governed by the browser and destination site.

This policy describes version 1.2.0 of Stilltime.
