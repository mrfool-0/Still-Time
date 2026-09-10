package com.mrfool.stilltime

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.mrfool.stilltime.ui.components.SplitFlapCard
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SplitFlapTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private fun image() = rule.onNodeWithTag("flap_MIN").captureToImage().asAndroidBitmap()

    @Test fun changedValueHasDistinctHingedFramesAndSettlesWithoutIdleMotion() {
        val value = mutableStateOf("59")
        rule.setContent { SplitFlapCard(value.value, "MIN", Color.Cyan, Modifier.size(160.dp, 180.dp)) }
        val before = image()
        rule.mainClock.autoAdvance = false
        rule.runOnIdle { value.value = "00" }
        rule.mainClock.advanceTimeBy(160)
        val folding = image()
        rule.mainClock.advanceTimeBy(280)
        val landing = image()
        rule.mainClock.advanceTimeBy(400)
        val settled = image()
        assertFalse("The upper leaf must move", before.sameAs(folding))
        assertFalse("The two hinge phases must differ", folding.sameAs(landing))
        assertFalse("The flap must settle onto the new number", landing.sameAs(settled))
        rule.mainClock.advanceTimeBy(60_000)
        assertTrue("No frames change while idle", settled.sameAs(image()))
        rule.onNodeWithContentDescription("MIN 00").assertExists()
        rule.mainClock.autoAdvance = true
    }

    @Test fun disablingMotionDuringFlipSnapsToLatestValue() {
        val value = mutableStateOf("08")
        val animate = mutableStateOf(true)
        rule.setContent { SplitFlapCard(value.value, "MIN", Color.Cyan, Modifier.size(160.dp, 180.dp), animate.value) }
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false
        rule.runOnIdle { value.value = "09" }
        rule.mainClock.advanceTimeBy(160)
        rule.runOnIdle { animate.value = false; value.value = "42" }
        rule.mainClock.advanceTimeBy(32)
        val snapped = image()
        rule.mainClock.advanceTimeBy(2_000)
        assertTrue(snapped.sameAs(image()))
        rule.onNodeWithContentDescription("MIN 42").assertExists()
        rule.mainClock.autoAdvance = true
    }
}
