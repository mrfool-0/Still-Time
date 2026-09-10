package com.mrfool.stilltime

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.mrfool.stilltime.model.*
import com.mrfool.stilltime.power.BatteryState
import com.mrfool.stilltime.spotify.*
import com.mrfool.stilltime.ui.components.*
import com.mrfool.stilltime.ui.theme.StilltimeTheme
import com.mrfool.stilltime.util.TimeTextFormatter
import java.io.File
import java.time.ZonedDateTime
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ThemeRenderTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val moment = ZonedDateTime.parse("2026-09-09T11:56:00+05:30[Asia/Kolkata]")
    private val readout = ClockReadout(moment, TimeTextFormatter.time(moment, true, false, Locale.US),
        "Wednesday, September 9", "Wed 9", BatteryState(84, true), "Charging · 84%", "11:56. Wednesday, September 9.", true,
        MotivationEntry(MotivationCategory.WISDOM, "Look within.", "Marcus Aurelius", "Meditations", "https://www.gutenberg.org/ebooks/2680"))

    private fun landscape() {
        rule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        rule.waitUntil(5_000) { rule.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
    }

    private fun capture(name: String) {
        rule.waitForIdle()
        val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
        val directory = File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir, "theme-captures").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        assertTrue(bitmap.width > 0 && bitmap.height > 0)
    }

    @Test fun referenceFacesAndAllWallpaperLayoutsRender() {
        landscape()
        val preferences = mutableStateOf(ClockPreferences(style = ClockStyle.PANORAMA))
        rule.setContent { StilltimeTheme { ClockFace(preferences.value, readout, Modifier.fillMaxSize()) } }
        for (style in listOf(ClockStyle.FLIP, ClockStyle.PANORAMA, ClockStyle.REDLINE, ClockStyle.CALENDAR, ClockStyle.CHROMA)) {
            rule.runOnIdle { preferences.value = ClockPreferences(style = style) }
            capture(style.name.lowercase())
        }
        for (wallpaper in WallpaperChoice.entries) {
            rule.runOnIdle { preferences.value = ClockPreferences(style = ClockStyle.WALLPAPER, wallpaper = wallpaper) }
            capture("wallpaper_${wallpaper.name.lowercase()}")
        }
        for (layout in WallpaperLayout.entries) {
            rule.runOnIdle { preferences.value = ClockPreferences(style = ClockStyle.WALLPAPER, wallpaperLayout = layout) }
            capture("wallpaper_${layout.name.lowercase()}")
        }
    }

    @Test fun spotifyHasEqualHalvesAndAccessibleWorkingTransportCallbacks() {
        landscape()
        var previous = 0
        var next = 0
        var toggles = 0
        val state = mutableStateOf(SpotifyUiState(status = SpotifyStatus.CONNECTED,
            title = "Preview track · UI test", artist = "Test fixture — no Spotify audio",
            artwork = android.graphics.BitmapFactory.decodeResource(rule.activity.resources, R.drawable.wallpaper_moon),
            timeline = PlaybackTimeline(72_000, 213_000, paused = true), canSkipPrevious = true, canSkipNext = true))
        rule.setContent {
            StilltimeTheme {
                SpotifyClock(ClockPreferences(style = ClockStyle.SPOTIFY), readout, state.value, false, true,
                    {}, { previous++ }, { toggles++ }, { next++ }, {}, Modifier.fillMaxSize())
            }
        }
        val left = rule.onNodeWithTag("spotify_clock_half").fetchSemanticsNode().boundsInRoot
        val right = rule.onNodeWithTag("spotify_player_half").fetchSemanticsNode().boundsInRoot
        assertEquals(left.width, right.width, 1f)
        assertTrue(left.right <= right.left + 1f)
        rule.onNodeWithContentDescription("Previous track").performClick()
        rule.onNodeWithContentDescription("Play").performClick()
        rule.onNodeWithContentDescription("Next track").performClick()
        rule.runOnIdle { assertEquals(1, previous); assertEquals(1, toggles); assertEquals(1, next) }
        rule.onNodeWithText("1:12").assertIsDisplayed()
        rule.onNodeWithText("3:33").assertIsDisplayed()
        val playBounds = rule.onNodeWithContentDescription("Play").fetchSemanticsNode().boundsInRoot
        assertTrue("Play button must fit fully within the screen", playBounds.bottom <= rule.onRoot().fetchSemanticsNode().boundsInRoot.bottom)
        capture("spotify_fixture")
        rule.runOnIdle { state.value = state.value.copy(canSkipNext = false, timeline = state.value.timeline.copy(paused = false)) }
        rule.onNodeWithContentDescription("Pause").assertIsDisplayed()
        rule.onNodeWithContentDescription("Next track").assertIsNotEnabled()
        rule.runOnIdle { state.value = SpotifyUiState(status = SpotifyStatus.APP_MISSING) }
        rule.onNodeWithText("Install Spotify").assertIsDisplayed()
        rule.onNodeWithContentDescription("Play").assertIsNotEnabled()
    }

    @Test fun portraitThemesRemainUsable() {
        rule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        rule.waitUntil(5_000) { rule.activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
        val preferences = mutableStateOf(ClockPreferences(style = ClockStyle.PANORAMA))
        rule.setContent { StilltimeTheme {
            if (preferences.value.style == ClockStyle.SPOTIFY) {
                SpotifyClock(preferences.value, readout, SpotifyUiState(status = SpotifyStatus.APP_MISSING), false, true,
                    {}, {}, {}, {}, {}, Modifier.fillMaxSize())
            } else ClockFace(preferences.value, readout, Modifier.fillMaxSize())
        } }
        for (style in listOf(ClockStyle.FLIP, ClockStyle.PANORAMA, ClockStyle.REDLINE, ClockStyle.CALENDAR, ClockStyle.CHROMA, ClockStyle.WALLPAPER, ClockStyle.SPOTIFY)) {
            rule.runOnIdle { preferences.value = ClockPreferences(style = style, wallpaperLayout = WallpaperLayout.GALLERY) }
            capture("${style.name.lowercase()}_portrait")
        }
        rule.onNodeWithText("Open Spotify").assertIsDisplayed()
        rule.onNodeWithText("Customize").assertIsDisplayed()
    }

    @Test fun appearancePreferencesVisiblyChangeEveryClockFace() {
        landscape()
        val preferences = mutableStateOf(ClockPreferences())
        rule.setContent { StilltimeTheme { ClockFace(preferences.value, readout, Modifier.fillMaxSize(), animationActive = false) } }
        for (style in ClockStyle.entries.filter { it != ClockStyle.SPOTIFY }) {
            rule.runOnIdle { preferences.value = ClockPreferences(style = style) }
            val before = rule.onRoot().captureToImage().asAndroidBitmap()
            rule.runOnIdle { preferences.value = preferences.value.copy(accent = AccentChoice.AQUA,
                themeColors = false, tintDigits = true, typography = ClockTypography.MONO) }
            val after = rule.onRoot().captureToImage().asAndroidBitmap()
            assertTrue("Appearance controls must affect $style", !before.sameAs(after))
            if (style in listOf(ClockStyle.FLIP, ClockStyle.CALENDAR, ClockStyle.CHROMA)) capture("appearance_${style.name.lowercase()}")
        }
    }

    @Test fun mediaAccessRequiresExplicitExplanationBeforeSettings() {
        landscape()
        var requests = 0
        rule.setContent { StilltimeTheme {
            SpotifyClock(ClockPreferences(style = ClockStyle.SPOTIFY), readout,
                SpotifyUiState(status = SpotifyStatus.ACCESS_REQUIRED), false, true,
                { requests++ }, {}, {}, {}, {}, Modifier.fillMaxSize())
        } }
        rule.onNodeWithText("Grant access").performClick()
        rule.runOnIdle { assertEquals(0, requests) }
        rule.onNodeWithText("Not now").performClick()
        rule.runOnIdle { assertEquals(0, requests) }
        rule.onNodeWithText("Grant access").performClick()
        rule.onNodeWithText("Open Android settings").performClick()
        rule.runOnIdle { assertEquals(1, requests) }
    }

    @Test fun originalSeriesReflectionsRenderInLandscape() = renderSeries(false)
    @Test fun originalSeriesReflectionsRenderInPortrait() = renderSeries(true)

    private fun renderSeries(portrait: Boolean) {
        rule.activityRule.scenario.onActivity { it.requestedOrientation = if (portrait)
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        rule.waitUntil(5_000) { rule.activity.resources.configuration.orientation == if (portrait)
            Configuration.ORIENTATION_PORTRAIT else Configuration.ORIENTATION_LANDSCAPE }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packs = com.mrfool.stilltime.data.MotivationLibrary.entries(context)
            .filter { it.category.inspiredBy != null }.groupBy { it.category }
        val entry = mutableStateOf(packs.values.first().first())
        rule.setContent { StilltimeTheme {
            ClockFace(ClockPreferences(style = ClockStyle.MUSE), readout.copy(motivation = entry.value),
                Modifier.fillMaxSize(), animationActive = false)
        } }
            for ((category, entries) in packs) {
                rule.runOnIdle { entry.value = entries.maxBy { it.text.length } }
                rule.onNodeWithText("SERIES-INSPIRED", useUnmergedTree = true).assertIsDisplayed()
                rule.onNodeWithText(entry.value.text, useUnmergedTree = true).assertIsDisplayed()
                capture("muse_${category.name.lowercase()}_${if (portrait) "portrait" else "landscape"}")
            }
    }
}
