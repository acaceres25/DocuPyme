package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DocuNavy,
    onPrimary = Color.White,
    primaryContainer = DocuNavyLight,
    onPrimaryContainer = Color.White,
    secondary = DocuGold,
    onSecondary = DocuNavy,
    secondaryContainer = DocuGoldLight,
    onSecondaryContainer = DocuNavyDark,
    tertiary = DocuNavyLight,
    onTertiary = Color.White,
    background = DocuBackground,
    onBackground = DocuTextPrimary,
    surface = DocuSurface,
    onSurface = DocuTextPrimary,
    surfaceVariant = DocuSurfaceVariant,
    onSurfaceVariant = DocuTextSecondary,
    error = DocuError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DocuGold,
    onPrimary = DocuNavyDark,
    primaryContainer = DocuNavyLight,
    onPrimaryContainer = Color.White,
    secondary = DocuGold,
    onSecondary = DocuNavyDark,
    background = DocuNavyDark,
    onBackground = Color.White,
    surface = DocuNavy,
    onSurface = Color.White,
    surfaceVariant = DocuNavyLight,
    onSurfaceVariant = Color(0xFFD1D5DB),
    error = DocuError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep DocuPyme brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
