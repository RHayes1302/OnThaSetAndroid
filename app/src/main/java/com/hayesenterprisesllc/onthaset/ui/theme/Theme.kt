package com.hayesenterprisesllc.onthaset.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** iOS dark-mode system colors, matching the SwiftUI app. */
object AppColors {
    val Yellow = Color(0xFFFFD60A)
    val Orange = Color(0xFFFF9F0A)
    val Green = Color(0xFF30D158)
    val Red = Color(0xFFFF453A)
    val Blue = Color(0xFF0A84FF)
    val Purple = Color(0xFFBF5AF2)
    val Pink = Color(0xFFFF375F)
    val Gray = Color(0xFF8E8E93)
    val Black = Color.Black
    val White = Color.White
    val Card = Color.White.copy(alpha = 0.05f)
    val Field = Color.White.copy(alpha = 0.1f)
}

private val scheme = darkColorScheme(
    primary = AppColors.Yellow,
    onPrimary = Color.Black,
    secondary = AppColors.Yellow,
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF1C1C1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFAEAEB2),
    error = AppColors.Red
)

@Composable
fun OnThaSetTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
