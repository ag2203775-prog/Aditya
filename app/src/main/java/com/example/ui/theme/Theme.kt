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
    primary = PrimaryIndigo,
    onPrimary = OnPrimaryDark,
    secondary = SecondaryPurple,
    background = CosmicDarkBg,
    surface = CardDarkBg,
    onBackground = TextDarkPrimary,
    onSurface = TextDarkPrimary,
    surfaceVariant = NumericKeyDark,
    onSurfaceVariant = TextDarkSecondary,
    primaryContainer = OpKeyDark,
    onPrimaryContainer = TextDarkPrimary,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFCA5A5)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigoLight,
    onPrimary = Color.White,
    secondary = SecondaryPurpleLight,
    background = CosmicLightBg,
    surface = CardLightBg,
    onBackground = TextLightPrimary,
    onSurface = TextLightPrimary,
    surfaceVariant = NumericKeyLight,
    onSurfaceVariant = TextLightSecondary,
    primaryContainer = OpKeyLight,
    onPrimaryContainer = TextLightPrimary,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFFB91C1C)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Ensure our high-contrast aesthetic is consistently applied on all devices
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
