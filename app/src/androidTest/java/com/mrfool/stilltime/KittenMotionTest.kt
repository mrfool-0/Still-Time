package com.mrfool.stilltime

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.mrfool.stilltime.model.*
import com.mrfool.stilltime.power.BatteryState
import com.mrfool.stilltime.ui.components.*
import com.mrfool.stilltime.util.TimeTextFormatter
import java.time.ZonedDateTime
import java.util.Locale
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class KittenMotionTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    @Test fun kittenBlinksAndStopsWhenHidden() {
        val time = ZonedDateTime.parse("2026-09-10T10:08:00Z")
        val readout = ClockReadout(time, TimeTextFormatter.time(time, true, false, Locale.US), "Thursday, September 10",
            "Thu 10", BatteryState(80, false), "80%", "10:08", true,
            MotivationEntry(MotivationCategory.WISDOM, "Look within.", "Marcus Aurelius", "Meditations", "https://www.gutenberg.org/ebooks/2680"))
        val active = mutableStateOf(true)
        rule.mainClock.autoAdvance = false
        rule.setContent { KittenClock(ClockPreferences(style = ClockStyle.CAT, brightness = BrightnessMode.NIGHT), readout, Color(0xFFFFB7C5), Modifier, active.value) }
        rule.mainClock.advanceTimeBy(32)
        val awake = rule.onRoot().captureToImage().asAndroidBitmap()
        rule.mainClock.advanceTimeBy(200)
        assertTrue("No motion between gestures", awake.sameAs(rule.onRoot().captureToImage().asAndroidBitmap()))
        rule.mainClock.advanceTimeBy(640)
        assertFalse("Kitten must blink", awake.sameAs(rule.onRoot().captureToImage().asAndroidBitmap()))
        rule.runOnUiThread { active.value = false }
        rule.mainClock.advanceTimeBy(64)
        val resting = rule.onRoot().captureToImage().asAndroidBitmap()
        rule.mainClock.advanceTimeBy(30000)
        assertTrue("Hidden kitten is static", resting.sameAs(rule.onRoot().captureToImage().asAndroidBitmap()))
    }
}
