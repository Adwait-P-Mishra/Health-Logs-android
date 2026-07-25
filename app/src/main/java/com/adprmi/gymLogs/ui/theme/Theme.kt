package com.adprmi.gymLogs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkTextPrimary,
    onPrimary = DarkBackgroundCard,
    primaryContainer = DarkBackgroundCard,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkTextPrimary,
    onSecondary = DarkBackgroundCard,
    tertiary = DarkTextPrimary,
    onTertiary = DarkBackgroundCard,
    background = DarkBackgroundPrimary,
    onBackground = DarkTextPrimary,
    surface = DarkBackgroundCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkBackgroundCard,
    onSurfaceVariant = DarkTextPrimary,
    outline = DarkTextPrimary.copy(alpha = 0.5f),
    outlineVariant = DarkSeparator,
    error = DarkTextPrimary,
    onError = DarkBackgroundCard
)

private val LightColorScheme = lightColorScheme(
    primary = LightTextPrimary,
    onPrimary = LightBackgroundCard,
    primaryContainer = LightBackgroundCard,
    onPrimaryContainer = LightTextPrimary,
    secondary = LightTextPrimary,
    onSecondary = LightBackgroundCard,
    tertiary = LightTextPrimary,
    onTertiary = LightBackgroundCard,
    background = LightBackgroundPrimary,
    onBackground = LightTextPrimary,
    surface = LightBackgroundCard,
    onSurface = LightTextPrimary,
    surfaceVariant = LightBackgroundCard,
    onSurfaceVariant = LightTextPrimary,
    outline = LightTextPrimary.copy(alpha = 0.5f),
    outlineVariant = LightSeparator,
    error = LightTextPrimary,
    onError = LightBackgroundCard
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
