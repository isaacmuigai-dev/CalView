package com.example.calview.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
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
    primary = Color.White,                // White accents for buttons etc.
    onPrimary = Color.Black,
    secondary = EnergyOrange,             // ALERT: Muted Orange (#FF9800)
    onSecondary = Color.Black,            // Black text on orange
    tertiary = CalmingBlue,               // INFO: Calming Blue
    onTertiary = Color.Black,
    surface = DarkGraySurface,            // Elevated Gray (#1C1C1C)
    onSurface = DarkOffWhite,             // Off-White text (#E0E0E0)
    background = DarkGrayBackground,      // Base Dark Gray (#121212)
    onBackground = DarkOffWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMutedText,
    outline = DarkBorder,
    outlineVariant = DarkDivider,
    inverseSurface = DarkOffWhite,
    inverseOnSurface = DarkGrayBackground,
    primaryContainer = Color(0xFF333333), // Dark grey container for white primary
    onPrimaryContainer = Color.White,
    error = DarkError,
    onError = Color.Black
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
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // For Android 15+ (SDK 35), edge-to-edge is enforced by default
            // We only need to set the system bar appearance (light/dark icons)
            // rely on enableEdgeToEdge() in MainActivity for transparent bars on older versions
            
            // Set system bar icon colors (light icons for dark theme, dark icons for light theme)
            insetsController.isAppearanceLightStatusBars = !useDarkTheme
            insetsController.isAppearanceLightNavigationBars = !useDarkTheme
        }
    }

    CompositionLocalProvider(
        LocalThemeGradient provides ThemeGradient(gradientBrush),
        LocalCalViewTypography provides LocalCalViewTypography.current
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
    /**
     * The theme gradient brush for backgrounds
     */
    val gradient: androidx.compose.ui.graphics.Brush
        @Composable
        get() = LocalThemeGradient.current.brush
    
    /**
     * Premium typography system with Inter + Space Grotesk
     * 
     * Usage examples:
     * - CalViewTheme.typography.heroNumber for large calorie displays
     * - CalViewTheme.typography.macroNumber for protein/carbs/fat values
     * - CalViewTheme.typography.sectionTitle for section headers
     * - CalViewTheme.typography.aiInsight for AI feedback text
     * - CalViewTheme.typography.dataValue for chart values
     */
    val typography: CalViewTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalCalViewTypography.current
}


