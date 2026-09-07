package com.mrfool.stilltime.util

import com.mrfool.stilltime.model.TimeFormatPreference
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoField
import java.util.Locale

data class TimeText(
    val hour: String,
    val paddedHour: String,
    val minute: String,
    val second: String,
    val period: String?,
    val spoken: String,
) {
    val digital: String
        get() = "$hour:$minute"
}

object TimeTextFormatter {
    fun uses24HourTime(
        preference: TimeFormatPreference,
        systemUses24HourTime: Boolean,
    ): Boolean = when (preference) {
        TimeFormatPreference.SYSTEM -> systemUses24HourTime
        TimeFormatPreference.TWELVE_HOUR -> false
        TimeFormatPreference.TWENTY_FOUR_HOUR -> true
    }

    fun time(
        moment: ZonedDateTime,
        use24HourTime: Boolean,
        showSeconds: Boolean,
        locale: Locale,
    ): TimeText {
        val localizedDigits = DateTimeFormatter.ofPattern("mm:ss", locale).format(moment)
        val minute = localizedDigits.substringBefore(':')
        val second = localizedDigits.substringAfter(':')
        val rawHour = if (use24HourTime) {
            DateTimeFormatter.ofPattern("HH", locale).format(moment)
        } else {
            DateTimeFormatter.ofPattern("h", locale).format(moment)
        }
        val paddedHour = if (use24HourTime) {
            rawHour
        } else {
            DateTimeFormatter.ofPattern("hh", locale).format(moment)
        }
        val period = if (use24HourTime) {
            null
        } else {
            DateTimeFormatter.ofPattern("a", locale).format(moment)
        }
        val spokenPattern = when {
            use24HourTime && showSeconds -> "HH:mm:ss"
            use24HourTime -> "HH:mm"
            showSeconds -> "h:mm:ss a"
            else -> "h:mm a"
        }

        return TimeText(
            hour = rawHour,
            paddedHour = paddedHour,
            minute = minute,
            second = second,
            period = period,
            spoken = DateTimeFormatter.ofPattern(spokenPattern, locale).format(moment),
        )
    }

    fun longDate(moment: ZonedDateTime, locale: Locale): String =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale).format(moment)

    fun compactDate(moment: ZonedDateTime, locale: Locale): String =
        DateTimeFormatter.ofPattern("EEE · MMM d", locale).format(moment)

    fun dayOfYearFraction(moment: ZonedDateTime): Float {
        val day = moment.get(ChronoField.DAY_OF_YEAR)
        return day.toFloat() / moment.toLocalDate().lengthOfYear().toFloat()
    }
}
