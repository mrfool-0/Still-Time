package com.mrfool.stilltime.data

import android.content.Context
import android.content.SharedPreferences
import com.mrfool.stilltime.model.AccentChoice
import com.mrfool.stilltime.model.BrightnessMode
import com.mrfool.stilltime.model.ClockPreferences
import com.mrfool.stilltime.model.ClockStyle
import com.mrfool.stilltime.model.MotivationCategory
import com.mrfool.stilltime.model.TimeFormatPreference
import com.mrfool.stilltime.model.WallpaperChoice
import com.mrfool.stilltime.model.WallpaperLayout
import com.mrfool.stilltime.model.WallpaperDim
import com.mrfool.stilltime.model.ClockTypography
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsStore(context: Context) :
    SharedPreferences.OnSharedPreferenceChangeListener,
    AutoCloseable {
    private val preferences = context.applicationContext.getSharedPreferences(
        FILE_NAME,
        Context.MODE_PRIVATE,
    )
    init {
        // Preserve the visible theme on upgrade without propagating its look to every face.
        if (!preferences.getBoolean("appearance_profiles_v1", false)) {
            val style = enumValue(preferences.getString(KEY_STYLE, null), ClockStyle.PEBBLE)
            preferences.edit().apply {
                putString(profileKey(style, KEY_ACCENT), preferences.getString(KEY_ACCENT, AccentChoice.ROSE.name))
                putString(profileKey(style, "typography"), preferences.getString("typography", ClockTypography.ORIGINAL.name))
                putBoolean(profileKey(style, "theme_colors"), preferences.getBoolean("theme_colors", true))
                putBoolean(profileKey(style, "tint_digits"), preferences.getBoolean("tint_digits", false))
                putBoolean("appearance_profiles_v1", true)
            }.apply()
        }
    }
    private val mutableState = MutableStateFlow(readPreferences())

    val state: StateFlow<ClockPreferences> = mutableState.asStateFlow()

    init {
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        mutableState.value = readPreferences()
    }

    override fun close() {
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    fun setStyle(value: ClockStyle) = edit(KEY_STYLE, value.name)

    fun setTimeFormat(value: TimeFormatPreference) = edit(KEY_TIME_FORMAT, value.name)

    fun setBrightness(value: BrightnessMode) = edit(KEY_BRIGHTNESS, value.name)

    fun setAccent(value: AccentChoice) {
        val style = state.value.style
        preferences.edit().putString(profileKey(style, KEY_ACCENT), value.name)
            .putBoolean(profileKey(style, "theme_colors"), false).apply()
        mutableState.value = readPreferences()
    }
    fun setThemeColors(value: Boolean) = edit(profileKey(state.value.style, "theme_colors"), value)
    fun setTintDigits(value: Boolean) = edit(profileKey(state.value.style, "tint_digits"), value)
    fun setDevicePlayer(value: Boolean) = edit("device_player", value)
    fun setMuseMotion(value: Boolean) = edit("muse_motion", value)
    fun setCatMotion(value: Boolean) = edit("cat_motion", value)

    fun setMotivationCategory(value: MotivationCategory) =
        edit(KEY_MOTIVATION_CATEGORY, value.name)

    fun setShowSeconds(value: Boolean) = edit(KEY_SHOW_SECONDS, value)

    fun setShowDate(value: Boolean) = edit(KEY_SHOW_DATE, value)

    fun setShowBattery(value: Boolean) = edit(KEY_SHOW_BATTERY, value)

    fun setKeepScreenOn(value: Boolean) = edit(KEY_KEEP_SCREEN_ON, value)

    fun setBurnInProtection(value: Boolean) = edit(KEY_BURN_IN_PROTECTION, value)
    fun setWallpaper(value: WallpaperChoice) = edit("wallpaper", value.name)
    fun setWallpaperLayout(value: WallpaperLayout) = edit("wallpaper_layout", value.name)
    fun setWallpaperDim(value: WallpaperDim) = edit("wallpaper_dim", value.name)
    fun setTypography(value: ClockTypography) = edit(profileKey(state.value.style, "typography"), value.name)
    fun setSpotifyMarquee(value: Boolean) = edit("spotify_marquee", value)
    fun setFlipAnimation(value: Boolean) = edit("flip_animation", value)

    private fun edit(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
        mutableState.value = readPreferences()
    }

    private fun edit(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
        mutableState.value = readPreferences()
    }

    fun preferencesFor(style: ClockStyle): ClockPreferences = readPreferences(style)

    private fun profileKey(style: ClockStyle, key: String) = "appearance_${style.name}_$key"

    private fun readPreferences(style: ClockStyle = enumValue(preferences.getString(KEY_STYLE, null), ClockStyle.PEBBLE)): ClockPreferences = ClockPreferences(
        style = style,
        timeFormat = enumValue(
            preferences.getString(KEY_TIME_FORMAT, null),
            TimeFormatPreference.SYSTEM,
        ),
        brightness = enumValue(
            preferences.getString(KEY_BRIGHTNESS, null),
            BrightnessMode.SYSTEM,
        ),
        accent = enumValue(preferences.getString(profileKey(style, KEY_ACCENT), null), AccentChoice.ROSE),
        motivationCategory = enumValue(
            preferences.getString(KEY_MOTIVATION_CATEGORY, null),
            MotivationCategory.ALL,
        ),
        showSeconds = preferences.getBoolean(KEY_SHOW_SECONDS, false),
        showDate = preferences.getBoolean(KEY_SHOW_DATE, true),
        showBattery = preferences.getBoolean(KEY_SHOW_BATTERY, true),
        keepScreenOn = preferences.getBoolean(KEY_KEEP_SCREEN_ON, true),
        burnInProtection = preferences.getBoolean(KEY_BURN_IN_PROTECTION, true),
        wallpaper = enumValue(preferences.getString("wallpaper", null), WallpaperChoice.MOON),
        wallpaperLayout = enumValue(preferences.getString("wallpaper_layout", null), WallpaperLayout.CINEMA),
        wallpaperDim = enumValue(preferences.getString("wallpaper_dim", null), WallpaperDim.BALANCED),
        typography = enumValue(preferences.getString(profileKey(style, "typography"), null), ClockTypography.ORIGINAL),
        spotifyMarquee = preferences.getBoolean("spotify_marquee", true),
        flipAnimation = preferences.getBoolean("flip_animation", true),
        themeColors = preferences.getBoolean(profileKey(style, "theme_colors"), true),
        tintDigits = preferences.getBoolean(profileKey(style, "tint_digits"), false),
        devicePlayer = preferences.getBoolean("device_player", true),
        museMotion = preferences.getBoolean("muse_motion", true),
        catMotion = preferences.getBoolean("cat_motion", true),
    )

    private inline fun <reified T : Enum<T>> enumValue(raw: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == raw } ?: fallback

    private companion object {
        const val FILE_NAME = "stilltime_settings"
        const val KEY_STYLE = "style"
        const val KEY_TIME_FORMAT = "time_format"
        const val KEY_BRIGHTNESS = "brightness"
        const val KEY_ACCENT = "accent"
        const val KEY_MOTIVATION_CATEGORY = "motivation_category"
        const val KEY_SHOW_SECONDS = "show_seconds"
        const val KEY_SHOW_DATE = "show_date"
        const val KEY_SHOW_BATTERY = "show_battery"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        const val KEY_BURN_IN_PROTECTION = "burn_in_protection"
    }
}
