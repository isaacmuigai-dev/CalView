package com.example.calview.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DarkCharcoal, // Dark buttons/primary actions
    onPrimary = PureWhite,
    secondary = CalAIGreen, // Green accent
    onSecondary = PureWhite,
    surface = CardBackground, // White cards
    onSurface = DarkCharcoal, // Dark text
    background = CreamBackground, // Cream/pink background
    onBackground = DarkCharcoal,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = MediumGrey,
    outline = LightGrey
)

@Composable
fun CalViewTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

