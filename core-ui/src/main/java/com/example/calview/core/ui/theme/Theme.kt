package com.example.calview.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DarkAccentPrimary,             // Dark black for primary actions
    onPrimary = PureWhite,
    secondary = DarkAccentSecondary,         // Charcoal accent
    onSecondary = PureWhite,
    tertiary = DarkAccentMuted,              // Muted dark for tertiary
    onTertiary = PureWhite,
    surface = PureWhite,                  // White cards
    onSurface = RichBlack,                // Rich black text
    background = WarmWhite,               // Warm white background
    onBackground = RichBlack,
    surfaceVariant = SoftCream,           // Soft cream for variants
    onSurfaceVariant = MutedGrey,
    outline = LightGrey,
    outlineVariant = LightGrey,
    error = ErrorRed,
    onError = PureWhite,
    primaryContainer = Color(0xFFE8E8E8), // Light grey for containers
    onPrimaryContainer = DarkAccentPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryLight,         // Bright indigo for primary actions
    onPrimary = DarkBackground,
    secondary = VioletAccent,             // Violet accent
    onSecondary = DarkBackground,
    tertiary = VioletGlow,                // Soft violet accent
    onTertiary = DarkBackground,
    surface = DarkSurface,                // Elevated dark slate
    onSurface = DarkOnSurface,            // Near white text
    background = DarkBackground,          // Deep space black
    onBackground = DarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMutedGrey,
    outline = DarkBorder,
    outlineVariant = DarkDivider,
    inverseSurface = PureWhite,
    inverseOnSurface = DarkBackground,
    primaryContainer = Color(0xFF312E81), // Indigo-900 for dark mode containers
    onPrimaryContainer = IndigoPrimaryLight
)

// Theme Gradient CompositionLocal
data class ThemeGradient(
    val brush: androidx.compose.ui.graphics.Brush
)

val LocalThemeGradient = androidx.compose.runtime.staticCompositionLocalOf {
    ThemeGradient(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(androidx.compose.ui.graphics.Color.White, androidx.compose.ui.graphics.Color.White)))
}

/**
 * CalView Theme with support for Light, Dark, and System automatic theme.
 * 
 * @param darkTheme Whether to use dark theme. If null, follows system setting.
 * @param appearanceMode "light", "dark", or "automatic" - controls theme selection
 */
@Composable
fun CalViewTheme(
    appearanceMode: String = "automatic",
    content: @Composable () -> Unit
) {
    val systemDarkTheme = isSystemInDarkTheme()
    
    val useDarkTheme = when (appearanceMode) {
        "dark" -> true
        "light" -> false
        else -> systemDarkTheme // "automatic" follows system
    }
    
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme
    
    // Define gradient based on theme - 3 color gradient for visible transition
    val gradientBrush = if (useDarkTheme) {
        androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(DarkGradientStart, DarkGradientMid, DarkGradientEnd)
        )
    } else {
        androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(LightGradientStart, LightGradientMid, LightGradientEnd)
        )
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalThemeGradient provides ThemeGradient(gradientBrush)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

object CalViewTheme {
    val gradient: androidx.compose.ui.graphics.Brush
        @Composable
        get() = LocalThemeGradient.current.brush
}


