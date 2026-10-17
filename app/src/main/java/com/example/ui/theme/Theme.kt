package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EdoColorScheme = darkColorScheme(
  primary = TealPrimary,
  secondary = AmberSecondary,
  background = DarkBg,
  surface = DarkSurface,
  error = RedError,
  onPrimary = DarkBg,
  onSecondary = DarkBg,
  onBackground = LightText,
  onSurface = LightText,
  onError = LightText
)

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = EdoColorScheme,
    typography = Typography,
    content = content
  )
}
