# Third-party and content notices

## Android libraries

Stilltime uses AndroidX, Jetpack Compose, Kotlin, and kotlinx.coroutines. Their applicable notices and license metadata are included by their distributions. AndroidX and much of the Android platform sample material are available under Apache License 2.0; Kotlin and kotlinx.coroutines are also distributed under Apache License 2.0. Stilltime's implementation is original and does not copy code from the clock applications listed in the research brief.

## Muse content

The data file `app/src/main/res/raw/motivation.tsv` contains several content classes with different rights status. Source links are provenance records, not blanket licenses.

### Public-domain wisdom

`WISDOM` entries are short quotations transcribed from works made available by Project Gutenberg. The selected works and translations are believed to be in the public domain in the United States. Project Gutenberg cautions that copyright status can differ outside the United States; distributors are responsible for checking every target jurisdiction. Project Gutenberg's trademark and license terms are not a sponsorship of this app.

### Life notes

`LIFE_FACT` entries are original summaries written for Stilltime and link to the supporting public-health or research source. They are general educational information, not medical diagnosis, treatment, or personalized advice. Qualifiers such as “may,” “associated,” and “in one study” are intentional and must be preserved.

### Anime moments — rights not cleared

`ANIME` entries are very short attributed excerpts included at the user's request for a prototype. Copyright and related rights in the underlying films, television episodes, scripts, translations, characters, and franchises remain with their respective owners. Transcript and reference links establish provenance only; they do not grant redistribution, merchandising, endorsement, or commercial-use rights.

The Apache-2.0 license for Stilltime's original code does **not** relicense these excerpts. Before publishing the app commercially or submitting it to an app store, obtain appropriate written permissions or remove/replace the `ANIME` rows.

Google Play's current intellectual-property policy is available at <https://support.google.com/googleplay/android-developer/answer/9888072>.

## Supplied wallpapers and theme references

Version 1.1 incorporates eight JPEGs supplied by the user on 9 September 2026. They remain unmodified, including embedded artist marks, in `app/src/main/res/drawable-nodpi`. Their copyright belongs to their respective owners; supplying them for this private app does not establish a redistribution license. These images are excluded from the code's Apache-2.0 license. The screenshot references informed independent clock implementations; no phone frame or screenshot UI is bundled as a clock face.

## Fredoka

The Fredoka font is distributed under the SIL Open Font License 1.1. Its full notice is in [docs/licenses/Fredoka-OFL.txt](docs/licenses/Fredoka-OFL.txt). It was obtained from the Google Fonts repository and is bundled without modification.

## Spotify App Remote and Gson

The unmodified Spotify App Remote 0.8.0 AAR comes from the official `spotify/android-sdk` release. Its Apache-2.0 license and notice are preserved in [docs/licenses](docs/licenses). Gson is Apache-2.0 licensed. Using Spotify services is also governed by Spotify's Developer Terms and policies; this app is not endorsed by Spotify. Album artwork and track metadata belong to their rights holders and are only displayed from the authorized Spotify session.
