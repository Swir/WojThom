package com.wojthom.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = Color(0xFF53C7FF),
    onPrimary = Color(0xFF001E2B),
    primaryContainer = Color(0xFF083B55),
    onPrimaryContainer = Color(0xFFD2F2FF),
    secondary = Color(0xFF9BD6F2),
    secondaryContainer = Color(0xFF15354A),
    tertiary = Color(0xFFB5A7FF),
    background = Color(0xFF050C14),
    surface = Color(0xFF0B1622),
    surfaceVariant = Color(0xFF132536),
    surfaceContainer = Color(0xFF0F1C29),
    surfaceContainerHigh = Color(0xFF17293A),
    outline = Color(0xFF36536A),
    error = Color(0xFFFF7184)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF00698A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC3ECFF),
    onPrimaryContainer = Color(0xFF002533),
    secondary = Color(0xFF3D6578),
    secondaryContainer = Color(0xFFD8EAF4),
    tertiary = Color(0xFF62558F),
    background = Color(0xFFF2F7FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE1EDF3),
    surfaceContainer = Color(0xFFF8FCFE),
    surfaceContainerHigh = Color(0xFFEAF3F7),
    outline = Color(0xFF78909D),
    error = Color(0xFFBA1A1A)
)

private val WojThomShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun WojThomTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = WojThomShapes,
        content = content
    )
}
