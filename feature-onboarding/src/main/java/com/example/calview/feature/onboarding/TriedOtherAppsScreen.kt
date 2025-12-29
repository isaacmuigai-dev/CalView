package com.example.calview.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
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
 * Screen asking if user has tried other calorie tracking apps.
 * Options: Yes (thumbs up), No (thumbs down)
 */
@Composable
fun TriedOtherAppsScreen(
    currentStep: Int = 4,
    totalSteps: Int = 7,
    selectedAnswer: String = "",
    onAnswerSelected: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "Have you tried other calorie tracking apps?",
        subtitle = null,
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = selectedAnswer.isNotEmpty()
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TriedAppsOption(
                label = "Yes",
                icon = Icons.Filled.ThumbUp,
                isSelected = selectedAnswer == "Yes",
                onClick = { onAnswerSelected("Yes") }
            )
            
            TriedAppsOption(
                label = "No",
                icon = Icons.Filled.ThumbDown,
                isSelected = selectedAnswer == "No",
                onClick = { onAnswerSelected("No") }
            )
        }
        
        Spacer(modifier = Modifier.weight(0.7f))
    }
}

@Composable
private fun TriedAppsOption(
    label: String,
    icon: ImageVector,
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
                .padding(horizontal = 20.dp),
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
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color(0xFF1C1C1E) else Color.Black,
                        modifier = Modifier.size(20.dp)
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
