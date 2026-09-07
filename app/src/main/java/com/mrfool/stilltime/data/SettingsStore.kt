package com.mrfool.stilltime.data

import android.content.Context
import android.content.SharedPreferences
import com.mrfool.stilltime.model.AccentChoice
import com.mrfool.stilltime.model.BrightnessMode
import com.mrfool.stilltime.model.ClockPreferences
import com.mrfool.stilltime.model.ClockStyle
import com.mrfool.stilltime.model.MotivationCategory
import com.mrfool.stilltime.model.TimeFormatPreference
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

    fun setAccent(value: AccentChoice) = edit(KEY_ACCENT, value.name)

    fun setMotivationCategory(value: MotivationCategory) =
        edit(KEY_MOTIVATION_CATEGORY, value.name)

    fun setShowSeconds(value: Boolean) = edit(KEY_SHOW_SECONDS, value)

    fun setShowDate(value: Boolean) = edit(KEY_SHOW_DATE, value)

    fun setShowBattery(value: Boolean) = edit(KEY_SHOW_BATTERY, value)

    fun setKeepScreenOn(value: Boolean) = edit(KEY_KEEP_SCREEN_ON, value)

    fun setBurnInProtection(value: Boolean) = edit(KEY_BURN_IN_PROTECTION, value)

    private fun edit(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
        mutableState.value = readPreferences()
    }

    private fun edit(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
        mutableState.value = readPreferences()
    }

    private fun readPreferences(): ClockPreferences = ClockPreferences(
        style = enumValue(preferences.getString(KEY_STYLE, null), ClockStyle.PEBBLE),
        timeFormat = enumValue(
            preferences.getString(KEY_TIME_FORMAT, null),
            TimeFormatPreference.SYSTEM,
        ),
        brightness = enumValue(
            preferences.getString(KEY_BRIGHTNESS, null),
            BrightnessMode.SYSTEM,
        ),
        accent = enumValue(preferences.getString(KEY_ACCENT, null), AccentChoice.ROSE),
        motivationCategory = enumValue(
            preferences.getString(KEY_MOTIVATION_CATEGORY, null),
            MotivationCategory.ALL,
        ),
        showSeconds = preferences.getBoolean(KEY_SHOW_SECONDS, false),
        showDate = preferences.getBoolean(KEY_SHOW_DATE, true),
        showBattery = preferences.getBoolean(KEY_SHOW_BATTERY, true),
        keepScreenOn = preferences.getBoolean(KEY_KEEP_SCREEN_ON, true),
        burnInProtection = preferences.getBoolean(KEY_BURN_IN_PROTECTION, true),
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
