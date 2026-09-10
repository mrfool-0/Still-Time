package com.mrfool.stilltime.util

import java.time.DayOfWeek
import java.time.YearMonth

object CalendarMonth {
    fun cells(month: YearMonth, firstDay: DayOfWeek): List<Int?> {
        val leading = (month.atDay(1).dayOfWeek.value - firstDay.value + 7) % 7
        return List(42) { index -> (index - leading + 1).takeIf { it in 1..month.lengthOfMonth() } }
    }
}
