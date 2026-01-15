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

// Background colors - Deep space aesthetic
val DarkBackground = Color(0xFF0F0F14)     // Deep space black
val DarkSurface = Color(0xFF1A1A22)        // Elevated dark slate
val DarkSurfaceVariant = Color(0xFF252530) // Slightly lighter for cards
val DarkCardBackground = Color(0xFF1E1E28) // Card background with slight purple tint

// Accent colors - Electric & vibrant
val ElectricCyan = Color(0xFF00D9FF)       // Primary accent - electric cyan
val VibrantPurple = Color(0xFF7B61FF)      // Secondary accent
val SoftPink = Color(0xFFFF6B9D)           // Tertiary accent

// Text colors for dark mode
val DarkOnBackground = Color(0xFFF5F5F7)   // Near white text
val DarkOnSurface = Color(0xFFE8E8ED)      // Slightly dimmer
val DarkMutedGrey = Color(0xFF6E6E78)      // Muted secondary text

// Macro colors (Dark Mode) - Softer, easier on eyes
val DarkProteinColor = Color(0xFFFF7B93)   // Soft coral
val DarkCarbsColor = Color(0xFFFFB366)     // Warm orange
val DarkFatsColor = Color(0xFF66D4FF)      // Sky blue

// Border and divider colors for dark mode
val DarkDivider = Color(0xFF2A2A35)
val DarkBorder = Color(0xFF3A3A45)

// Success/Warning colors for dark mode
val DarkSuccess = Color(0xFF4ADE80)        // Green
val DarkWarning = Color(0xFFFFA726)        // Orange
val DarkError = Color(0xFFEF5350)          // Red

// ============================================
// GRADIENT DEFINITIONS - Bold & Visible
// ============================================

// New Modern Theme Base Colors
val DeepPurple = Color(0xFF451E61)          // #451E61 - Rich deep purple
val ModernCoral = Color(0xFFFB8E6A)         // #FB8E6A - Vibrant coral/peach

// Light Theme Gradient (White -> Soft Coral -> Soft Purple blend)
val LightGradientStart = Color(0xFFFFFFFF)  // Pure white
val LightGradientMid = Color(0xFFFFF0EB)    // White with soft coral tint
val LightGradientEnd = Color(0xFFF5EEF8)    // White with soft purple tint

// Dark Theme Gradient (Subtle Purple-Black - from Cal AI reference)
val DarkGradientStart = Color(0xFF16162A)   // Top: Deep purple-tinted black
val DarkGradientMid = Color(0xFF0F0F15)     // Middle: Very dark charcoal
val DarkGradientEnd = Color(0xFF0A0A0E)     // Bottom: Near black

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
