package com.mrfool.stilltime.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrfool.stilltime.model.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.sin

@Composable
internal fun KittenClock(preferences: ClockPreferences, readout: ClockReadout, accent: Color,
    modifier: Modifier, active: Boolean) {
    val blink = remember { Animatable(0f) }
    val gesture = remember { Animatable(0f) }
    val moving = active && preferences.catMotion
    LaunchedEffect(moving) {
        blink.snapTo(0f); gesture.snapTo(0f)
        if (!moving) return@LaunchedEffect
        // Greet on entry rather than looking frozen for the first several seconds.
        delay(700)
        if (currentCoroutineContext()[MotionDurationScale]?.scaleFactor != 0f) {
            blink.animateTo(1f, tween(140))
            delay(70)
            blink.animateTo(0f, tween(220))
            gesture.animateTo(1f, tween(1400))
        }
        var count = 0
        while (isActive) {
            delay(if (count % 3 == 0) 6500 else 8500)
            if (currentCoroutineContext()[MotionDurationScale]?.scaleFactor == 0f) continue
            blink.animateTo(1f, tween(140))
            delay(70)
            blink.animateTo(0f, tween(220))
            if (++count % 3 == 0) {
                gesture.snapTo(0f)
                gesture.animateTo(1f, tween(1400))
            }
        }
    }
    BoxWithConstraints(modifier.fillMaxSize().background(Color(0xFF101216))) {
        val portrait = maxHeight > maxWidth
        val clockSize = minOf(maxWidth.value * (if (portrait) .19f else .095f), maxHeight.value * .24f).sp
        val clock: @Composable (Modifier) -> Unit = { area ->
            Column(area, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("LITTLE MOMENTS", color = accent, fontSize = 10.sp, letterSpacing = 3.sp)
                Spacer(Modifier.height(16.dp))
                Text(readout.time.digital, color = preferences.digitColor(Color(0xFFF6EEE4)),
                    fontFamily = preferences.typography.fontFamily(FontFamily.SansSerif), fontWeight = FontWeight.Light,
                    fontSize = clockSize,
                    letterSpacing = (-3).sp, maxLines = 1)
                if (preferences.showSeconds) Text(readout.time.second, color = accent, fontSize = 14.sp)
                if (preferences.showDate) Text(readout.longDate, color = Color(0xFFAFABA8), fontSize = 12.sp)
            }
        }
        val cat: @Composable (Modifier) -> Unit = { area ->
            Canvas(area.padding(24.dp)) {
                val scale = minOf(size.width / 360f, size.height / 420f)
                translate((size.width - 360 * scale) / 2, (size.height - 420 * scale) / 2) {
                    scale(scale, scale, Offset.Zero) { drawKitten(accent, blink.value, sin(gesture.value * PI).toFloat()) }
                }
            }
        }
        if (portrait) Column(Modifier.fillMaxSize().padding(top = 28.dp, bottom = 42.dp)) {
            clock(Modifier.fillMaxWidth().weight(.38f)); cat(Modifier.fillMaxWidth().weight(.62f))
        } else Row(Modifier.fillMaxSize().padding(20.dp, 16.dp, 20.dp, 30.dp)) {
            clock(Modifier.fillMaxHeight().weight(1f)); cat(Modifier.fillMaxHeight().weight(1f))
        }
        if (preferences.showBattery) Text(readout.batteryLabel,
            Modifier.align(Alignment.BottomCenter).padding(18.dp), color = Color(0xFF878789), fontSize = 10.sp)
    }
}

/** Resolution-independent artwork inspired by the supplied kitten reference. */
internal fun DrawScope.drawKitten(accent: Color, blink: Float, gesture: Float) {
    val fur = Color(0xFFFFFBF0)
    val ink = Color(0xFF482E32)
    val pink = lerp(accent, Color(0xFFFFAEB9), .45f)
    fun shape(path: Path, fill: Color = fur) {
        drawPath(path, fill); drawPath(path, ink, style = Stroke(3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    drawOval(accent.copy(alpha = .09f), Offset(52f, 383f), Size(268f, 24f))
    rotate(gesture * -14f, Offset(281f, 341f)) {
        shape(Path().apply { moveTo(261f, 367f); cubicTo(348f, 350f, 342f, 285f, 310f, 267f)
            cubicTo(290f, 257f, 288f, 274f, 302f, 288f); cubicTo(326f, 315f, 311f, 342f, 257f, 344f); close() })
    }
    shape(Path().apply { moveTo(110f, 238f); cubicTo(72f, 304f, 67f, 384f, 122f, 389f)
        cubicTo(155f, 392f, 204f, 392f, 239f, 387f); cubicTo(285f, 380f, 280f, 305f, 249f, 240f); close() })
    drawOval(ink.copy(alpha = .08f), Offset(104f, 238f), Size(151f, 36f))
    drawPath(Path().apply { moveTo(137f, 322f); quadraticTo(143f, 355f, 151f, 389f)
        moveTo(214f, 322f); quadraticTo(210f, 355f, 199f, 389f)
        moveTo(176f, 344f); lineTo(176f, 389f) }, ink, style = Stroke(2.5f, cap = StrokeCap.Round))
    for (x in listOf(116f, 126f, 228f, 238f)) drawLine(ink, Offset(x, 379f), Offset(x + 1, 388f), 2f, StrokeCap.Round)
    rotate(gesture * 5f, Offset(178f, 244f)) {
        rotate(gesture * -11f, Offset(97f, 131f)) {
            shape(Path().apply { moveTo(68f, 150f); quadraticTo(45f, 81f, 64f, 28f)
                quadraticTo(107f, 36f, 140f, 80f); close() })
            shape(Path().apply { moveTo(74f, 128f); quadraticTo(57f, 80f, 69f, 42f)
                quadraticTo(102f, 51f, 122f, 81f); close() }, pink)
        }
        shape(Path().apply { moveTo(226f, 83f); quadraticTo(265f, 45f, 302f, 38f)
            quadraticTo(317f, 93f, 291f, 153f); close() })
        shape(Path().apply { moveTo(248f, 88f); quadraticTo(270f, 63f, 296f, 51f)
            quadraticTo(305f, 91f, 287f, 128f); close() }, pink)
        shape(Path().apply { moveTo(54f, 163f); cubicTo(63f, 88f, 119f, 65f, 180f, 66f)
            cubicTo(253f, 63f, 300f, 111f, 307f, 174f); cubicTo(333f, 253f, 268f, 277f, 180f, 272f)
            cubicTo(78f, 277f, 31f, 250f, 54f, 163f); close() })
        for (x in listOf(89f, 268f)) drawCircle(Brush.radialGradient(listOf(pink.copy(alpha = .50f), Color.Transparent),
            Offset(x, 213f), 31f), 31f, Offset(x, 213f))
        for (x in listOf(117f, 240f)) {
            scale(1f, (1f - blink * .94f), Offset(x, 166f)) {
                drawOval(ink, Offset(x - 31, 122f), Size(62f, 83f))
                drawOval(Color(0xFF6D493E), Offset(x - 24, 140f), Size(49f, 61f))
                drawOval(Color(0xFFE0C786), Offset(x - 18, 188f), Size(37f, 12f))
                drawCircle(Color.White, 11f, Offset(x - 11, 142f))
                drawCircle(Color.White.copy(alpha = .9f), 4f, Offset(x + 15, 161f))
                drawCircle(Color.White.copy(alpha = .8f), 2f, Offset(x - 10, 177f))
            }
        }
        shape(Path().apply { moveTo(155f, 211f); quadraticTo(178f, 222f, 199f, 210f)
            cubicTo(197f, 247f, 162f, 249f, 155f, 211f); close() }, pink)
        shape(Path().apply { moveTo(173f, 195f); quadraticTo(180f, 190f, 187f, 195f)
            quadraticTo(181f, 204f, 173f, 195f); close() }, pink)
        drawPath(Path().apply { moveTo(180f, 201f); lineTo(180f, 210f)
            quadraticTo(165f, 222f, 155f, 212f); moveTo(180f, 210f); quadraticTo(192f, 222f, 201f, 211f) },
            ink, style = Stroke(2.5f, cap = StrokeCap.Round))
    }
}
