package com.example.calview.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.CalAIButton
import com.example.calview.core.ui.theme.Inter

/**
 * Welcome screen matching the "Calorie tracking made easy" design.
 * Features:
 * - Language selector (top-right)
 * - Phone mockup with nutrition UI screenshot (Vector Style)
 * - "Calorie tracking made easy" title
 * - "Get Started" button
 * - "Already have an account? Sign in" link
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    selectedLanguage: LanguageOption = supportedLanguages.first(),
    onLanguageSelected: (LanguageOption) -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Language selector at top-right
            LanguageSelector(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageSelected,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Phone Mockup taking up almost half the screen
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PhoneMockup(
                        modifier = Modifier
                            .fillMaxHeight(0.75f) // Adjust scale within the available space
                            .aspectRatio(0.48f)
                    )
                }

                // Bottom section: Title, Subtitle, Button
                Column(
                    modifier = Modifier.wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = "Calorie tracking\nmade easy",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp, // Slightly smaller to fit if needed
                        lineHeight = 40.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subtitle
                    Text(
                        text = "Scan your food with AI and track\nyour nutrition effortlessly",
                        fontFamily = Inter,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Get Started button
                    CalAIButton(
                        text = "Get Started",
                        onClick = onGetStarted
                    )

                    // Sign in link
                    Row(
                        modifier = Modifier.padding(top = 24.dp, bottom = 48.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Already have an account? ",
                            fontFamily = Inter,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Sign in",
                            fontFamily = Inter,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .clickable { onSignIn() }
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Sign in to existing account"
                                }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Phone mockup showing the nutrition analysis UI.
 * Vector icon style: Transparent backgrounds, outlined borders.
 */
@Composable
fun PhoneMockup(
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Phone frame - Outlined
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .rotate(-5f),
            shape = RoundedCornerShape(32.dp),
            color = Color.Transparent,
            border = BorderStroke(4.dp, borderColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp) // Bezel spacing
            ) {
                // Inner Screen Outline
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    border = BorderStroke(2.dp, borderColor.copy(alpha = 0.5f))
                ) {
                    // Simulated nutrition UI
                    NutritionScreenMockup(borderColor)
                }
            }
        }
    }
}

/**
 * Simulated nutrition UI content for the phone mockup.
 * Uses transparent backgrounds and borders to blend with screen background.
 */
@Composable
fun NutritionScreenMockup(contentColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Status bar simulation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("2:10", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("••••", fontSize = 10.sp, color = contentColor.copy(alpha = 0.5f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, contentColor)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(contentColor.copy(alpha = 0.1f), shape = RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Nutrition",
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Food image placeholder
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, contentColor.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("🍕🥗", fontSize = 32.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Food title
        Text(
            text = "Turkey Sandwich With\nPotato Chips",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            color = contentColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Nutrition cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NutritionMini("🔥", "Calories", "460", contentColor, Modifier.weight(1f))
            NutritionMini("🍞", "Carbs", "45g", contentColor, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NutritionMini("🥩", "Protein", "25g", contentColor, Modifier.weight(1f))
            NutritionMini("🧈", "Fat", "20g", contentColor, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Health score
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, contentColor.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("❤️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Health Score", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = contentColor)
                }
                Text("7/10", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = contentColor)
            }
        }
    }
}

@Composable
private fun NutritionMini(
    emoji: String,
    label: String,
    value: String,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(label, fontSize = 10.sp, color = contentColor.copy(alpha = 0.7f))
                Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = contentColor)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(
        onGetStarted = {},
        onSignIn = {},
        onLanguageSelected = {}
    )
}
