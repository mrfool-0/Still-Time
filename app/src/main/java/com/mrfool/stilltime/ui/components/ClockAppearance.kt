package com.mrfool.stilltime.ui.components

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.core.content.res.ResourcesCompat
import com.mrfool.stilltime.R
import com.mrfool.stilltime.model.ClockPreferences
import com.mrfool.stilltime.model.ClockTypography

fun ClockPreferences.accentColor(original: Color = Color(accent.seed)): Color =
    if (themeColors) original else Color(accent.seed)

fun ClockPreferences.digitColor(original: Color, lightBackground: Boolean = false): Color =
    if (!tintDigits) original else if (lightBackground) lerp(Color(accent.seed), Color.Black, .65f) else Color(accent.seed)

fun ClockTypography.typeface(context: Context, original: Typeface): Typeface = when (this) {
    ClockTypography.ORIGINAL -> original
    ClockTypography.CLASSIC -> Typeface.create("sans-serif", Typeface.NORMAL)
    ClockTypography.ROUNDED -> ResourcesCompat.getFont(context, R.font.fredoka) ?: original
    ClockTypography.EDITORIAL -> Typeface.SERIF
    ClockTypography.MONO -> Typeface.MONOSPACE
    ClockTypography.CONDENSED -> Typeface.create("sans-serif-condensed", Typeface.NORMAL)
}
