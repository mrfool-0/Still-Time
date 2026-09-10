package com.mrfool.stilltime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrfool.stilltime.model.*
import com.mrfool.stilltime.power.BatteryState
import com.mrfool.stilltime.util.TimeTextFormatter
import java.time.ZonedDateTime
import java.util.Locale

/** Static, scaled real faces: no miniature clocks, timers, connections, or animation loops. */
@Composable
fun ThemePreview(style: ClockStyle, preferences: ClockPreferences, modifier: Modifier = Modifier) {
    val sample = remember {
        val moment = ZonedDateTime.parse("2026-09-09T10:08:00+05:30[Asia/Kolkata]")
        ClockReadout(moment, TimeTextFormatter.time(moment, true, false, Locale.US),
            "Wednesday, September 9", "Wed 9", BatteryState(84, true), "84%", "", true,
            MotivationEntry(MotivationCategory.WISDOM, "Look within.", "Marcus Aurelius",
                "Meditations", "https://www.gutenberg.org/ebooks/2680"))
    }
    Layout(modifier = modifier.clip(RoundedCornerShape(12.dp)).clearAndSetSemantics {}, content = {
        if (style == ClockStyle.SPOTIFY) {
            Row(Modifier.fillMaxSize().background(Color(0xFF090C0B)).padding(28.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("10:08", Modifier.weight(1f), color = Color.White, fontSize = 58.sp)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(110.dp).background(Color(0xFF1A3828), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                        Text("♪", color = Color(0xFF1ED760), fontSize = 48.sp)
                    }
                    Text("‹     ▶     ›", color = Color.White, fontSize = 26.sp)
                }
            }
        } else ClockFace(preferences.copy(style = style, showSeconds = false, showBattery = false), sample,
            Modifier.fillMaxSize(), animationActive = false)
    }) { measurables, constraints ->
        val designWidth = 600.dp.roundToPx()
        val designHeight = 338.dp.roundToPx()
        val child = measurables.single().measure(Constraints.fixed(designWidth, designHeight))
        layout(constraints.maxWidth, constraints.maxHeight) {
            child.placeWithLayer(0, 0) {
                transformOrigin = TransformOrigin(0f, 0f)
                scaleX = constraints.maxWidth.toFloat() / designWidth
                scaleY = constraints.maxHeight.toFloat() / designHeight
            }
        }
    }
}

val ClockStyle.designNote: String get() = when (this) {
    ClockStyle.PEBBLE -> "Soft shapes · cozy color"
    ClockStyle.FLIP -> "Mechanical flip · warm minimalism"
    ClockStyle.EDITORIAL -> "Bold type · paper tones"
    ClockStyle.ORBIT -> "Analog precision · mint accents"
    ClockStyle.SOLAR -> "Sunlit gradients · playful warmth"
    ClockStyle.MUSE -> "203 sourced thoughts · slow rotation"
    ClockStyle.NOIR -> "Pure black · quiet essentials"
    ClockStyle.PANORAMA -> "Wide dial · crisp white"
    ClockStyle.REDLINE -> "Wide dial · midnight red"
    ClockStyle.CALENDAR -> "Analog time · your month at a glance"
    ClockStyle.CHROMA -> "Layered color · rounded numerals"
    ClockStyle.WALLPAPER -> "Your artwork · three compositions"
    ClockStyle.SPOTIFY -> "Time meets music · split-screen"
}
