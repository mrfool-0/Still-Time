package com.mrfool.stilltime.util

import java.time.DayOfWeek
import java.time.YearMonth
import org.junit.Assert.*
import org.junit.Test

class CalendarMonthTest {
    @Test fun `leap February has 29 days and correct Monday alignment`() {
        val cells = CalendarMonth.cells(YearMonth.of(2024, 2), DayOfWeek.MONDAY)
        assertEquals(listOf(null, null, null, 1, 2, 3, 4), cells.take(7))
        assertEquals((1..29).toList(), cells.filterNotNull())
        assertEquals(42, cells.size)
    }
    @Test fun `Sunday-first month starts in first cell`() {
        assertEquals(1, CalendarMonth.cells(YearMonth.of(2026, 2), DayOfWeek.SUNDAY).first())
    }
    @Test fun `six-row month keeps its final day`() {
        val cells = CalendarMonth.cells(YearMonth.of(2026, 8), DayOfWeek.SUNDAY)
        assertEquals(31, cells[36])
        assertEquals((1..31).toList(), cells.filterNotNull())
    }
}
