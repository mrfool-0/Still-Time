package com.mrfool.stilltime.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.mrfool.stilltime.model.ClockPreferences
import com.mrfool.stilltime.spotify.*
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

private val SpotifyGreen = Color(0xFF1ED760)

@Composable
fun SpotifyClock(
    preferences: ClockPreferences,
    readout: ClockReadout,
    state: SpotifyUiState,
    active: Boolean,
    interactive: Boolean,
    onConnect: () -> Unit,
    onPrevious: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onCustomize: () -> Unit,
    modifier: Modifier = Modifier,
    onUseDevicePlayer: (() -> Unit)? = null,
    onSeek: (Long, String) -> Unit = { _, _ -> },
    onShuffle: () -> Unit = {},
) {
    val accent = preferences.accentColor(Color(0xFFFFB7C5))
    BoxWithConstraints(modifier.background(Brush.linearGradient(listOf(Color(0xFF101114), Color(0xFF16161A))))
        .windowInsetsPadding(WindowInsets.safeDrawing)) {
        val landscape = maxWidth > maxHeight
        val clockSide: @Composable (Modifier) -> Unit = { side ->
            BoxWithConstraints(side.testTag("spotify_clock_half").padding(24.dp)) {
                val clockTextSize = minOf(maxWidth.value * .28f, maxHeight.value * .46f).sp
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NOW & HERE", color = accent, fontSize = 10.sp, letterSpacing = 4.sp)
                    Spacer(Modifier.height(14.dp))
                    Text(readout.moment.format(DateTimeFormatter.ofPattern("HH:mm")),
                        fontFamily = preferences.typography.fontFamily(), fontWeight = FontWeight.Medium,
                        color = preferences.digitColor(Color(0xFFF4F0E9)), fontSize = clockTextSize,
                        letterSpacing = (-3).sp, maxLines = 1)
                    Text(readout.longDate, color = Color.White.copy(alpha = .56f), fontSize = 13.sp)
                    if (preferences.showBattery) Text(readout.batteryLabel, Modifier.padding(top = 24.dp),
                        color = Color.White.copy(alpha = .34f), fontSize = 11.sp)
                }
                if (interactive) TextButton(onClick = onCustomize, modifier = Modifier.align(Alignment.TopStart).testTag("spotify_customize")) {
                    Text("Customize", color = Color.White.copy(alpha = .65f), fontSize = 11.sp)
                }
            }
        }
        val playerSide: @Composable (Modifier) -> Unit = { side ->
            Surface(side.testTag("spotify_player_half").padding(if (landscape) 18.dp else 14.dp),
                color = accent.copy(alpha = .035f), shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, accent.copy(alpha = .10f))) {
                SpotifyPlayerPanel(state, preferences.spotifyMarquee && active, active, interactive,
                    onConnect, onPrevious, onToggle, onNext, Modifier.fillMaxSize(), onUseDevicePlayer,
                    accent, preferences.typography.fontFamily(FontFamily.SansSerif), onSeek, onShuffle)
            }
        }
        if (landscape) {
            Row(Modifier.fillMaxSize()) {
                clockSide(Modifier.weight(1f).fillMaxHeight())
                playerSide(Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                clockSide(Modifier.weight(.42f).fillMaxWidth())
                playerSide(Modifier.weight(.58f).fillMaxWidth())
            }
        }
    }
}

@Composable
fun SpotifyPlayerPanel(
    state: SpotifyUiState,
    marquee: Boolean,
    active: Boolean,
    interactive: Boolean,
    onConnect: () -> Unit,
    onPrevious: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    onUseDevicePlayer: (() -> Unit)? = null,
    accent: Color = Color(0xFFFFB7C5),
    fontFamily: FontFamily = FontFamily.SansSerif,
    onSeek: (Long, String) -> Unit = { _, _ -> },
    onShuffle: () -> Unit = {},
) {
    val context = LocalContext.current
    val connected = state.status == SpotifyStatus.CONNECTED
    val hasTrack = connected && state.title.isNotBlank()
    var showAccessExplanation by remember { mutableStateOf(false) }
    if (showAccessExplanation) AlertDialog(
        onDismissRequest = { showAccessExplanation = false },
        title = { Text("Allow media access?") },
        text = { Text("Android uses notification access to expose active media sessions. This is a broad system permission. Stilltime uses it only to show Spotify’s song, artwork and playback controls; it does not read or store notification messages. You can revoke access in Android Settings anytime.") },
        confirmButton = { TextButton(onClick = { showAccessExplanation = false; onConnect() }) { Text("Open Android settings") } },
        dismissButton = { TextButton(onClick = { showAccessExplanation = false }) { Text("Not now") } },
    )
    BoxWithConstraints(modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
        val chromeHeight = 230.dp + (if (!hasTrack && interactive) 48.dp else 0.dp) +
            (if (interactive && !connected && state.status != SpotifyStatus.CONNECTING && onUseDevicePlayer != null) 48.dp else 0.dp) +
            (if (state.error != null) 42.dp else 0.dp)
        val artworkSize = minOf(maxWidth * .64f, (maxHeight - chromeHeight).coerceAtLeast(64.dp), 230.dp)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Spotify", color = SpotifyGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp)
                Text(if (hasTrack && !state.timeline.paused) "NOW PLAYING" else if (hasTrack) "PAUSED" else "YOUR MUSIC",
                    color = accent.copy(alpha = .7f), fontSize = 8.sp, letterSpacing = 1.8.sp)
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.size(artworkSize).clip(RoundedCornerShape(18.dp)).background(accent.copy(alpha = .08f)), contentAlignment = Alignment.Center) {
                val artwork = state.artwork
                if (artwork != null) {
                    Image(artwork.asImageBitmap(), "Album artwork", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                } else {
                    Text("♪", color = accent.copy(alpha = .6f), fontSize = 40.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            val title = when {
                hasTrack -> state.title
                state.status == SpotifyStatus.SETUP_REQUIRED -> "Spotify setup pending"
                state.status == SpotifyStatus.APP_MISSING -> "Install Spotify"
                state.status == SpotifyStatus.SDK_UNAVAILABLE -> "Spotify found · SDK unavailable"
                state.status == SpotifyStatus.ACCESS_REQUIRED -> "Allow media access"
                state.status == SpotifyStatus.CONNECTING -> "Connecting…"
                connected -> "Ready when you are"
                else -> "Your music, beside time"
            }
            key(state.title, state.artist) {
                val scrollModifier = if (marquee && hasTrack && !state.timeline.paused) {
                    Modifier.basicMarquee(iterations = 3, initialDelayMillis = 3_000, repeatDelayMillis = 5_000, velocity = 22.dp)
                } else Modifier
                Text(title, Modifier.fillMaxWidth().then(scrollModifier), color = Color(0xFFF5F0EB), fontSize = 19.sp, fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold, lineHeight = 22.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (hasTrack) state.artist else when (state.status) {
                    SpotifyStatus.SETUP_REQUIRED -> "A registered Spotify Client ID is needed."
                    SpotifyStatus.APP_MISSING -> "Play music in the Spotify Android app."
                    SpotifyStatus.SDK_UNAVAILABLE -> "Choose Device player in Customize, or use the official Spotify app."
                    SpotifyStatus.ACCESS_REQUIRED -> "Optional Android media-session connection."
                    else -> "Play something in Spotify to begin."
                }, Modifier.fillMaxWidth().padding(top = 3.dp).then(scrollModifier),
                    color = Color.White.copy(alpha = .48f), fontSize = 11.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            key(state.trackId, state.canSeek, interactive, active) {
                SpotifyProgress(state.timeline, active && hasTrack, Modifier.fillMaxWidth(),
                    enabled = active && hasTrack && interactive && state.canSeek, accent = accent,
                    onSeek = { onSeek(it, state.trackId) })
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                ShuffleButton(state.shuffled, hasTrack && interactive && state.canShuffle, accent, onShuffle)
                TransportButton("Previous track", hasTrack && interactive && state.canSkipPrevious, false, -1, onPrevious, accent)
                TransportButton(if (state.timeline.paused) "Play" else "Pause", hasTrack && interactive, true,
                    if (state.timeline.paused) 0 else 2, onToggle, accent)
                TransportButton("Next track", hasTrack && interactive && state.canSkipNext, false, 1, onNext, accent)
            }
            if (!hasTrack && interactive) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!connected && state.status !in setOf(SpotifyStatus.SETUP_REQUIRED, SpotifyStatus.APP_MISSING, SpotifyStatus.SDK_UNAVAILABLE)) {
                        TextButton(onClick = {
                            if (state.status == SpotifyStatus.ACCESS_REQUIRED) showAccessExplanation = true else onConnect()
                        }, enabled = state.status != SpotifyStatus.CONNECTING) {
                            Text(if (state.status == SpotifyStatus.ACCESS_REQUIRED) "Grant access" else "Connect", color = accent)
                        }
                    }
                    TextButton(onClick = {
                        val intent = SpotifyPackages.installed(context).firstNotNullOfOrNull { context.packageManager.getLaunchIntentForPackage(it) }
                            ?: Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=com.spotify.music".toUri())
                        try { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: ActivityNotFoundException) { }
                    }) { Text("Open Spotify", color = Color.White.copy(alpha = .7f)) }
                }
            }
            if (interactive && !connected && state.status != SpotifyStatus.CONNECTING && onUseDevicePlayer != null) {
                TextButton(onClick = onUseDevicePlayer) { Text("Use device player", color = accent) }
            }
            state.error?.let { Text(it, color = Color(0xFFFFB6AD), fontSize = 10.sp, lineHeight = 14.sp, maxLines = 3) }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun SpotifyProgress(timeline: PlaybackTimeline, active: Boolean, modifier: Modifier,
    enabled: Boolean, accent: Color, onSeek: (Long) -> Unit) {
    var dragged by remember { mutableStateOf<Float?>(null) }
    var pending by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(pending) { if (pending != null) { delay(2000); pending = null } }
    LaunchedEffect(timeline) { pending = null }
    val position by produceState(timeline.positionMs, timeline, active) {
        value = timeline.positionAt(SystemClock.elapsedRealtime())
        while (active && !timeline.paused && timeline.speed > 0 && value < timeline.durationMs) {
            delay(1_000)
            value = timeline.positionAt(SystemClock.elapsedRealtime())
        }
    }
    Column(modifier) {
        val shown = (dragged?.toLong() ?: pending ?: position).coerceIn(0, timeline.durationMs.coerceAtLeast(0))
        Slider(value = shown.toFloat(), onValueChange = { dragged = it },
            onValueChangeFinished = {
                dragged?.toLong()?.let { if (enabled) { pending = it; onSeek(it) } }
                dragged = null
            }, enabled = enabled && timeline.durationMs > 0,
            valueRange = 0f..timeline.durationMs.coerceAtLeast(1).toFloat(),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("spotify_seek")
                .semantics { contentDescription = "Track position" },
            colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent,
                inactiveTrackColor = Color.White.copy(alpha = .12f), disabledActiveTrackColor = accent.copy(alpha = .35f)),
            thumb = { Box(Modifier.size(10.dp).background(if (enabled) accent else accent.copy(alpha = .35f), CircleShape)) },
            track = { SliderDefaults.Track(it, Modifier.height(3.dp), thumbTrackGapSize = 0.dp,
                colors = SliderDefaults.colors(activeTrackColor = accent, inactiveTrackColor = Color.White.copy(alpha = .12f)),
                enabled = enabled, drawStopIndicator = null) })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(playbackTimeLabel(shown), color = Color.White.copy(alpha = .6f), fontSize = 10.sp, lineHeight = 14.sp)
            Text(playbackTimeLabel(timeline.durationMs), color = Color.White.copy(alpha = .45f), fontSize = 10.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun TransportButton(label: String, enabled: Boolean, primary: Boolean, kind: Int, onClick: () -> Unit, accent: Color) {
    IconButton(onClick, enabled = enabled, modifier = Modifier.size(48.dp).testTag("spotify_${label.lowercase().replace(' ', '_')}")
        .semantics { contentDescription = label }
        .background(if (primary) accent.copy(alpha = if (enabled) 1f else .22f) else Color.Transparent, CircleShape)) {
        Canvas(Modifier.size(22.dp)) {
            val color = (if (primary) Color.Black else Color.White).copy(alpha = if (enabled) 1f else .24f)
            if (kind == 2) {
                drawRect(color, Offset(size.width * .2f, size.height * .1f), Size(size.width * .2f, size.height * .8f))
                drawRect(color, Offset(size.width * .6f, size.height * .1f), Size(size.width * .2f, size.height * .8f))
            } else {
                val previous = kind == -1
                val path = Path().apply {
                    moveTo(size.width * if (previous) .8f else .2f, size.height * .1f)
                    lineTo(size.width * if (previous) .2f else .8f, size.height * .5f)
                    lineTo(size.width * if (previous) .8f else .2f, size.height * .9f)
                    close()
                }
                drawPath(path, color)
                if (kind != 0) drawRect(color, Offset(size.width * if (previous) .1f else .8f, size.height * .1f), Size(size.width * .1f, size.height * .8f))
            }
        }
    }
}

@Composable
private fun ShuffleButton(checked: Boolean, enabled: Boolean, accent: Color, onClick: () -> Unit) {
    IconToggleButton(checked = checked, onCheckedChange = { onClick() }, enabled = enabled,
        modifier = Modifier.size(48.dp).testTag("spotify_shuffle")
            .semantics { contentDescription = if (enabled) "Shuffle" else "Shuffle unavailable in this session" }) {
        Canvas(Modifier.size(22.dp)) {
            val color = (if (checked) accent else Color.White).copy(alpha = if (enabled) 1f else .24f)
            fun path(ascending: Boolean) = Path().apply {
                val a = if (ascending) .78f else .22f
                val b = 1f - a
                moveTo(0f, size.height * a)
                cubicTo(size.width * .5f, size.height * a, size.width * .5f, size.height * b, size.width, size.height * b)
                moveTo(size.width * .78f, size.height * (b - .16f))
                lineTo(size.width, size.height * b)
                lineTo(size.width * .78f, size.height * (b + .16f))
            }
            drawPath(path(false), color, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round))
            drawPath(path(true), color, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round))
            if (checked) drawCircle(color, 1.5.dp.toPx(), Offset(size.width / 2, size.height + 5.dp.toPx()))
        }
    }
}
