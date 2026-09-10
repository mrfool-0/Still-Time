package com.mrfool.stilltime.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/** One finite light drift per thought. No infinite transition, timer, blur or image allocation. */
@Composable
internal fun MuseAurora(thought: String, accent: Color, animate: Boolean, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(1f) }
    val seed = remember(thought) { (thought.hashCode().toLong() and 0xffffL) / 65535f }
    LaunchedEffect(thought, animate) {
        if (animate) {
            progress.snapTo(0f)
            // Compose's MotionDurationScale follows Android's animator-duration setting.
            progress.animateTo(1f, tween(2400, easing = FastOutSlowInEasing))
        } else progress.snapTo(1f)
    }
    // State is read only in drawing: text/layout do not recompose every animation frame.
    Canvas(modifier.fillMaxSize()) {
        val p = progress.value
        val breath = sin(p * PI).toFloat()
        val drift = (p - 1f) * .07f
        drawRect(Brush.linearGradient(listOf(Color(0xFF080B13), Color(0xFF151321), Color(0xFF090D14)),
            Offset.Zero, Offset(size.width, size.height)))
        val upper = Offset(size.width * (.76f + seed * .12f + drift), size.height * (.1f + drift))
        val lower = Offset(size.width * (.08f + seed * .14f - drift), size.height * (.94f - drift))
        drawRect(Brush.radialGradient(listOf(accent.copy(alpha = .19f + breath * .055f), Color.Transparent),
            center = upper, radius = size.maxDimension * .64f))
        drawRect(Brush.radialGradient(listOf(Color(0xFF759EDB).copy(alpha = .12f + breath * .035f), Color.Transparent),
            center = lower, radius = size.maxDimension * .52f))
        // Fine contour lines stay near the edges, leaving the quote's reading area calm.
        repeat(3) { index ->
            val y = size.height * (.08f + index * .025f + drift * .3f)
            val ribbon = Path().apply {
                moveTo(size.width * .45f, -size.height * .05f)
                cubicTo(size.width * .64f, y, size.width * .86f, y + size.height * .18f,
                    size.width * 1.08f, y + size.height * .06f)
            }
            drawPath(ribbon, accent.copy(alpha = .055f + breath * .025f), style = Stroke(width = 1.dp.toPx()))
        }
    }
}
