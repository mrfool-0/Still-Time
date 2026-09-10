package com.mrfool.stilltime.model

import androidx.annotation.DrawableRes
import com.mrfool.stilltime.R

enum class WallpaperChoice(@DrawableRes val resource: Int, val title: String, val accent: Long) {
    NEON(R.drawable.wallpaper_neon, "Neon gaze", 0xFFE5A0EF),
    INK(R.drawable.wallpaper_ink, "Ink eyes", 0xFFE0DDEB),
    RONIN(R.drawable.wallpaper_ronin, "Ronin", 0xFFB7C49B),
    EMBER(R.drawable.wallpaper_ember, "Ember", 0xFFF1C59A),
    SILENCE(R.drawable.wallpaper_silence, "Silent blade", 0xFFD9E1E7),
    DRAGON(R.drawable.wallpaper_dragon, "Dragon mist", 0xFFDADDE2),
    MOON(R.drawable.wallpaper_moon, "Moon gate", 0xFFF48D92),
    CRIMSON(R.drawable.wallpaper_crimson, "Crimson gaze", 0xFFFF9297),
}

enum class WallpaperLayout(val title: String) { CINEMA("Cinema"), GALLERY("Gallery"), POSTER("Poster") }
enum class WallpaperDim(val title: String, val opacity: Float) {
    SOFT("Soft", 0.22f), BALANCED("Balanced", 0.48f), NIGHT("Night", 0.72f),
}
enum class ClockTypography(val title: String) { CLASSIC("Classic"), ROUNDED("Rounded"), EDITORIAL("Editorial") }
