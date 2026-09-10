package com.mrfool.stilltime.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrfool.stilltime.R
import com.mrfool.stilltime.model.ClockPreferences
import com.mrfool.stilltime.model.ClockTypography
import com.mrfool.stilltime.model.WallpaperLayout

val RoundedClockFont = FontFamily(Font(R.font.fredoka))

fun ClockTypography.fontFamily(original: FontFamily = RoundedClockFont): FontFamily = when (this) {
    ClockTypography.ORIGINAL -> original
    ClockTypography.CLASSIC -> FontFamily.SansSerif
    ClockTypography.ROUNDED -> RoundedClockFont
    ClockTypography.EDITORIAL -> FontFamily.Serif
    ClockTypography.MONO -> FontFamily.Monospace
    ClockTypography.CONDENSED -> FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL))
}

@Composable
fun WallpaperClock(preferences: ClockPreferences, readout: ClockReadout, modifier: Modifier) {
    BoxWithConstraints(modifier.background(Color.Black)) {
        val landscape = maxWidth > maxHeight
        val gallery = preferences.wallpaperLayout == WallpaperLayout.GALLERY
        val poster = preferences.wallpaperLayout == WallpaperLayout.POSTER
        val imageModifier = if (gallery) {
            if (landscape) Modifier.align(Alignment.CenterEnd).fillMaxHeight().fillMaxWidth(.52f)
            else Modifier.align(Alignment.TopCenter).fillMaxWidth().fillMaxHeight(.55f)
        } else Modifier.fillMaxSize()
        Box(imageModifier) {
            Image(painterResource(preferences.wallpaper.resource), contentDescription = null,
                modifier = Modifier.fillMaxSize(), contentScale = if (gallery) ContentScale.Fit else ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = preferences.wallpaperDim.opacity)))
            if (!gallery) Box(Modifier.fillMaxSize().background(
                if (poster || !landscape) Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .85f)))
                else Brush.horizontalGradient(listOf(Color.Black.copy(alpha = .82f), Color.Transparent)),
            ))
        }
        val position = when {
            poster -> Alignment.BottomStart
            !landscape -> Alignment.BottomCenter
            else -> Alignment.CenterStart
        }
        val textWidth = if (landscape && !poster) maxWidth * .47f else maxWidth
        val clockTextSize = minOf((textWidth.value - 56f) * .28f, maxHeight.value * .34f).sp
        Column(Modifier.align(position).width(textWidth)
            .then(if (gallery && !landscape) Modifier.fillMaxHeight(.45f) else Modifier)
            .padding(horizontal = 28.dp, vertical = 36.dp), verticalArrangement = Arrangement.Center,
            horizontalAlignment = if (landscape || poster) Alignment.Start else Alignment.CenterHorizontally) {
            Text(preferences.wallpaper.title.uppercase(), color = preferences.accentColor(Color(preferences.wallpaper.accent)),
                fontSize = 10.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(readout.time.digital, color = preferences.digitColor(Color.White), fontFamily = preferences.typography.fontFamily(),
                fontSize = clockTextSize,
                fontWeight = FontWeight.Medium, letterSpacing = (-3).sp, maxLines = 1)
            if (preferences.showDate) Text(readout.longDate, color = Color.White.copy(alpha = .76f), fontSize = 14.sp)
            if (preferences.showBattery) Text(readout.batteryLabel, Modifier.padding(top = 16.dp),
                color = Color.White.copy(alpha = .50f), fontSize = 11.sp)
        }
    }
}
