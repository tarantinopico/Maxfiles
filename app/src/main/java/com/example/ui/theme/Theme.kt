package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SophisticatedPrimary,
    onPrimary = SophisticatedOnPrimary,
    secondary = SophisticatedSecondary,
    background = SophisticatedBackground,
    surface = SophisticatedSurface,
    surfaceVariant = SophisticatedSurfaceVariant,
    onBackground = SophisticatedText,
    onSurface = SophisticatedText,
    onSurfaceVariant = SophisticatedTextMuted,
    error = SophisticatedError
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF005AC1),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF535F70),
    background = Color(0xFFFDFBFF),
    surface = Color(0xFFFDFBFF),
    surfaceVariant = Color(0xFFE0E2EC),
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E),
    onSurfaceVariant = Color(0xFF43474E),
    error = Color(0xFFBA1A1A)
)

@Composable
fun MaxFilesTheme(
  useSystemTheme: Boolean = false,
  forceDarkTheme: Boolean = true,
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val darkTheme = if (useSystemTheme) isSystemInDarkTheme() else forceDarkTheme
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
