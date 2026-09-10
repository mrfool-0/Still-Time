package com.mrfool.stilltime.power

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.ZonedDateTime

@Composable
fun rememberClockMoment(
    showSeconds: Boolean,
    isActive: Boolean,
    refreshSeconds: Long = 60L,
): State<ZonedDateTime> = produceState(
    initialValue = ZonedDateTime.now(),
    key1 = showSeconds,
    key2 = isActive,
    key3 = refreshSeconds,
) {
    if (!isActive) return@produceState

    val intervalMillis = if (showSeconds) SECOND_MILLIS else refreshSeconds.coerceIn(1L, 60L) * SECOND_MILLIS
    while (this.isActive) {
        val nowMillis = System.currentTimeMillis()
        value = ZonedDateTime.now()
        val untilBoundary = intervalMillis - (nowMillis % intervalMillis)
        delay(untilBoundary.coerceAtLeast(MIN_DELAY_MILLIS) + CLOCK_SETTLE_MILLIS)
    }
}

private const val SECOND_MILLIS = 1_000L
private const val MIN_DELAY_MILLIS = 16L
private const val CLOCK_SETTLE_MILLIS = 12L
