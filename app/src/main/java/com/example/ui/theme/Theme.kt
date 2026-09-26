package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PlayzoVioletLight,
    onPrimary = Color.Black,
    primaryContainer = PlayzoVioletDark,
    onPrimaryContainer = Color.White,
    secondary = PlayzoCyanLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0E4E5C),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = PlayzoCoral,
    onTertiary = Color.White,
    background = PlayzoDarkBg,
    onBackground = Color(0xFFF9FAFB),
    surface = PlayzoDarkSurface,
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = PlayzoDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = PlayzoDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = PlayzoViolet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = PlayzoVioletDark,
    secondary = PlayzoCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = PlayzoCoral,
    onTertiary = Color.White,
    background = PlayzoLightBg,
    onBackground = Color(0xFF0F172A),
    surface = PlayzoLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = PlayzoLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF64748B),
    outline = PlayzoLightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek dark video cinema mode
    dynamicColor: Boolean = false, // Keep Playzo's signature brand violet & cyan
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
