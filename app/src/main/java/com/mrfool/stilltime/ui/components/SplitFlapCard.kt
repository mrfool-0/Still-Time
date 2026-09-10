package com.mrfool.stilltime.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/** Two hinged leaves; only the changed card animates, with no idle frame loop. */
@Composable
fun SplitFlapCard(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    fontFamily: FontFamily = FontFamily.Monospace,
    digitColor: Color = Color(0xFFF5EEE7),
) {
    var settled by remember { mutableStateOf(value) }
    var outgoing by remember { mutableStateOf(value) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(value, animate) {
        if (!animate) {
            progress.snapTo(1f)
            settled = value
            outgoing = value
        } else if (value != settled) {
            outgoing = settled
            progress.snapTo(0f)
            // A clock jump replaces the interrupted target instead of replaying missed minutes.
            settled = value
            progress.animateTo(1f, tween(640, easing = FastOutSlowInEasing))
            outgoing = value
        }
    }

    BoxWithConstraints(modifier.clip(RoundedCornerShape(18.dp))
        .background(Color(0xFF141414))
        .testTag("flap_$label")
        .semantics { contentDescription = "$label $value" }) {
        val density = LocalDensity.current
        val textSize = with(density) { min(maxWidth.toPx() * .65f, maxHeight.toPx() * .64f).toSp() }
        @Composable fun Leaf(text: String, top: Boolean, moving: Boolean = false) {
            Box(Modifier.fillMaxSize().graphicsLayer {
                transformOrigin = TransformOrigin(.5f, .5f)
                cameraDistance = 24 * this.density
                if (moving) {
                    val p = progress.value
                    rotationX = if (top) -180f * p.coerceAtMost(.5f) else 180f * (1f - p).coerceAtMost(.5f)
                    alpha = if ((top && p >= .5f) || (!top && p < .5f) || p >= 1f) 0f else 1f
                }
            }.drawWithContent {
                clipRect(top = if (top) 0f else size.height / 2,
                    bottom = if (top) size.height / 2 else size.height) {
                    this@drawWithContent.drawContent()
                    if (moving) {
                        val shade = if (top) progress.value * .5f else (1f - progress.value) * .5f
                        drawRect(Color.Black.copy(alpha = shade.coerceIn(0f, .25f)))
                    }
                }
            }.background(Brush.verticalGradient(listOf(Color(0xFF353332), Color(0xFF242322)))),
                contentAlignment = Alignment.Center) {
                Text(text, color = digitColor, fontSize = textSize,
                    fontFamily = fontFamily, fontWeight = FontWeight.Medium,
                    letterSpacing = (-3).sp, maxLines = 1)
            }
        }
        Leaf(value, top = true)
        Leaf(outgoing, top = false)
        Leaf(outgoing, top = true, moving = true)
        Leaf(value, top = false, moving = true)
        Box(Modifier.fillMaxWidth().height(2.dp).align(Alignment.Center).background(Color(0xFF090909)))
        Row(Modifier.fillMaxWidth().align(Alignment.Center), horizontalArrangement = Arrangement.SpaceBetween) {
            repeat(2) { Box(Modifier.size(5.dp, 16.dp).background(Color(0xFF0B0B0B), RoundedCornerShape(2.dp))) }
        }
        Text(label, Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
            color = accent.copy(alpha = .7f), fontSize = 8.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
    }
}
