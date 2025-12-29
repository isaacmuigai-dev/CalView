package com.example.calview.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import com.example.calview.feature.onboarding.components.OnboardingScreenLayout

/**
 * Goal selection screen.
 * Options: Lose Weight, Maintain, Gain Weight
 */
@Composable
fun GoalScreen(
    currentStep: Int = 7,
    totalSteps: Int = 7,
    selectedGoal: String = "",
    onGoalSelected: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "What is your goal?",
        subtitle = "This helps us generate a plan for your calorie intake.",
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = selectedGoal.isNotEmpty()
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        
        // Goal options
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GoalOption(
                label = "Lose Weight",
                isSelected = selectedGoal == "Lose Weight",
                onClick = { onGoalSelected("Lose Weight") }
            )
            
            GoalOption(
                label = "Maintain",
                isSelected = selectedGoal == "Maintain",
                onClick = { onGoalSelected("Maintain") }
            )
            
            GoalOption(
                label = "Gain Weight",
                isSelected = selectedGoal == "Gain Weight",
                onClick = { onGoalSelected("Gain Weight") }
            )
        }
        
        Spacer(modifier = Modifier.weight(0.7f))
    }
}

@Composable
private fun GoalOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF1C1C1E) else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE5E5E5))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
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
