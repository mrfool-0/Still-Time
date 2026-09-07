package com.mrfool.stilltime.util

import com.mrfool.stilltime.model.ClockStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class ClockStyleTest {
    @Test
    fun `next wraps after the last style`() {
        assertEquals(ClockStyle.PEBBLE, ClockStyle.NOIR.next())
    }

    @Test
    fun `previous wraps before the first style`() {
        assertEquals(ClockStyle.NOIR, ClockStyle.PEBBLE.previous())
    }
}
