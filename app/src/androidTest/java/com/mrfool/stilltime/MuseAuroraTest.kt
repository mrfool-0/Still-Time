package com.mrfool.stilltime

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.mrfool.stilltime.ui.components.MuseAurora
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MuseAuroraTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test fun lightMovesThenSettlesAndCanBeDisabled() {
        val thought = mutableStateOf("First reflection")
        val active = mutableStateOf(true)
        rule.setContent { MuseAurora(thought.value, Color(0xFFC8B6FF), active.value) }
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false
        rule.runOnIdle { thought.value = "Next reflection" }
        rule.mainClock.advanceTimeBy(100)
        val start = rule.onRoot().captureToImage().asAndroidBitmap()
        rule.mainClock.advanceTimeBy(1200)
        val middle = rule.onRoot().captureToImage().asAndroidBitmap()
        assertFalse("Light should drift during its finite transition", start.sameAs(middle))
        rule.mainClock.advanceTimeBy(3000)
        val settled = rule.onRoot().captureToImage().asAndroidBitmap()
        rule.mainClock.advanceTimeBy(10000)
        assertTrue("No idle animation", settled.sameAs(rule.onRoot().captureToImage().asAndroidBitmap()))
        rule.runOnUiThread { active.value = false; thought.value = "A new reflection" }
        rule.mainClock.advanceTimeBy(100)
        val disabled = rule.onRoot().captureToImage().asAndroidBitmap()
        rule.mainClock.advanceTimeBy(1000)
        assertTrue("Inactive backgrounds stay still", disabled.sameAs(rule.onRoot().captureToImage().asAndroidBitmap()))
        rule.runOnUiThread { active.value = true }
        rule.mainClock.advanceTimeBy(800)
        rule.runOnUiThread { active.value = false }
        rule.mainClock.advanceTimeBy(100)
        val interrupted = rule.onRoot().captureToImage().asAndroidBitmap()
        rule.mainClock.advanceTimeBy(1000)
        assertTrue("Deactivation cancels motion", interrupted.sameAs(rule.onRoot().captureToImage().asAndroidBitmap()))
    }
}

class MuseReducedMotionTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>(effectContext =
        object : androidx.compose.ui.MotionDurationScale { override val scaleFactor = 0f })

    @Test fun zeroDurationScaleSettlesOnNextFrame() {
        val thought = mutableStateOf("First reflection")
        val active = mutableStateOf(true)
        rule.setContent { MuseAurora(thought.value, Color.Cyan, active.value) }
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false
        rule.runOnIdle { thought.value = "Next reflection" }
        rule.mainClock.advanceTimeBy(64)
        val immediate = rule.onRoot().captureToImage().asAndroidBitmap()
        rule.mainClock.advanceTimeBy(1000)
        assertTrue("Reduced motion must not drift", immediate.sameAs(rule.onRoot().captureToImage().asAndroidBitmap()))
        rule.runOnUiThread { active.value = false }
        rule.mainClock.advanceTimeBy(64)
        assertTrue("Reduced motion renders the settled endpoint", immediate.sameAs(rule.onRoot().captureToImage().asAndroidBitmap()))
    }
}
