package com.mrfool.stilltime

import android.content.Context
import android.content.ContextWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.mrfool.stilltime.data.SettingsStore
import com.mrfool.stilltime.model.*
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class AppearanceProfilesTest {
    private fun isolatedContext(): Context = object : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
        private val prefix = "profile_test_${UUID.randomUUID()}_"
        override fun getApplicationContext(): Context = this
        override fun getSharedPreferences(name: String, mode: Int) = super.getSharedPreferences(prefix + name, mode)
    }

    @Test fun colorsAndFontsAreIndependentAndSurviveReopening() {
        val context = isolatedContext()
        SettingsStore(context).use { store ->
            store.setStyle(ClockStyle.FLIP); store.setAccent(AccentChoice.AQUA)
            store.setTintDigits(true); store.setTypography(ClockTypography.MONO)
            store.setStyle(ClockStyle.MUSE)
            assertTrue(store.state.value.themeColors)
            assertFalse(store.state.value.tintDigits)
            assertEquals(ClockTypography.ORIGINAL, store.state.value.typography)
            store.setAccent(AccentChoice.GOLD)
            assertEquals(AccentChoice.AQUA, store.preferencesFor(ClockStyle.FLIP).accent)
            assertEquals(AccentChoice.GOLD, store.preferencesFor(ClockStyle.MUSE).accent)
        }
        SettingsStore(context).use { store ->
            store.setStyle(ClockStyle.FLIP)
            assertEquals(AccentChoice.AQUA, store.state.value.accent)
            assertEquals(ClockTypography.MONO, store.state.value.typography)
            assertTrue(store.state.value.tintDigits)
            store.setThemeColors(true)
            assertFalse(store.preferencesFor(ClockStyle.MUSE).themeColors)
            assertEquals(AccentChoice.GOLD, store.preferencesFor(ClockStyle.MUSE).accent)
        }
    }

    @Test fun migrationPreservesOnlyThePreviouslySelectedTheme() {
        val context = isolatedContext()
        context.getSharedPreferences("stilltime_settings", Context.MODE_PRIVATE).edit()
            .putString("style", "WALLPAPER").putString("accent", "JADE")
            .putBoolean("theme_colors", false).putBoolean("tint_digits", true)
            .putString("typography", "CONDENSED").commit()
        repeat(2) {
            SettingsStore(context).use { store ->
                assertEquals(AccentChoice.JADE, store.preferencesFor(ClockStyle.WALLPAPER).accent)
                assertFalse(store.preferencesFor(ClockStyle.WALLPAPER).themeColors)
                assertTrue(store.preferencesFor(ClockStyle.NOIR).themeColors)
                store.setStyle(ClockStyle.NOIR)
            }
        }
    }
}
