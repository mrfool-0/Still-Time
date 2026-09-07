package com.mrfool.stilltime.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mrfool.stilltime.model.ClockPreferences
import com.mrfool.stilltime.model.ClockStyle
import com.mrfool.stilltime.model.MotivationCategory
import com.mrfool.stilltime.model.MotivationEntry
import com.mrfool.stilltime.power.BatteryState
import com.mrfool.stilltime.ui.theme.StilltimeTheme
import com.mrfool.stilltime.util.TimeTextFormatter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

private val previewMoment = ZonedDateTime.of(
    2026,
    9,
    7,
    10,
    8,
    42,
    0,
    ZoneId.of("Asia/Kolkata"),
)

private val previewReadout = ClockReadout(
    moment = previewMoment,
    time = TimeTextFormatter.time(previewMoment, false, false, Locale.US),
    longDate = TimeTextFormatter.longDate(previewMoment, Locale.US),
    compactDate = TimeTextFormatter.compactDate(previewMoment, Locale.US),
    battery = BatteryState(percent = 84, isCharging = true),
    batteryLabel = "Charging · 84%",
    accessibilityLabel = "10:08 AM. Monday, September 7, 2026. Charging, 84 percent.",
    use24HourTime = false,
    motivation = MotivationEntry(
        category = MotivationCategory.WISDOM,
        text = "Look within; within is the fountain of all good.",
        attribution = "Marcus Aurelius",
        sourceTitle = "Meditations · c. 170–180 CE",
        sourceUrl = "https://www.gutenberg.org/ebooks/2680.txt.utf-8",
    ),
)

@Preview(name = "Pebble", group = "Clock faces", widthDp = 800, heightDp = 450)
@Composable
private fun PebblePreview() = FacePreview(ClockStyle.PEBBLE)

@Preview(name = "Flip", group = "Clock faces", widthDp = 800, heightDp = 450)
@Composable
private fun FlipPreview() = FacePreview(ClockStyle.FLIP)

@Preview(name = "Editorial", group = "Clock faces", widthDp = 800, heightDp = 450)
@Composable
private fun EditorialPreview() = FacePreview(ClockStyle.EDITORIAL)

@Preview(name = "Orbit", group = "Clock faces", widthDp = 800, heightDp = 450)
@Composable
private fun OrbitPreview() = FacePreview(ClockStyle.ORBIT)

@Preview(name = "Solar", group = "Clock faces", widthDp = 800, heightDp = 450)
@Composable
private fun SolarPreview() = FacePreview(ClockStyle.SOLAR)

@Preview(name = "Muse", group = "Clock faces", widthDp = 800, heightDp = 450)
@Composable
private fun MusePreview() = FacePreview(ClockStyle.MUSE)

@Preview(name = "Noir", group = "Clock faces", widthDp = 800, heightDp = 450)
@Composable
private fun NoirPreview() = FacePreview(ClockStyle.NOIR)

@Composable
private fun FacePreview(style: ClockStyle) {
    StilltimeTheme {
        ClockFace(
            preferences = ClockPreferences(style = style),
            readout = previewReadout,
        )
    }
}
