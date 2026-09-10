package com.mrfool.stilltime.spotify

data class PlaybackTimeline(
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val sampledAtMs: Long = 0,
    val paused: Boolean = true,
    val speed: Float = 1f,
) {
    fun positionAt(elapsedRealtimeMs: Long): Long {
        val delta = if (paused) 0 else ((elapsedRealtimeMs - sampledAtMs).coerceAtLeast(0) * speed.coerceAtLeast(0f)).toLong()
        return (positionMs + delta).coerceIn(0, durationMs.coerceAtLeast(0))
    }
}

fun playbackTimeLabel(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0) / 1000
    return "%d:%02d".format(java.util.Locale.ROOT, seconds / 60, seconds % 60)
}
