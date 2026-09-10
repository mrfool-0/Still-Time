package com.mrfool.stilltime.model

import androidx.annotation.StringRes
import com.mrfool.stilltime.R

enum class ClockStyle(@StringRes val labelRes: Int) {
    PEBBLE(R.string.style_pebble),
    FLIP(R.string.style_flip),
    EDITORIAL(R.string.style_editorial),
    ORBIT(R.string.style_orbit),
    SOLAR(R.string.style_solar),
    MUSE(R.string.style_muse),
    NOIR(R.string.style_noir),
    PANORAMA(R.string.style_panorama),
    REDLINE(R.string.style_redline),
    CALENDAR(R.string.style_calendar),
    CHROMA(R.string.style_chroma),
    WALLPAPER(R.string.style_wallpaper),
    SPOTIFY(R.string.style_spotify),
    CAT(R.string.style_cat),
    ;

    fun next(): ClockStyle = entries[(ordinal + 1) % entries.size]

    fun previous(): ClockStyle = entries[(ordinal - 1 + entries.size) % entries.size]
}

enum class TimeFormatPreference(@StringRes val labelRes: Int) {
    SYSTEM(R.string.format_system),
    TWELVE_HOUR(R.string.format_12_hour),
    TWENTY_FOUR_HOUR(R.string.format_24_hour),
}

enum class BrightnessMode(
    @StringRes val labelRes: Int,
    val windowBrightness: Float?,
) {
    SYSTEM(R.string.brightness_system, null),
    NIGHT(R.string.brightness_night, 0.04f),
    COZY(R.string.brightness_cozy, 0.16f),
    BRIGHT(R.string.brightness_bright, 0.46f),
}

enum class AccentChoice(val seed: Long) {
    ROSE(0xFFFFB7C5),
    MINT(0xFF9EE6CF),
    APRICOT(0xFFFFC98B),
    LILAC(0xFFC8B6FF),
    SKY(0xFF9AD9FF),
    CORAL(0xFFFF8F91),
    LAVENDER(0xFFADA7FF),
    ICE(0xFFC6EFFF),
    JADE(0xFF71D8AF),
    AQUA(0xFF6FDED9),
    LIME(0xFFD3E98A),
    GOLD(0xFFEED08B),
    AMBER(0xFFFFB765),
    PEACH(0xFFFFD9C2),
    ORCHID(0xFFE4A6ED),
    BERRY(0xFFE58ABE),
    BLUE(0xFF8FAEFA),
    SAGE(0xFFB8C9A3),
    SAND(0xFFDDCBB4),
    FROST(0xFFE8EDF2),
}

enum class MotivationCategory(@StringRes val labelRes: Int, val inspiredBy: String? = null) {
    ALL(R.string.motivation_all),
    WISDOM(R.string.motivation_wisdom),
    LIFE_FACT(R.string.motivation_life_facts),
    ANIME(R.string.motivation_anime),
    ONE_PIECE(R.string.motivation_one_piece, "One Piece"),
    NARUTO(R.string.motivation_naruto, "Naruto"),
    LOTM(R.string.motivation_lotm, "Lord of Mysteries · novel"),
    BLEACH(R.string.motivation_bleach, "Bleach"),
    ATTACK_ON_TITAN(R.string.motivation_aot, "Attack on Titan"),
}

data class ClockPreferences(
    val style: ClockStyle = ClockStyle.PEBBLE,
    val timeFormat: TimeFormatPreference = TimeFormatPreference.SYSTEM,
    val brightness: BrightnessMode = BrightnessMode.SYSTEM,
    val accent: AccentChoice = AccentChoice.ROSE,
    val motivationCategory: MotivationCategory = MotivationCategory.ALL,
    val showSeconds: Boolean = false,
    val showDate: Boolean = true,
    val showBattery: Boolean = true,
    val keepScreenOn: Boolean = true,
    val burnInProtection: Boolean = true,
    val wallpaper: WallpaperChoice = WallpaperChoice.MOON,
    val wallpaperLayout: WallpaperLayout = WallpaperLayout.CINEMA,
    val wallpaperDim: WallpaperDim = WallpaperDim.BALANCED,
    val typography: ClockTypography = ClockTypography.ORIGINAL,
    val spotifyMarquee: Boolean = true,
    val flipAnimation: Boolean = true,
    val themeColors: Boolean = true,
    val tintDigits: Boolean = false,
    val devicePlayer: Boolean = true,
    val museMotion: Boolean = true,
    val catMotion: Boolean = true,
)
