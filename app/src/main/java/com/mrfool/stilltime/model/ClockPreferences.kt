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
}

enum class MotivationCategory(@StringRes val labelRes: Int) {
    ALL(R.string.motivation_all),
    WISDOM(R.string.motivation_wisdom),
    LIFE_FACT(R.string.motivation_life_facts),
    ANIME(R.string.motivation_anime),
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
)
