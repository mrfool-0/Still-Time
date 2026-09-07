package com.mrfool.stilltime.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrfool.stilltime.model.ClockPreferences
import com.mrfool.stilltime.model.ClockStyle
import com.mrfool.stilltime.model.MotivationCategory
import com.mrfool.stilltime.model.MotivationEntry
import com.mrfool.stilltime.power.BatteryState
import com.mrfool.stilltime.util.TimeText
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Immutable
data class ClockReadout(
    val moment: ZonedDateTime,
    val time: TimeText,
    val longDate: String,
    val compactDate: String,
    val battery: BatteryState,
    val batteryLabel: String,
    val accessibilityLabel: String,
    val use24HourTime: Boolean,
    val motivation: MotivationEntry,
)

@Composable
fun ClockFace(
    preferences: ClockPreferences,
    readout: ClockReadout,
    modifier: Modifier = Modifier,
) {
    val accent = remember(preferences.accent) { Color(preferences.accent.seed) }
    val accessibleModifier = modifier.clearAndSetSemantics {
        contentDescription = readout.accessibilityLabel
    }

    when (preferences.style) {
        ClockStyle.PEBBLE -> PebbleClock(preferences, readout, accent, accessibleModifier)
        ClockStyle.FLIP -> FlipClock(preferences, readout, accent, accessibleModifier)
        ClockStyle.EDITORIAL -> EditorialClock(preferences, readout, accent, accessibleModifier)
        ClockStyle.ORBIT -> OrbitClock(preferences, readout, accent, accessibleModifier)
        ClockStyle.SOLAR -> SolarClock(preferences, readout, accent, accessibleModifier)
        ClockStyle.MUSE -> MuseClock(preferences, readout, accent, accessibleModifier)
        ClockStyle.NOIR -> NoirClock(preferences, readout, accent, accessibleModifier)
    }
}

@Composable
private fun PebbleClock(
    preferences: ClockPreferences,
    readout: ClockReadout,
    accent: Color,
    modifier: Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isPortrait = maxHeight > maxWidth
        val density = LocalDensity.current
        val timeSize = with(density) {
            (if (isPortrait) maxWidth * 0.22f else maxHeight * 0.34f).toSp()
        }
        val secondSize = with(density) { (timeSize.toDp() * 0.28f).toSp() }

        Canvas(Modifier.fillMaxSize()) {
            drawRect(Color(0xFF191825))
            drawCircle(
                color = accent.copy(alpha = 0.18f),
                radius = size.minDimension * 0.34f,
                center = Offset(size.width * 0.12f, size.height * 0.18f),
            )
            drawCircle(
                color = Color(0xFFC8B6FF).copy(alpha = 0.13f),
                radius = size.minDimension * 0.3f,
                center = Offset(size.width * 0.92f, size.height * 0.83f),
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.035f),
                topLeft = Offset(size.width * 0.15f, size.height * 0.16f),
                size = Size(size.width * 0.7f, size.height * 0.68f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.minDimension * 0.08f),
                style = Stroke(width = 1.2.dp.toPx()),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = readout.time.digital,
                    color = Color(0xFFFFF7F1),
                    fontSize = timeSize,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-5).sp,
                    maxLines = 1,
                )
                if (preferences.showSeconds || readout.time.period != null) {
                    Spacer(Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.padding(bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (preferences.showSeconds) {
                            Text(
                                text = readout.time.second,
                                color = accent,
                                fontSize = secondSize,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        readout.time.period?.let { period ->
                            Surface(
                                color = accent,
                                contentColor = Color(0xFF301D25),
                                shape = RoundedCornerShape(100),
                            ) {
                                Text(
                                    text = period,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
            if (preferences.showDate || preferences.showBattery) {
                Spacer(Modifier.height(if (isPortrait) 28.dp else 18.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (preferences.showDate) {
                        MetadataPill(readout.compactDate, Color(0xFFFFF7F1), accent)
                    }
                    if (preferences.showBattery) {
                        MetadataPill(readout.batteryLabel, Color(0xFFFFF7F1), accent)
                    }
                }
            }
        }

        Sparkle(
            color = accent,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(38.dp)
                .size(if (isPortrait) 28.dp else 34.dp),
        )
    }
}

@Composable
private fun MetadataPill(text: String, content: Color, accent: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .background(Color.White.copy(alpha = 0.07f))
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(5.dp).clip(CircleShape).background(accent))
        Text(text = text, color = content.copy(alpha = 0.82f), fontSize = 12.sp)
    }
}

@Composable
private fun Sparkle(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val c = center
        drawLine(color, Offset(c.x, 0f), Offset(c.x, size.height), 2.dp.toPx(), StrokeCap.Round)
        drawLine(color, Offset(0f, c.y), Offset(size.width, c.y), 2.dp.toPx(), StrokeCap.Round)
        drawLine(
            color.copy(alpha = 0.58f),
            Offset(size.width * 0.2f, size.height * 0.2f),
            Offset(size.width * 0.8f, size.height * 0.8f),
            1.dp.toPx(),
            StrokeCap.Round,
        )
        drawLine(
            color.copy(alpha = 0.58f),
            Offset(size.width * 0.8f, size.height * 0.2f),
            Offset(size.width * 0.2f, size.height * 0.8f),
            1.dp.toPx(),
            StrokeCap.Round,
        )
    }
}

@Composable
private fun FlipClock(
    preferences: ClockPreferences,
    readout: ClockReadout,
    accent: Color,
    modifier: Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isPortrait = maxHeight > maxWidth
        val cardHeight = if (isPortrait) maxWidth * 0.54f else maxHeight * 0.56f
        val values = buildList {
            add(readout.time.paddedHour)
            add(readout.time.minute)
            if (preferences.showSeconds) add(readout.time.second)
        }

        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF211D1B), Color(0xFF0D0C0C)),
                ),
            )
            val lineY = size.height * 0.77f
            drawLine(
                color = accent.copy(alpha = 0.18f),
                start = Offset(size.width * 0.08f, lineY),
                end = Offset(size.width * 0.92f, lineY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 10.dp.toPx())),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(if (isPortrait) 0.9f else 0.78f)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(cardHeight),
                horizontalArrangement = Arrangement.spacedBy(if (isPortrait) 8.dp else 14.dp),
            ) {
                values.forEachIndexed { index, value ->
                    FlipCard(
                        value = value,
                        label = when (index) {
                            0 -> "HOUR"
                            1 -> "MIN"
                            else -> "SEC"
                        },
                        accent = accent,
                        compact = values.size == 3,
                    )
                }
            }
            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (preferences.showDate) readout.longDate.uppercase() else "STILLTIME",
                    color = Color(0xFFE9E1D8).copy(alpha = 0.68f),
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    maxLines = 1,
                )
                if (preferences.showBattery) {
                    Text(
                        text = readout.batteryLabel.uppercase(),
                        color = accent.copy(alpha = 0.88f),
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.FlipCard(
    value: String,
    label: String,
    accent: Color,
    compact: Boolean,
) {
    Surface(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        color = Color(0xFF292625),
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 12.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.48f)
                    .align(Alignment.TopCenter)
                    .background(Color.White.copy(alpha = 0.025f)),
            )
            Text(
                text = value,
                color = Color(0xFFF5EEE7),
                fontSize = if (compact) 62.sp else 82.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-5).sp,
                maxLines = 1,
            )
            HorizontalDivider(
                modifier = Modifier.align(Alignment.Center),
                thickness = 2.dp,
                color = Color(0xFF100F0F),
            )
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(Modifier.size(width = 5.dp, height = 18.dp).background(Color(0xFF0A0909)))
                Box(Modifier.size(width = 5.dp, height = 18.dp).background(Color(0xFF0A0909)))
            }
            Text(
                text = label,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                color = accent.copy(alpha = 0.74f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
        }
    }
}

@Composable
private fun EditorialClock(
    preferences: ClockPreferences,
    readout: ClockReadout,
    accent: Color,
    modifier: Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(Color(0xFFF0EDE5)),
    ) {
        val isPortrait = maxHeight > maxWidth
        val density = LocalDensity.current
        val mainSize = with(density) {
            (if (isPortrait) maxWidth * 0.23f else maxHeight * 0.39f).toSp()
        }

        Canvas(Modifier.fillMaxSize()) {
            drawLine(
                color = Color(0xFF1E1B18).copy(alpha = 0.18f),
                start = Offset(size.width * 0.07f, size.height * 0.13f),
                end = Offset(size.width * 0.93f, size.height * 0.13f),
                strokeWidth = 1.dp.toPx(),
            )
            drawLine(
                color = Color(0xFF1E1B18).copy(alpha = 0.18f),
                start = Offset(size.width * 0.07f, size.height * 0.87f),
                end = Offset(size.width * 0.93f, size.height * 0.87f),
                strokeWidth = 1.dp.toPx(),
            )
        }

        Text(
            text = "STILL / ${readout.moment.year}",
            modifier = Modifier.align(Alignment.TopStart).padding(26.dp),
            color = Color(0xFF1E1B18).copy(alpha = 0.74f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.4.sp,
        )
        Text(
            text = "№ ${readout.moment.dayOfYear.toString().padStart(3, '0')}",
            modifier = Modifier.align(Alignment.TopEnd).padding(26.dp),
            color = accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )

        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = readout.time.digital,
                color = Color(0xFF171513),
                fontSize = mainSize,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-6).sp,
                maxLines = 1,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (preferences.showDate) {
                    Text(
                        text = readout.longDate,
                        color = Color(0xFF312D29).copy(alpha = 0.82f),
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = if (isPortrait) 15.sp else 17.sp,
                    )
                }
                if (preferences.showSeconds) {
                    Text(
                        text = "  /  ${readout.time.second}",
                        color = accent,
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                    )
                }
            }
        }

        if (preferences.showBattery) {
            Text(
                text = readout.batteryLabel,
                modifier = Modifier.align(Alignment.BottomStart).padding(26.dp),
                color = Color(0xFF1E1B18).copy(alpha = 0.66f),
                fontSize = 11.sp,
                letterSpacing = 1.sp,
            )
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(26.dp)
                .size(18.dp)
                .background(accent),
        )
    }
}

@Composable
private fun OrbitClock(
    preferences: ClockPreferences,
    readout: ClockReadout,
    accent: Color,
    modifier: Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color(0xFF071C1D))) {
        val isPortrait = maxHeight > maxWidth
        val diameter = minOf(
            maxWidth * if (isPortrait) 0.76f else 0.48f,
            maxHeight * if (isPortrait) 0.52f else 0.76f,
        )
        val handColor = Color(0xFFF1F4EB)

        Canvas(
            modifier = Modifier.size(diameter).align(Alignment.Center),
        ) {
            val radius = size.minDimension / 2f
            val c = center
            drawCircle(Color(0xFF0C292A), radius)
            drawCircle(
                color = Color(0xFFB7D8D3).copy(alpha = 0.24f),
                radius = radius * 0.91f,
                style = Stroke(width = 1.dp.toPx()),
            )
            repeat(60) { tick ->
                val angle = (tick * 6f - 90f) * PI.toFloat() / 180f
                val major = tick % 5 == 0
                val outer = radius * 0.84f
                val inner = radius * if (major) 0.73f else 0.79f
                drawLine(
                    color = if (major) handColor.copy(alpha = 0.84f)
                    else handColor.copy(alpha = 0.22f),
                    start = Offset(c.x + cos(angle) * inner, c.y + sin(angle) * inner),
                    end = Offset(c.x + cos(angle) * outer, c.y + sin(angle) * outer),
                    strokeWidth = if (major) 2.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = (readout.moment.minute + readout.moment.second / 60f) * 6f,
                useCenter = false,
                topLeft = Offset(radius * 0.03f, radius * 0.03f),
                size = Size(radius * 1.94f, radius * 1.94f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )

            val hourAngle = ((readout.moment.hour % 12) + readout.moment.minute / 60f) * 30f
            val minuteAngle = (readout.moment.minute + readout.moment.second / 60f) * 6f
            drawClockHand(hourAngle, radius * 0.43f, 6.dp.toPx(), handColor)
            drawClockHand(minuteAngle, radius * 0.64f, 4.dp.toPx(), handColor)
            if (preferences.showSeconds) {
                drawClockHand(
                    readout.moment.second * 6f,
                    radius * 0.69f,
                    1.5.dp.toPx(),
                    accent,
                )
            }
            drawCircle(accent, 6.dp.toPx(), c)
            drawCircle(Color(0xFF071C1D), 2.dp.toPx(), c)
        }

        Column(
            modifier = Modifier.align(if (isPortrait) Alignment.BottomCenter else Alignment.CenterEnd)
                .padding(if (isPortrait) 34.dp else 30.dp),
            horizontalAlignment = if (isPortrait) Alignment.CenterHorizontally else Alignment.End,
        ) {
            Text(
                text = readout.time.spoken,
                color = handColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            )
            if (preferences.showDate) {
                Text(
                    text = readout.compactDate,
                    color = Color(0xFFB7D8D3).copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                )
            }
        }

        if (preferences.showBattery) {
            Text(
                text = readout.batteryLabel,
                modifier = Modifier.align(Alignment.TopStart).padding(28.dp),
                color = accent.copy(alpha = 0.76f),
                fontSize = 10.sp,
                letterSpacing = 1.1.sp,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawClockHand(
    degrees: Float,
    length: Float,
    width: Float,
    color: Color,
) {
    rotate(degrees, pivot = center) {
        drawLine(
            color = color,
            start = Offset(center.x, center.y + length * 0.1f),
            end = Offset(center.x, center.y - length),
            strokeWidth = width,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun SolarClock(
    preferences: ClockPreferences,
    readout: ClockReadout,
    accent: Color,
    modifier: Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isPortrait = maxHeight > maxWidth
        val density = LocalDensity.current
        val timeSize = with(density) {
            (if (isPortrait) maxWidth * 0.2f else maxHeight * 0.32f).toSp()
        }
        val minutes = readout.moment.hour * 60 + readout.moment.minute
        val dayProgress = ((minutes - 360f) / 720f).coerceIn(0f, 1f)
        val isDay = minutes in 360..1080
        val topColor = if (isDay) Color(0xFF98CAE4) else Color(0xFF11152E)
        val bottomColor = if (isDay) Color(0xFFF5C6A5) else Color(0xFF2B2141)

        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(listOf(topColor, bottomColor)))
            val progress = if (isDay) dayProgress else ((minutes + 360) % 1440) / 720f
            val orbX = size.width * (0.09f + progress.coerceIn(0f, 1f) * 0.82f)
            val orbY = size.height * (0.7f - sin(progress.coerceIn(0f, 1f) * PI).toFloat() * 0.45f)
            val orbColor = if (isDay) Color(0xFFFFE3A3) else Color(0xFFE8E6FF)
            drawCircle(orbColor.copy(alpha = 0.1f), size.minDimension * 0.17f, Offset(orbX, orbY))
            drawCircle(orbColor.copy(alpha = 0.22f), size.minDimension * 0.12f, Offset(orbX, orbY))
            drawCircle(orbColor, size.minDimension * 0.075f, Offset(orbX, orbY))

            repeat(4) { index ->
                val y = size.height * (0.72f + index * 0.075f)
                drawArc(
                    color = Color.White.copy(alpha = 0.13f - index * 0.02f),
                    startAngle = 188f,
                    sweepAngle = 164f,
                    useCenter = false,
                    topLeft = Offset(size.width * (0.08f + index * 0.025f), y),
                    size = Size(size.width * (0.84f - index * 0.05f), size.height * 0.22f),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = readout.time.digital,
                color = if (isDay) Color(0xFF20283A) else Color(0xFFF8F1EB),
                fontSize = timeSize,
                fontWeight = FontWeight.Light,
                letterSpacing = (-4).sp,
                maxLines = 1,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (preferences.showDate) {
                    Text(
                        text = readout.compactDate,
                        color = if (isDay) Color(0xFF28344A).copy(alpha = 0.76f)
                        else Color.White.copy(alpha = 0.68f),
                        fontSize = 12.sp,
                        letterSpacing = 1.2.sp,
                    )
                }
                if (preferences.showSeconds) {
                    Text(
                        text = readout.time.second,
                        color = accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        if (preferences.showBattery) {
            Text(
                text = readout.batteryLabel,
                modifier = Modifier.align(Alignment.BottomCenter).padding(26.dp),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
            )
        }
    }
}

@Composable
private fun MuseClock(
    preferences: ClockPreferences,
    readout: ClockReadout,
    accent: Color,
    modifier: Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isPortrait = maxHeight > maxWidth
        val quoteLength = readout.motivation.text.length
        val quoteSize = when {
            isPortrait && quoteLength > 120 -> 27.sp
            isPortrait -> 34.sp
            quoteLength > 120 -> 30.sp
            quoteLength > 80 -> 36.sp
            else -> 43.sp
        }
        val label = when (readout.motivation.category) {
            MotivationCategory.WISDOM -> "FAMOUS WISDOM"
            MotivationCategory.LIFE_FACT -> "LIFE NOTE"
            MotivationCategory.ANIME -> "ANIME MOMENT"
            MotivationCategory.ALL -> "MUSE"
        }
        val openingMark = if (readout.motivation.category == MotivationCategory.LIFE_FACT) {
            "✦"
        } else {
            "“"
        }

        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF16162A), Color(0xFF24203C), Color(0xFF151521)),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                ),
            )
            drawCircle(
                color = accent.copy(alpha = 0.12f),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.9f, size.height * 0.08f),
            )
            drawCircle(
                color = Color(0xFF9AD9FF).copy(alpha = 0.07f),
                radius = size.minDimension * 0.34f,
                center = Offset(size.width * 0.05f, size.height * 0.95f),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = readout.time.digital,
                    color = Color(0xFFFFFBF7),
                    fontSize = if (isPortrait) 24.sp else 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-1).sp,
                )
                if (preferences.showSeconds) {
                    Text(
                        text = ":${readout.time.second}",
                        modifier = Modifier.padding(bottom = 2.dp),
                        color = accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                text = label,
                color = accent,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(if (isPortrait) 0.84f else 0.7f)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = openingMark,
                color = accent,
                fontFamily = FontFamily.Serif,
                fontSize = 58.sp,
                lineHeight = 36.sp,
            )
            Text(
                text = readout.motivation.text,
                color = Color(0xFFFFFBF7),
                fontFamily = FontFamily.Serif,
                fontSize = quoteSize,
                fontWeight = FontWeight.Normal,
                lineHeight = quoteSize * 1.18f,
            )
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(26.dp).height(2.dp).background(accent))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = readout.motivation.attribution,
                    color = Color.White.copy(alpha = 0.76f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.4.sp,
                )
            }
            Text(
                text = readout.motivation.sourceTitle,
                modifier = Modifier.padding(start = 36.dp, top = 4.dp),
                color = Color.White.copy(alpha = 0.44f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (preferences.showDate) readout.compactDate else "STILLTIME MUSE",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                letterSpacing = 1.1.sp,
            )
            if (preferences.showBattery) {
                Text(
                    text = readout.batteryLabel,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                )
            }
        }
    }
}

@Composable
private fun NoirClock(
    preferences: ClockPreferences,
    readout: ClockReadout,
    accent: Color,
    modifier: Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color.Black)) {
        val isPortrait = maxHeight > maxWidth
        val canvasHeight = if (isPortrait) maxWidth * 0.48f else maxHeight * 0.56f
        val hour = if (readout.use24HourTime) {
            readout.moment.hour
        } else {
            ((readout.moment.hour + 11) % 12) + 1
        }
        val digits = buildString {
            append(hour.toString().padStart(2, '0'))
            append(':')
            append(readout.moment.minute.toString().padStart(2, '0'))
            if (preferences.showSeconds) {
                append(':')
                append(readout.moment.second.toString().padStart(2, '0'))
            }
        }

        SevenSegmentDisplay(
            value = digits,
            color = accent,
            modifier = Modifier
                .fillMaxWidth(if (isPortrait) 0.9f else 0.82f)
                .height(canvasHeight)
                .align(Alignment.Center),
        )

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(26.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (preferences.showDate) {
                NoirLabel(readout.compactDate, accent)
            }
            readout.time.period?.let { NoirLabel(it, accent) }
            if (preferences.showBattery) {
                NoirLabel(readout.batteryLabel, accent)
            }
        }
    }
}

@Composable
private fun NoirLabel(text: String, accent: Color) {
    Text(
        text = text.uppercase(),
        color = accent.copy(alpha = 0.52f),
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        letterSpacing = 1.6.sp,
    )
}

@Composable
private fun SevenSegmentDisplay(
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val digitCount = value.count(Char::isDigit)
        val colonCount = value.count { it == ':' }
        val unitWidth = size.width / (digitCount + colonCount * 0.34f + (value.length - 1) * 0.12f)
        val digitWidth = unitWidth
        val digitHeight = min(size.height, digitWidth * 1.82f)
        val totalWidth = digitCount * digitWidth + colonCount * digitWidth * 0.34f +
            (value.length - 1) * digitWidth * 0.12f
        var x = (size.width - totalWidth) / 2f
        val y = (size.height - digitHeight) / 2f

        value.forEach { character ->
            if (character == ':') {
                val radius = digitWidth * 0.065f
                drawCircle(color, radius, Offset(x + digitWidth * 0.17f, y + digitHeight * 0.36f))
                drawCircle(color, radius, Offset(x + digitWidth * 0.17f, y + digitHeight * 0.67f))
                x += digitWidth * 0.34f
            } else {
                drawSevenSegmentDigit(character, Offset(x, y), digitWidth, digitHeight, color)
                x += digitWidth
            }
            x += digitWidth * 0.12f
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSevenSegmentDigit(
    character: Char,
    origin: Offset,
    width: Float,
    height: Float,
    color: Color,
) {
    val lit = when (character) {
        '0' -> setOf(0, 1, 2, 3, 4, 5)
        '1' -> setOf(1, 2)
        '2' -> setOf(0, 1, 3, 4, 6)
        '3' -> setOf(0, 1, 2, 3, 6)
        '4' -> setOf(1, 2, 5, 6)
        '5' -> setOf(0, 2, 3, 5, 6)
        '6' -> setOf(0, 2, 3, 4, 5, 6)
        '7' -> setOf(0, 1, 2)
        '8' -> setOf(0, 1, 2, 3, 4, 5, 6)
        '9' -> setOf(0, 1, 2, 3, 5, 6)
        else -> emptySet()
    }
    val inset = width * 0.14f
    val stroke = width * 0.105f
    val mid = origin.y + height / 2f
    val left = origin.x + inset
    val right = origin.x + width - inset
    val top = origin.y + inset
    val bottom = origin.y + height - inset
    val segments = listOf(
        Offset(left, top) to Offset(right, top),
        Offset(right, top) to Offset(right, mid),
        Offset(right, mid) to Offset(right, bottom),
        Offset(left, bottom) to Offset(right, bottom),
        Offset(left, mid) to Offset(left, bottom),
        Offset(left, top) to Offset(left, mid),
        Offset(left, mid) to Offset(right, mid),
    )
    segments.forEachIndexed { index, segment ->
        if (index in lit) {
            drawLine(
                color = color,
                start = segment.first,
                end = segment.second,
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}
