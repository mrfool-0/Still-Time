# Release-specific rules for the vendored Spotify SDK.
# App Remote bundles optional Jackson adapters; this app uses its Gson mapper.
-dontwarn com.fasterxml.jackson.databind.**
# SDK compile-time nullability annotations are not distributed in its AAR.
-dontwarn com.spotify.base.annotations.NotNull
-dontwarn javax.annotation.Nonnull
-dontwarn javax.annotation.Nullable

# Spotify reflectively loads this provider and its public constructor.
# The SDK's class-only keep rule loses the constructor in R8 full mode.
-keep interface com.spotify.android.appremote.internal.PackageProvider { *; }
-keep class com.spotify.android.appremote.internal.ReleaseSpotifyLocator { *; }
