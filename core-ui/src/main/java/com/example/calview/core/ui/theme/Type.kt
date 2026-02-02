package com.example.calview.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font as GoogleFontRes
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

// =============================================================================
// PREMIUM TYPOGRAPHY SYSTEM
// Designed to beat Cal AI with sharper, more intentional typography
// =============================================================================

// Google Fonts Provider
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.example.calview.core.ui.R.array.com_google_android_gms_fonts_certs
)

// =============================================================================
// PRIMARY FONT: INTER
// Why Inter beats SF Pro:
// - Sharper numerals (huge for calories & macros)
// - Better contrast at small sizes
// - Feels more data-driven & modern
// - Extremely polished on Android
// =============================================================================
private val interFont = GoogleFont("Inter")

val InterFontFamily = FontFamily(
    GoogleFontRes(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    GoogleFontRes(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    GoogleFontRes(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    GoogleFontRes(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    GoogleFontRes(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.ExtraBold),
)

val Inter = InterFontFamily

// =============================================================================
// BRAND FONT: PLUS JAKARTA SANS
// Premium choice for "CalViewAI" branding. 
// Sleeker, more modern, and highly legible.
// =============================================================================
private val plusJakartaSansFont = GoogleFont("Plus Jakarta Sans")

val PlusJakartaSansFontFamily = FontFamily(
    GoogleFontRes(googleFont = plusJakartaSansFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    GoogleFontRes(googleFont = plusJakartaSansFont, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    GoogleFontRes(googleFont = plusJakartaSansFont, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    GoogleFontRes(googleFont = plusJakartaSansFont, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    GoogleFontRes(googleFont = plusJakartaSansFont, fontProvider = googleFontProvider, weight = FontWeight.ExtraBold),
)

// Alias for branding to allow easy switching
val BrandingFontFamily = PlusJakartaSansFontFamily

// =============================================================================
// BRAND FONT: WATERLILY
// Used for "CalViewAI" branding on splash and dashboard
// =============================================================================
// [DEPRECATED] Old brand font
val WaterlilyFontFamily = FontFamily(
    Font(com.example.calview.core.ui.R.font.waterlily, FontWeight.Normal)
)

// =============================================================================
// SECONDARY FONT: SPACE GROTESK (For Numbers - Premium Feel)
// Makes numbers feel futuristic while text stays readable
// Used sparingly for hero numbers = chef's kiss
// =============================================================================
// =============================================================================
// SECONDARY FONT: SPACE GROTESK (MAPPED TO INTER FOR UNIFORMITY)
// The user requested all text to be Inter, so we simply alias the Space Grotesk
// family to use the Inter font definition while keeping the variable name
// to avoid breaking existing usages.
// =============================================================================
// private val spaceGroteskFont = GoogleFont("Space Grotesk") // DISABLED

val SpaceGroteskFontFamily = FontFamily(
    GoogleFontRes(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    GoogleFontRes(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    GoogleFontRes(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    GoogleFontRes(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Bold),
)

// =============================================================================
// EXTENDED TYPOGRAPHY - Semantic Text Styles
// These go beyond Material3's default Typography for fitness/AI-specific needs
// =============================================================================

@Immutable
data class CalViewTypography(
    // =========================================================================
    // HERO NUMBERS (Calories, Macros, Stats)
    // Font: Space Grotesk | Weight: Bold (700-800) | Tracking: -2%
    // Tighter tracking makes numbers feel confident & premium
    // =========================================================================
    val heroNumber: TextStyle,
    val heroNumberLarge: TextStyle,
    val heroNumberSmall: TextStyle,
    
    // =========================================================================
    // MACRO NUMBERS (Protein, Carbs, Fat values)
    // Slightly smaller than hero, but still prominent
    // =========================================================================
    val macroNumber: TextStyle,
    val macroLabel: TextStyle,
    
    // =========================================================================
    // PRIMARY HEADLINES
    // Font: Inter | Weight: SemiBold (600)
    // Use for: "Calories left", "Health score", Section titles
    // Avoid Bold here - Cal AI overuses bold, which flattens hierarchy
    // =========================================================================
    val sectionTitle: TextStyle,
    val cardTitle: TextStyle,
    
    // =========================================================================
    // SECONDARY LABELS (UI Microcopy)
    // Font: Inter | Weight: Medium (500) | Opacity: 70-80%
    // Use for: "Protein left", "Recently uploaded", Tabs
    // Makes UI feel lighter and more breathable
    // =========================================================================
    val secondaryLabel: TextStyle,
    val tabLabel: TextStyle,
    val chipLabel: TextStyle,
    
    // =========================================================================
    // EXPLANATORY TEXT / AI FEEDBACK
    // Font: Inter | Weight: Regular (400) | Line height: 1.45-1.55
    // This is where Cal AI feels dense. We feel coach-like instead.
    // =========================================================================
    val aiInsight: TextStyle,
    val bodyText: TextStyle,
    val caption: TextStyle,
    
    // =========================================================================
    // BUTTON & ACTION TEXT
    // Clear, confident, action-oriented
    // =========================================================================
    val buttonLarge: TextStyle,
    val buttonMedium: TextStyle,
    val buttonSmall: TextStyle,
    
    // =========================================================================
    // DATA LABELS (Charts, Progress indicators)
    // Tabular numbers, tight tracking
    // =========================================================================
    val dataValue: TextStyle,
    val dataLabel: TextStyle,
    val percentageValue: TextStyle,
)

// Tracking values converted to em units (sp)
// -2% tracking = -0.02em
private val tightTracking = (-0.02).sp
private val normalTracking = 0.sp
private val wideTracking = 0.5.sp

val LocalCalViewTypography = staticCompositionLocalOf {
    CalViewTypography(
        // Hero Numbers - Space Grotesk for that futuristic premium feel
        heroNumber = TextStyle(
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            lineHeight = 52.sp,
            letterSpacing = tightTracking
        ),
        heroNumberLarge = TextStyle(
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 64.sp,
            lineHeight = 68.sp,
            letterSpacing = tightTracking
        ),
        heroNumberSmall = TextStyle(
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            lineHeight = 40.sp,
            letterSpacing = tightTracking
        ),
        
        // Macro Numbers
        macroNumber = TextStyle(
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 28.sp,
            letterSpacing = tightTracking
        ),
        macroLabel = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = normalTracking
        ),
        
        // Section Titles - SemiBold, not Bold (cleaner hierarchy)
        sectionTitle = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = normalTracking
        ),
        cardTitle = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = normalTracking
        ),
        
        // Secondary Labels - Medium weight, lighter feel
        secondaryLabel = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = normalTracking
        ),
        tabLabel = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = wideTracking
        ),
        chipLabel = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = normalTracking
        ),
        
        // AI/Explanatory Text - Regular weight, generous line height
        aiInsight = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 23.sp, // ~1.53 ratio for readability
            letterSpacing = normalTracking
        ),
        bodyText = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 21.sp, // ~1.5 ratio
            letterSpacing = normalTracking
        ),
        caption = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = normalTracking
        ),
        
        // Buttons
        buttonLarge = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = normalTracking
        ),
        buttonMedium = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = normalTracking
        ),
        buttonSmall = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = wideTracking
        ),
        
        // Data Values (charts, progress)
        dataValue = TextStyle(
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = tightTracking
        ),
        dataLabel = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = normalTracking
        ),
        percentageValue = TextStyle(
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = tightTracking
        ),
    )
}

// =============================================================================
// MATERIAL 3 TYPOGRAPHY (Maps to Material components)
// All using Inter for consistency across Material components
// =============================================================================

val Typography = Typography(
    // Display styles - for very large text
    displayLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = tightTracking
    ),
    displayMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = tightTracking
    ),
    displaySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = tightTracking
    ),
    
    // Headlines - SemiBold for cleaner hierarchy (not Bold like Cal AI)
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = normalTracking
    ),
    headlineMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = normalTracking
    ),
    headlineSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = normalTracking
    ),
    
    // Titles
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = normalTracking
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = normalTracking
    ),
    titleSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = normalTracking
    ),
    
    // Body text - Regular weight with generous line height
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp, // 1.5 ratio
        letterSpacing = normalTracking
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp, // ~1.5 ratio
        letterSpacing = normalTracking
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp, // 1.5 ratio
        letterSpacing = normalTracking
    ),
    
    // Labels - Medium weight for UI elements
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = normalTracking
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = wideTracking
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = wideTracking
    )
)
