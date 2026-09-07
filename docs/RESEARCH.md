# Stilltime research brief

Research was performed on 7 September 2026. The goal was not to copy an existing clock, but to validate the platform architecture, identify defensible low-power techniques, survey useful interaction patterns, and build a traceable content library.

## Conclusions that shaped the app

### Use two platform surfaces

Android documents `DreamService` as the system mechanism for screensavers that run while a charging device is idle or docked. Stilltime therefore exposes the same Compose UI in both a normal full-screen Activity and a non-interactive `DreamService`. The Activity is the configurable desk mode; the dream is the system-managed charging experience.

Android's current power guidance says to use the lightest suitable API and warns that wake locks can drain battery quickly. For a visible clock, the documented option is `keepScreenOn`. Stilltime scopes that flag to the foreground window and removes it on disposal. It deliberately has no `WAKE_LOCK`, alarm, job, foreground-service, or network permission.

Sources:

- [Choose the right API to keep the device awake — Android Developers](https://developer.android.com/develop/background-work/background-tasks/awake)
- [`DreamService` API reference — Android Developers](https://developer.android.com/reference/android/service/dreams/DreamService)
- [Platform power management with Doze — Android Open Source Project](https://source.android.com/docs/core/power/platform_mgmt)

### Update only when the displayed value can change

With seconds disabled, the visible clock changes once per minute. Stilltime calculates the delay to the next wall-clock boundary and suspends until then. Enabling seconds intentionally increases updates to once per second. The ticker is tied to Activity lifecycle state, so it stops after pause. Battery state comes from Android's sticky battery broadcast rather than polling.

The UI uses stable preference models, caches the local Muse corpus, and remembers date/time formatting work. Compose's official performance guidance recommends remembering expensive calculations and restricting state reads to the phase that needs them; Stilltime follows the same principle without adding continuous animation clocks.

Source: [Jetpack Compose performance best practices — Android Developers](https://developer.android.com/develop/ui/compose/performance/bestpractices)

### Treat display power and burn-in as hardware-dependent

Stilltime makes no universal battery-percentage promise. OLED and LCD panels behave differently, brightness curves differ by device, and the system screensaver implementation is OEM-controlled. The app offers:

- a sparse, pure-black Noir face intended for OLED;
- a 4% Night window brightness option;
- seconds disabled by default;
- a deterministic 3 dp content drift every two minutes;
- no video, particle loop, shader animation, sensor stream, weather fetch, or photo slideshow.

AOSP's historical clock-plugin guidance identifies clocks as high-risk for AOD battery consumption and burn-in, recommends sparse lit pixels, and recommends moving clock content over time. Stilltime borrows the engineering principles, not AOSP code. Academic measurements also support the more limited claim that OLED display power depends on emitted colors; actual savings must be measured on the target panel.

Sources:

- [AOSP clock plugin system-health guidance](https://android.googlesource.com/platform/frameworks/base/%2Bshow/android10-release/packages/SystemUI/docs/clock-plugins.md)
- [AOSP burn-in helper directory](https://android.googlesource.com/platform/frameworks/base/%2B/android16-qpr2-release/packages/SystemUI/src/com/android/systemui/doze/util/)
- [Chameleon: A Color-Adaptive Web Browser for Mobile OLED Displays](https://arxiv.org/abs/1101.1240)
- [Power Profiler — Android Developers](https://developer.android.com/studio/profile/power-profiler)

## Open-source feature survey

The following repositories were evaluated as product benchmarks. No source code or visual assets were copied into Stilltime.

| Project | Useful finding | Licensing implication |
| --- | --- | --- |
| [FsClock-Android](https://github.com/schorschii/fsclock-android) | Validates demand for DreamService, date/battery options, Android TV support, and positional burn-in movement. | GPL project; treated only as a feature reference. |
| [Nightdream](https://github.com/firebirdberlin/nightdream) | Demonstrates a bedside-clock focus, pure-black AMOLED mode, brightness controls, and system screensaver support. | GPL project; treated only as a feature reference. |
| [Material Clock](https://github.com/rbouaf/material-clock) | Shows a modern Compose clock can remain expressive with careful typography and strong component hierarchy. | MIT, but Stilltime uses an independent design and implementation. |
| [ClockDesk](https://github.com/nx-d1frnt/ClockDesk) | Reinforces pixel drift and adaptive rendering as useful always-on-display features. | Feature comparison only; no code incorporated. |

The survey led to a deliberately narrower scope than feature-heavy alarm/radio/weather clocks. Every data source, permission, receiver, and animation has an idle-energy cost. Stilltime keeps only glanceable clock, date, battery, brightness, style, and source-linked inspiration features.

## Visual design synthesis

Seven faces cover distinct use cases without image assets or runtime effects:

- **Pebble** — soft gradients, rounded metadata pills, and a playful sparkle.
- **Flip** — warm, dimensional split-flap cards without flip animation.
- **Editorial** — high-contrast serif typography and print-like rules.
- **Orbit** — an analog instrument face drawn directly on Canvas.
- **Solar** — a time-derived day/night horizon; it changes only with clock ticks.
- **Muse** — typographic quotation cards with category, attribution, work, and source action.
- **Noir** — pure-black, sparse seven-segment output for the lowest-lit-pixel option.

Each face adapts to portrait and landscape dimensions. All controls use at least 44–48 dp interactive targets, system time-format behavior remains available, and the composed clock exposes a single concise accessibility description.

## Content research and provenance

The Muse corpus is intentionally local and source-addressable. Public-domain quotations were checked against primary Project Gutenberg texts from Marcus Aurelius, Epictetus, Ralph Waldo Emerson, Henry David Thoreau, Benjamin Franklin, Frederick Douglass, Booker T. Washington, Samuel Smiles, William James, and James Allen. Life notes are original summaries of sources including CDC sleep/hydration guidance, U.S. HHS physical-activity guidance, WHO diet/social-connection/green-space material, NIH mindfulness/relaxation guidance, and peer-reviewed habit and learning research.

Anime dialogue was cross-checked against at least two transcript/reference witnesses where practical, kept at 20 words or fewer, and labelled with speaker and work. Provenance is not permission: none of those links grants redistribution rights to the underlying screenplays. The app and notice file make that limitation explicit.

Representative sources:

- [Project Gutenberg: Meditations](https://www.gutenberg.org/ebooks/2680)
- [CDC: About Sleep](https://www.cdc.gov/sleep/about/index.html)
- [WHO: Healthy diet](https://www.who.int/news-room/fact-sheets/detail/healthy-diet)
- [WHO: Social connection](https://www.who.int/news-room/questions-and-answers/item/social-connection)
- [Lally et al.: habit formation](https://doi.org/10.1002/ejsp.674)
- [Roediger & Karpicke: test-enhanced learning](https://doi.org/10.1111/j.1467-9280.2006.01693.x)
- [U.S. Copyright Office: short phrases](https://www.copyright.gov/help/faq/definitions.html)
- [Google Play intellectual-property policy](https://support.google.com/googleplay/android-developer/answer/9888072)

## Verification plan

Automated checks cover time formatting, 12/24-hour behavior, style cycling, burn-in offsets, data parsing, entry uniqueness, source completeness, category minimums, and quotation length. Android lint and debug APK assembly are run together. Emulator visual checks cover portrait/landscape rendering, every face, the settings surface, and Muse source metadata.

Power consumption cannot be certified from an emulator. Before claiming a measured improvement, run controlled A/B sessions on the same physical device, brightness, charge state, temperature, and connectivity, then compare Noir/seconds-off with the other faces using the Android Studio Power Profiler. Its ODPM rail data is supported on Pixel 6 and newer devices, while other devices may expose only battery-gauge or coulomb-counter data.
