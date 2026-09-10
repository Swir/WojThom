package com.wojthom.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4CC9F0),
    secondary = Color(0xFF7DD3FC),
    background = Color(0xFF07111F),
    surface = Color(0xFF0E1B2B),
    surfaceVariant = Color(0xFF17283B)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF006A83),
    secondary = Color(0xFF316878),
    background = Color(0xFFF6FAFD),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE4F0F6)
)

@Composable
fun WojThomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
