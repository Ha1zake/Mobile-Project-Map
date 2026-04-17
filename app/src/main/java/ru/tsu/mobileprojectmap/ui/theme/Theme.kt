package ru.tsu.mobileprojectmap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = TsuBlue,
    onPrimary = TsuWhite,
    primaryContainer = Color(0xFFDDEEFF),
    onPrimaryContainer = TsuNavy,
    secondary = TsuSky,
    onSecondary = TsuNavy,
    secondaryContainer = Color(0xFFE8F4FF),
    onSecondaryContainer = TsuNavy,
    tertiary = Color(0xFF4B96DB),
    onTertiary = TsuWhite,
    background = TsuIce,
    onBackground = TsuInk,
    surface = TsuWhite,
    onSurface = TsuInk,
    surfaceVariant = Color(0xFFD8E8F8),
    onSurfaceVariant = TsuInk,
    outline = TsuMist
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8EC7FF),
    onPrimary = TsuNavy,
    primaryContainer = Color(0xFF1A4674),
    onPrimaryContainer = Color(0xFFDDEEFF),
    secondary = Color(0xFFAED7FF),
    onSecondary = TsuNavy,
    background = Color(0xFF0E1E2E),
    onBackground = Color(0xFFE8F4FF),
    surface = Color(0xFF13273A),
    onSurface = Color(0xFFE8F4FF),
    surfaceVariant = Color(0xFF243B52),
    onSurfaceVariant = Color(0xFFD8E8F8),
    outline = Color(0xFF6F8BA8)
)

@Composable
fun MobileProjectMapTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
