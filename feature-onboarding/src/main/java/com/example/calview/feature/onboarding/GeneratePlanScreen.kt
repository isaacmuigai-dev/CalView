package com.example.calview.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import com.example.calview.feature.onboarding.components.OnboardingScreenLayout

/**
 * Time to generate your custom plan screen.
 * Shows Korean finger heart illustration with "All done!" badge.
 */
@Composable
fun GeneratePlanScreen(
    currentStep: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "",
        subtitle = null,
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = true
    ) {
        Spacer(modifier = Modifier.weight(0.1f))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circle with Korean finger heart illustration
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Korean finger heart with love emoji
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "❤️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "🤞", fontSize = 80.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // All done! badge
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✅",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "All done!",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = "Time to generate your\ncustom plan!",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )
        }
        
        Spacer(modifier = Modifier.weight(0.4f))
    }
}
