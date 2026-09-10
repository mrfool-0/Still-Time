package com.mrfool.stilltime

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Bitmap
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import com.mrfool.stilltime.data.SettingsStore
import com.mrfool.stilltime.model.ClockStyle
import com.mrfool.stilltime.model.WallpaperChoice
import org.junit.Assert.assertEquals

class StandbyScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before fun resetStyle() {
        composeRule.runOnIdle { SettingsStore(composeRule.activity).use { it.setStyle(ClockStyle.PEBBLE) } }
    }

    @Test
    fun clockOpensSettingsAndShowsMuseControls() {
        composeRule.onRoot().performTouchInput { click(center) }
        composeRule.onNodeWithText("Customize").assertIsDisplayed()
        composeRule.onNodeWithTag("style_picker", useUnmergedTree = true)
            .performScrollToIndex(5)
        composeRule.onNodeWithTag("style_MUSE", useUnmergedTree = true).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("style_MUSE", useUnmergedTree = true).assertIsSelected()
        composeRule.onNodeWithTag("settings_list", useUnmergedTree = true).performScrollToIndex(1)
        composeRule.onNodeWithText("MUSE FEED").assertExists()
        composeRule.onNodeWithText("View source").assertExists()
    }

    @Test fun wallpaperSelectionPersistsAndSpotifyHasItsOwnCustomizeControl() {
        composeRule.onRoot().performTouchInput { click(center) }
        composeRule.onNodeWithTag("style_picker", useUnmergedTree = true).performScrollToIndex(11)
        composeRule.onNodeWithTag("style_WALLPAPER", useUnmergedTree = true).performClick()
        composeRule.onNodeWithTag("settings_list", useUnmergedTree = true).performScrollToIndex(1)
        composeRule.onNodeWithTag("wallpaper_NEON", useUnmergedTree = true).performClick()
        composeRule.onNodeWithTag("wallpaper_NEON", useUnmergedTree = true).assertIsSelected()
        composeRule.runOnIdle {
            SettingsStore(composeRule.activity).use { assertEquals(WallpaperChoice.NEON, it.state.value.wallpaper) }
        }
        composeRule.onNodeWithTag("settings_list", useUnmergedTree = true).performScrollToIndex(0)
        composeRule.onNodeWithTag("style_picker", useUnmergedTree = true).performScrollToIndex(12)
        composeRule.onNodeWithTag("style_SPOTIFY", useUnmergedTree = true).performClick()
        composeRule.onAllNodesWithContentDescription("Close settings").onLast().performClick()
        composeRule.onNodeWithTag("spotify_customize").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("CLOCK STYLE").assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription("Close settings").onLast().performClick()
    }

    @Test fun polishedMenuKeepsDoneVisibleAndPersistsMotionChoice() {
        composeRule.onRoot().performTouchInput { click(center) }
        composeRule.onNodeWithTag("style_picker", useUnmergedTree = true).performScrollToIndex(1)
        composeRule.onNodeWithTag("style_FLIP", useUnmergedTree = true).performClick()
        val directory = File(composeRule.activity.filesDir, "theme-captures").apply { mkdirs() }
        File(directory, "menu_polished.png").outputStream().use {
            composeRule.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        composeRule.onNodeWithTag("settings_list", useUnmergedTree = true).performScrollToIndex(1)
        composeRule.onNodeWithText("Flip animation").performClick().assertIsOff()
        composeRule.onNodeWithText("Done").assertIsDisplayed().performClick()
        composeRule.runOnIdle {
            SettingsStore(composeRule.activity).use {
                assertEquals(false, it.state.value.flipAnimation)
                it.setFlipAnimation(true)
            }
        }
    }

    @Test fun landscapeMenuKeepsPreviewAndExitUsable() {
        composeRule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        composeRule.waitUntil(5_000) { composeRule.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
        composeRule.onRoot().performTouchInput { click(center) }
        composeRule.onNodeWithTag("style_picker", useUnmergedTree = true).performScrollToIndex(1)
        composeRule.onNodeWithTag("style_FLIP", useUnmergedTree = true).performClick().assertIsDisplayed()
        composeRule.onNodeWithText("Done").assertIsDisplayed()
        val directory = File(composeRule.activity.filesDir, "theme-captures").apply { mkdirs() }
        File(directory, "menu_landscape.png").outputStream().use {
            composeRule.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        composeRule.onNodeWithTag("settings_list", useUnmergedTree = true).performScrollToIndex(4)
        composeRule.onNodeWithText("Done").assertIsDisplayed().performClick()
    }
}
