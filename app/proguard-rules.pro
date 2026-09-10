# Stilltime has no reflection-based application code. The optimized Android defaults
# are sufficient; this file is intentionally kept for future release-specific rules.
# App Remote bundles optional Jackson adapters; this app uses its Gson mapper.
-dontwarn com.fasterxml.jackson.databind.**
# SDK compile-time nullability annotations are not distributed in its AAR.
-dontwarn com.spotify.base.annotations.NotNull
-dontwarn javax.annotation.Nonnull
-dontwarn javax.annotation.Nullable
