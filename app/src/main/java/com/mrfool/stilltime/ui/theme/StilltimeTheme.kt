package com.mrfool.stilltime.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StilltimeColors = darkColorScheme(
    primary = Color(0xFFFFB7C5),
    onPrimary = Color(0xFF3A1822),
    secondary = Color(0xFFC8B6FF),
    background = Color(0xFF09090B),
    onBackground = Color(0xFFF8F5F2),
    surface = Color(0xFF1B1A20),
    onSurface = Color(0xFFF8F5F2),
    surfaceVariant = Color(0xFF2A282F),
    onSurfaceVariant = Color(0xFFD5CFD8),
)

@Composable
fun StilltimeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StilltimeColors,
        content = content,
    )
}
