package com.mrfool.stilltime

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import org.junit.Rule
import org.junit.Test

class StandbyScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun clockOpensSettingsAndShowsMuseControls() {
        composeRule.onRoot().performTouchInput { click(center) }
        composeRule.onNodeWithText("Customize").assertIsDisplayed()
        composeRule.onNodeWithTag("style_picker", useUnmergedTree = true)
            .performScrollToIndex(5)
        composeRule.onNodeWithTag("style_MUSE", useUnmergedTree = true).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("style_MUSE", useUnmergedTree = true).assertIsSelected()
        composeRule.onNodeWithTag("settings_list", useUnmergedTree = true).performScrollToIndex(2)
        composeRule.onNodeWithText("MUSE FEED").assertExists()
        composeRule.onNodeWithText("View source").assertExists()
    }
}
