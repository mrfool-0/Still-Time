package com.mrfool.stilltime.ui.components

import android.app.AlarmManager
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import com.mrfool.stilltime.R
import com.mrfool.stilltime.model.ClockPreferences
import com.mrfool.stilltime.util.CalendarMonth
import java.time.Instant
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val NightRed = Color(0xFFEE343F)

private fun DrawScope.label(text: String, x: Float, y: Float, height: Float, color: Color, paint: Paint) {
    paint.color = color.toArgb()
    paint.textSize = height
    paint.textAlign = Paint.Align.CENTER
    drawIntoCanvas { it.nativeCanvas.drawText(text, x, y - (paint.ascent() + paint.descent()) / 2, paint) }
}

@Composable
fun PanoramaClock(preferences: ClockPreferences, readout: ClockReadout, red: Boolean, modifier: Modifier) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val alarm = remember(readout.moment.toEpochSecond() / 60) {
        context.getSystemService(AlarmManager::class.java)?.nextAlarmClock?.triggerTime?.let {
            Instant.ofEpochMilli(it).atZone(readout.moment.zone)
                .format(DateTimeFormatter.ofPattern(if (readout.use24HourTime) "HH:mm" else "h:mm a", locale))
        } ?: "NO ALARM"
    }
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans-serif", Typeface.NORMAL) } }
    val ink = if (red) NightRed else Color.White
    val highlight = if (red) NightRed else Color(0xFFFFAC58)
    Canvas(modifier.background(Color.Black).padding(22.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2
        val halfW = size.width * .48f
        val portrait = size.height > size.width
        val halfH = if (portrait) min(size.height * .36f, halfW * 1.1f) else size.height * .46f
        for (tick in 0 until 60) {
            val angle = Math.toRadians(tick * 6.0)
            val dx = sin(angle).toFloat()
            val dy = -cos(angle).toFloat()
            val distance = min(halfW / abs(dx).coerceAtLeast(.001f), halfH / abs(dy).coerceAtLeast(.001f))
            val major = tick % 5 == 0
            val cardinal = tick % 15 == 0
            val inset = if (major && !cardinal) .28f else .11f
            drawLine(ink.copy(alpha = if (major) .52f else .20f),
                Offset(cx + dx * distance * (1 - inset), cy + dy * distance * (1 - inset)),
                Offset(cx + dx * distance, cy + dy * distance),
                strokeWidth = if (major) 2.4.dp.toPx() else 1.3.dp.toPx(), cap = StrokeCap.Round)
        }
        val numberSize = size.minDimension * .145f
        label("12", cx, cy - halfH * .69f, numberSize, ink, paint)
        label("6", cx, cy + halfH * .72f, numberSize, ink, paint)
        label("9", cx - halfW * .70f, cy, numberSize, ink, paint)
        label("3", cx + halfW * .70f, cy, numberSize, ink, paint)
        val metaSize = size.minDimension * .044f
        label(alarm, if (portrait) cx else cx - halfW * .39f,
            if (portrait) cy + halfH * 1.35f else cy, metaSize, highlight.copy(alpha = .85f), paint)
        if (preferences.showDate) {
            label(readout.moment.format(DateTimeFormatter.ofPattern("EEE d", locale)).uppercase(locale),
                if (portrait) cx else cx + halfW * .39f, if (portrait) cy + halfH * 1.52f else cy,
                metaSize, if (red) ink else Color(0xFFDF687C), paint)
        }
        fun hand(degrees: Double, length: Float, width: Float, color: Color) {
            val rad = Math.toRadians(degrees)
            val direction = Offset(sin(rad).toFloat(), -cos(rad).toFloat())
            drawLine(color, Offset(cx, cy) - direction * 9.dp.toPx(), Offset(cx, cy) + direction * length,
                width, StrokeCap.Round)
        }
        val handRadius = min(halfW, halfH)
        hand((readout.moment.hour % 12) * 30.0 + readout.moment.minute * .5, handRadius * .49f, 9.dp.toPx(), ink)
        hand(readout.moment.minute * 6.0, handRadius * .86f, 7.dp.toPx(), ink)
        if (preferences.showSeconds) hand(readout.moment.second * 6.0, handRadius, 1.5.dp.toPx(), highlight)
        drawCircle(highlight, 5.dp.toPx(), Offset(cx, cy))
        drawCircle(Color.Black, 2.dp.toPx(), Offset(cx, cy))
    }
}

@Composable
fun CalendarClock(preferences: ClockPreferences, readout: ClockReadout, modifier: Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val month = YearMonth.from(readout.moment)
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    val cells = remember(month, firstDay) { CalendarMonth.cells(month, firstDay) }
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL) } }
    BoxWithConstraints(modifier.background(Color.Black).padding(20.dp)) {
        val analog: @Composable (Modifier) -> Unit = { dialModifier ->
            Canvas(dialModifier.padding(10.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension * .47f
                for (tick in 0 until 60) {
                    val rad = Math.toRadians(tick * 6.0)
                    val direction = Offset(sin(rad).toFloat(), -cos(rad).toFloat())
                    drawLine(NightRed.copy(alpha = if (tick % 5 == 0) .8f else .36f),
                        center + direction * radius * .93f, center + direction * radius,
                        if (tick % 5 == 0) 2.3.dp.toPx() else 1.2.dp.toPx())
                }
                for (hour in 1..12) {
                    val rad = Math.toRadians(hour * 30.0)
                    label(hour.toString(), center.x + sin(rad).toFloat() * radius * .76f,
                        center.y - cos(rad).toFloat() * radius * .76f, radius * .22f, NightRed, paint)
                }
                fun hand(degrees: Double, length: Float, width: Float) {
                    val rad = Math.toRadians(degrees)
                    drawLine(NightRed, center, center + Offset(sin(rad).toFloat(), -cos(rad).toFloat()) * radius * length,
                        width.dp.toPx(), StrokeCap.Round)
                }
                hand((readout.moment.hour % 12) * 30.0 + readout.moment.minute * .5, .52f, 6f)
                hand(readout.moment.minute * 6.0, .91f, 5f)
                if (preferences.showSeconds) hand(readout.moment.second * 6.0, .95f, 1f)
                drawCircle(NightRed, 5.dp.toPx(), center)
                drawCircle(Color.Black, 2.dp.toPx(), center)
            }
        }
        val calendar: @Composable (Modifier) -> Unit = { gridModifier ->
            Canvas(gridModifier.padding(14.dp)) {
                val cellW = size.width / 7
                val cellH = size.height / 8
                paint.textAlign = Paint.Align.LEFT
                label(month.month.getDisplayName(TextStyle.FULL, locale).uppercase(locale),
                    size.width / 2, cellH * .35f, cellH * .54f, NightRed, paint)
                for (col in 0..6) {
                    label(firstDay.plus(col.toLong()).getDisplayName(TextStyle.NARROW, locale),
                        cellW * (col + .5f), cellH * 1.65f, cellH * .40f, NightRed.copy(alpha = .65f), paint)
                }
                cells.forEachIndexed { index, day ->
                    if (day != null) {
                        val x = cellW * (index % 7 + .5f)
                        val y = cellH * (index / 7 + 2.75f)
                        val selected = day == readout.moment.dayOfMonth
                        if (selected) drawCircle(NightRed, min(cellW, cellH) * .43f, Offset(x, y))
                        label(day.toString(), x, y, cellH * .43f, if (selected) Color.Black else NightRed.copy(alpha = .84f), paint)
                    }
                }
            }
        }
        if (maxWidth > maxHeight) {
            Row(Modifier.fillMaxSize()) { analog(Modifier.weight(1f).fillMaxHeight()); calendar(Modifier.weight(1f).fillMaxHeight()) }
        } else {
            Column(Modifier.fillMaxSize()) { analog(Modifier.weight(1f).fillMaxWidth()); calendar(Modifier.weight(1f).fillMaxWidth()) }
        }
    }
}

@Composable
fun ChromaClock(preferences: ClockPreferences, readout: ClockReadout, modifier: Modifier) {
    val context = LocalContext.current
    val paint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = ResourcesCompat.getFont(context, R.font.fredoka)
            fontVariationSettings = "'wght' 700"
        }
    }
    Box(modifier.background(Color.Black), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(28.dp)) {
            val digits = readout.time.paddedHour + readout.time.minute
            paint.textSize = 300f
            val advances = digits.map { paint.measureText(it.toString()) * .80f }
            val width = advances.sum() + 58f
            val scale = min(size.width / width, size.height * .65f / 300f)
            val colors = listOf(0xFFEF464E, 0xFFF99536, 0xFFDCE16D, 0xFFD6EAC0)
            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                native.withTranslation((size.width - width * scale) / 2, size.height / 2) {
                native.scale(scale, scale)
                var x = 0f
                digits.forEachIndexed { index, digit ->
                    if (index == 2) x += 58f
                    native.withRotation(if (index % 2 == 0) -7f else 5f, x + advances[index] / 2, 0f) {
                        paint.color = colors[index].toInt()
                        paint.alpha = 216
                        drawText(digit.toString(), x, -(paint.ascent() + paint.descent()) / 2, paint)
                    }
                    x += advances[index]
                }
                paint.color = 0xFFF3D82D.toInt()
                paint.alpha = 220
                val colonX = advances.take(2).sum() + 14f
                native.drawCircle(colonX, -46f, 24f, paint)
                native.drawCircle(colonX, 52f, 24f, paint)
                }
            }
        }
        if (preferences.showDate) Text(readout.longDate, Modifier.align(Alignment.BottomCenter).padding(bottom = 34.dp),
            color = Color.White.copy(alpha = .45f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
