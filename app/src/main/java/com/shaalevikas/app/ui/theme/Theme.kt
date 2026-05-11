package com.shaalevikas.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GreenPrimary    = Color(0xFF2E7D32)
val GreenLight      = Color(0xFF60AD5E)
val AmberAccent     = Color(0xFFFFA000)
val BackgroundLight = Color(0xFFF1F8E9)

private val LightColors = lightColorScheme(
    primary          = GreenPrimary,
    onPrimary        = Color.White,
    primaryContainer = GreenLight,
    secondary        = AmberAccent,
    background       = BackgroundLight,
    surface          = Color.White,
    onBackground     = Color(0xFF1B1B1B),
    onSurface        = Color(0xFF1B1B1B)
)

@Composable
fun ShaaleVikasTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, typography = Typography(), content = content)
}
