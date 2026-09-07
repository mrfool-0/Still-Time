package com.mrfool.stilltime.util

import com.mrfool.stilltime.power.BurnInOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BurnInOffsetTest {
    @Test
    fun `offset stays inside safe normalized bounds for positive and negative epochs`() {
        (-20_000L..20_000L).forEach { minute ->
            val offset = BurnInOffset.forEpochMinute(minute)
            assertTrue(offset.xFraction in -1f..1f)
            assertTrue(offset.yFraction in -1f..1f)
        }
    }

    @Test
    fun `offset is deterministic`() {
        assertEquals(
            BurnInOffset.forEpochMinute(2_981_234L),
            BurnInOffset.forEpochMinute(2_981_234L),
        )
    }

    @Test
    fun `successive minutes use varied positions`() {
        val positions = (0L until 30L).map(BurnInOffset::forEpochMinute).toSet()

        assertTrue(positions.size >= 25)
    }
}
