package com.example.calview.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import com.example.calview.feature.onboarding.components.OnboardingScreenLayout

/**
 * What would you like to accomplish screen.
 * Options: Eat and live healthier, Boost my energy and mood, 
 * Stay motivated and consistent, Feel better about my body
 */
@Composable
fun AccomplishScreen(
    currentStep: Int,
    totalSteps: Int,
    selectedAccomplishment: String = "",
    onAccomplishmentSelected: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "What would you like to accomplish?",
        subtitle = null,
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = selectedAccomplishment.isNotEmpty()
    ) {
        Spacer(modifier = Modifier.weight(0.15f))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AccomplishOption(
                label = "Eat and live healthier",
                emoji = "🍎",
                isSelected = selectedAccomplishment == "Eat and live healthier",
                onClick = { onAccomplishmentSelected("Eat and live healthier") }
            )
            
            AccomplishOption(
                label = "Boost my energy and mood",
                emoji = "☀️",
                isSelected = selectedAccomplishment == "Boost my energy and mood",
                onClick = { onAccomplishmentSelected("Boost my energy and mood") }
            )
            
            AccomplishOption(
                label = "Stay motivated and consistent",
                emoji = "🔥",
                isSelected = selectedAccomplishment == "Stay motivated and consistent",
                onClick = { onAccomplishmentSelected("Stay motivated and consistent") }
            )
            
            AccomplishOption(
                label = "Feel better about my body",
                emoji = "🧘",
                isSelected = selectedAccomplishment == "Feel better about my body",
                onClick = { onAccomplishmentSelected("Feel better about my body") }
            )
        }
        
        Spacer(modifier = Modifier.weight(0.6f))
    }
}

@Composable
private fun AccomplishOption(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF1C1C1E) else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE5E5E5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Surface(
                shape = CircleShape,
                color = if (isSelected) Color.White else Color(0xFFF5F5F5),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = emoji,
                        fontSize = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = label,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = if (isSelected) Color.White else Color.Black
            )
        }
    }
}
