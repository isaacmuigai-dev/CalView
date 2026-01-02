package com.example.calview.feature.onboarding

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.CalAIButton
import com.example.calview.core.ui.theme.Inter
import androidx.compose.ui.tooling.preview.Preview

/**
 * Welcome screen matching the "Calorie tracking made easy" design.
 * Features:
 * - Language selector (top-right)
 * - Phone mockup with nutrition UI screenshot
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
        color = Color.White,
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
            
            // Main content
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))
                
                // App icon/branding
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🍎",
                            fontSize = 48.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Title
                Text(
                    text = "Calorie tracking\nmade easy",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    lineHeight = 44.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Subtitle
                Text(
                    text = "Scan your food with AI and track\nyour nutrition effortlessly",
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Get Started button
                CalAIButton(
                    text = "Get Started",
                    onClick = onGetStarted
                )
                
                // Sign in link
                Row(
                    modifier = Modifier.padding(top = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an account? ",
                        fontFamily = Inter,
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Sign in",
                        fontFamily = Inter,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onSignIn() }
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}


/**
 * Phone mockup showing the nutrition analysis UI.
 * Simulates a tilted phone with app screenshot.
 */
@Composable
fun PhoneMockup(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Phone frame with shadow and slight rotation
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(0.5f)
                .rotate(-5f)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                ),
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFF1C1C1E) // Dark phone bezel
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Screen content
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
                ) {
                    // Simulated nutrition UI
                    NutritionScreenMockup()
                }
            }
        }
    }
}

/**
 * Simulated nutrition UI content for the phone mockup.
 */
@Composable
fun NutritionScreenMockup() {
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
            Text("2:10", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("••••", fontSize = 10.sp, color = Color.Gray)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Navigation header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF2C2C2E),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Nutrition",
                    color = Color.White,
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
            color = Color(0xFFF5F5F5)
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
            lineHeight = 18.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Nutrition cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NutritionMini("🔥", "Calories", "460", Modifier.weight(1f))
            NutritionMini("🍞", "Carbs", "45g", Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NutritionMini("🥩", "Protein", "25g", Modifier.weight(1f))
            NutritionMini("🧈", "Fat", "20g", Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Health score
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF5F5F5),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("❤️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Health Score", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Text("7/10", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Text(
                    text = "✨ Fix Results",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
            Surface(
                modifier = Modifier.weight(1f),
                color = Color.Black,
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Done",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun NutritionMini(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(label, fontSize = 10.sp, color = Color.Gray)
                Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
