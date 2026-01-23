package com.example.calview.core.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// BRANDABLE LIGHT THEME COLORS
// Fresh, Modern, Calming Health-Focused Palette
// ============================================

// Primary Colors - Sage Green (calming, health-focused)
val SageGreen = Color(0xFF4A7C59)
val SageGreenLight = Color(0xFF6B9B7A)
val SageGreenDark = Color(0xFF2E5A3B)

// Secondary Colors - Warm Coral (energetic accent)
val WarmCoral = Color(0xFFE8927C)
val WarmCoralLight = Color(0xFFF5B5A4)
val WarmCoralDark = Color(0xFFD97055)

// Tertiary Colors - Soft Lavender (premium feel)
val SoftLavender = Color(0xFF9B8AA8)
val SoftLavenderLight = Color(0xFFBEAEC9)
val SoftLavenderDark = Color(0xFF7A6987)

// Background Colors
val WarmWhite = Color(0xFFFAFAF8)          // Softer than pure white
val PureWhite = Color(0xFFFFFFFF)
val SoftCream = Color(0xFFF8F6F4)          // Card backgrounds
val LightGrey = Color(0xFFE8E8E8)          // Borders, dividers

// Text Colors
val RichBlack = Color(0xFF1A1A1A)          // Primary text
val CharcoalGrey = Color(0xFF3D3D3D)       // Secondary text
val MutedGrey = Color(0xFF7A7A7A)          // Tertiary/muted text
val PlaceholderGrey = Color(0xFFB0B0B0)    // Placeholder text

// Macro Colors (Light Mode) - Vibrant but balanced
val ProteinColor = Color(0xFFE57373)       // Coral red
val CarbsColor = Color(0xFFFFB74D)         // Golden amber
val FatsColor = Color(0xFF64B5F6)          // Sky blue

// Success/Warning/Error
val SuccessGreen = Color(0xFF4CAF50)
val WarningAmber = Color(0xFFFFC107)
val ErrorRed = Color(0xFFE53935)

// Legacy aliases for backwards compatibility
val CreamBackground = WarmWhite
val CardBackground = PureWhite
val DarkCharcoal = RichBlack
val MediumGrey = MutedGrey
val OffWhite = WarmWhite
val DarkGrey = RichBlack
val ShadowColor = Color(0xFFE0E0E0)
val SurfaceVariant = SoftCream
val CalAIGreen = SageGreen
val Avocado = SageGreenDark

// ============================================
// DARK THEME COLORS - Modern & Brandable
// ============================================

// ============================================
// DARK THEME COLORS - REDESIGNED (AMOLED Black)
// ============================================

// Background colors - Pure Black for AMOLED
val AmoledBlack = Color(0xFF000000)
val DarkGrayBackground = AmoledBlack       // Base background is now pure black
val DarkGraySurface = Color(0xFF121212)    // Dark Gray for cards (was background)
val DarkSurfaceVariant = Color(0xFF1E1E1E) // Slightly lighter for input fields

// Accent colors - Health & Vitality
val VibrantHealthGreen = Color(0xFF00FF85) // Vibrant Green (Primary)
val EnergyOrange = Color(0xFFFF9800)       // Muted Orange (Secondary/Alerts)
val CalmingBlue = Color(0xFF2979FF)        // Calming Blue (Tertiary/Info)

// Text colors for dark mode - Pure White
val DarkOffWhite = Color(0xFFFFFFFF)       // Primary text -> Pure White
val DarkMutedText = Color(0xFFB0B0B0)      // Secondary text

// Macro colors (Dark Mode) - Adjusted for contrast
val DarkProteinColor = Color(0xFFFF8A80)   // Soft Red
val DarkCarbsColor = Color(0xFFFFD180)     // Soft Orange
val DarkFatsColor = Color(0xFF80D8FF)      // Soft Blue

// Border and divider colors
val DarkDivider = Color(0xFF2C2C2C)
val DarkBorder = Color(0xFF333333)         // Slightly lighter border for visibility

// Success/Warning colors
val DarkSuccess = VibrantHealthGreen
val DarkWarning = EnergyOrange
val DarkError = Color(0xFFCF6679)

// ============================================
// GRADIENT DEFINITIONS - Bold & Visible
// ============================================

// New Modern Theme Base Colors
val DeepPurple = Color(0xFF451E61)
val ModernCoral = Color(0xFFFB8E6A)

// Light Theme Gradient (White -> Soft Coral -> Soft Purple blend)
val LightGradientStart = Color(0xFFFFFFFF)
val LightGradientMid = Color(0xFFFFF0EB)
val LightGradientEnd = Color(0xFFF5EEF8)

// Dark Theme Gradient (Pure Black for AMOLED)
val DarkGradientStart = AmoledBlack   // Top: Pure Black
val DarkGradientMid = AmoledBlack     // Middle: Pure Black
val DarkGradientEnd = AmoledBlack     // Bottom: Pure Black

// Primary accent colors for new theme - Modern Indigo/Violet
val IndigoPrimary = Color(0xFF6366F1)       // Deep indigo (primary)
val IndigoPrimaryLight = Color(0xFF818CF8)  // Bright indigo
val IndigoPrimaryDark = Color(0xFF4F46E5)   // Rich indigo
val VioletAccent = Color(0xFF8B5CF6)        // Violet accent
val VioletGlow = Color(0xFFA78BFA)          // Soft violet glow

// Legacy teal colors (kept for backwards compatibility)
val TealPrimary = IndigoPrimary
val TealPrimaryLight = IndigoPrimaryLight
val TealPrimaryDark = IndigoPrimaryDark
val MintAccent = VioletAccent               // Map to violet
val AquaGlow = Color(0xFF06B6D4)            // Keep cyan for specific uses

// ============================================
// LIGHT THEME DARK ACCENT COLORS
// Black/Dark accent for buttons, nav bar, and premium elements
// ============================================
val DarkAccentPrimary = Color(0xFF1A1A1A)      // Rich black primary
val DarkAccentSecondary = Color(0xFF2D2D2D)    // Charcoal secondary
val DarkAccentMuted = Color(0xFF404040)        // Muted dark grey
