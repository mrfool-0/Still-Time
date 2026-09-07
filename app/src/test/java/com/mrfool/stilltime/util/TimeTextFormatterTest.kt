package com.mrfool.stilltime.util

import com.mrfool.stilltime.model.TimeFormatPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class TimeTextFormatterTest {
    private val midnight = ZonedDateTime.of(
        2026,
        9,
        7,
        0,
        5,
        9,
        0,
        ZoneId.of("Asia/Kolkata"),
    )

    @Test
    fun `24 hour time keeps a leading zero and omits period`() {
        val result = TimeTextFormatter.time(
            moment = midnight,
            use24HourTime = true,
            showSeconds = false,
            locale = Locale.US,
        )

        assertEquals("00", result.hour)
        assertEquals("05", result.minute)
        assertEquals("00:05", result.digital)
        assertNull(result.period)
    }

    @Test
    fun `12 hour time formats midnight without a leading zero`() {
        val result = TimeTextFormatter.time(
            moment = midnight,
            use24HourTime = false,
            showSeconds = true,
            locale = Locale.US,
        )

        assertEquals("12", result.hour)
        assertEquals("12", result.paddedHour)
        assertEquals("09", result.second)
        assertEquals("AM", result.period)
        assertEquals("12:05:09 AM", result.spoken)
    }

    @Test
    fun `format preference overrides or follows system`() {
        assertTrue(TimeTextFormatter.uses24HourTime(TimeFormatPreference.SYSTEM, true))
        assertFalse(TimeTextFormatter.uses24HourTime(TimeFormatPreference.SYSTEM, false))
        assertTrue(
            TimeTextFormatter.uses24HourTime(
                TimeFormatPreference.TWENTY_FOUR_HOUR,
                false,
            ),
        )
        assertFalse(
            TimeTextFormatter.uses24HourTime(
                TimeFormatPreference.TWELVE_HOUR,
                true,
            ),
        )
    }

    @Test
    fun `localized date is not assembled from English fragments`() {
        val french = TimeTextFormatter.longDate(midnight, Locale.FRENCH)

        assertTrue(french.contains("septembre"))
        assertTrue(french.contains("2026"))
    }
}
