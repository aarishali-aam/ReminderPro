package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EditorialLightColorScheme = lightColorScheme(
  primary = Gray800,
  onPrimary = PureWhite,
  primaryContainer = Gray100,
  onPrimaryContainer = Gray800,
  secondary = Gray600,
  onSecondary = PureWhite,
  secondaryContainer = Gray50,
  onSecondaryContainer = Gray800,
  tertiary = Gray700,
  onTertiary = PureWhite,
  tertiaryContainer = Gray100,
  onTertiaryContainer = Gray800,
  background = PureWhite,
  onBackground = Gray800,
  surface = PureWhite,
  onSurface = Gray800,
  surfaceVariant = Gray50,
  onSurfaceVariant = Gray500,
  outline = Gray200,
  outlineVariant = Slate200
)

private val EditorialDarkColorScheme = darkColorScheme(
  primary = PureWhite,
  onPrimary = Gray900,
  primaryContainer = DarkCard,
  onPrimaryContainer = PureWhite,
  secondary = Gray300,
  onSecondary = Gray900,
  secondaryContainer = DarkSurface,
  onSecondaryContainer = Gray200,
  tertiary = Gray400,
  onTertiary = Gray900,
  tertiaryContainer = DarkCard,
  onTertiaryContainer = Gray100,
  background = DarkBg,
  onBackground = PureWhite,
  surface = DarkSurface,
  onSurface = PureWhite,
  surfaceVariant = DarkCard,
  onSurfaceVariant = Gray400,
  outline = DarkBorder,
  outlineVariant = DarkBorder
)

val ChronoShapes = Shapes(
  extraSmall = RoundedCornerShape(6.dp),
  small = RoundedCornerShape(8.dp),
  medium = RoundedCornerShape(12.dp),
  large = RoundedCornerShape(16.dp),
  extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun ChronoPulseTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) EditorialDarkColorScheme else EditorialLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = ChronoShapes,
    content = content
  )
}
