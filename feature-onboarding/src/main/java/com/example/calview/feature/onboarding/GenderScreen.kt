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
 * Gender selection screen matching the design.
 * Options: Female, Male, Other
 */
@Composable
fun GenderScreen(
    currentStep: Int = 1,
    totalSteps: Int = 3,
    selectedGender: String = "",
    onGenderSelected: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "Choose your Gender",
        subtitle = "This will be used to calibrate your custom plan.",
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = selectedGender.isNotEmpty()
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        
        // Gender options
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GenderOption(
                label = "Female",
                isSelected = selectedGender == "Female",
                onClick = { onGenderSelected("Female") }
            )
            
            GenderOption(
                label = "Male",
                isSelected = selectedGender == "Male",
                onClick = { onGenderSelected("Male") }
            )
            
            GenderOption(
                label = "Other",
                isSelected = selectedGender == "Other",
                onClick = { onGenderSelected("Other") }
            )
        }
        
        Spacer(modifier = Modifier.weight(0.7f))
    }
}

@Composable
private fun GenderOption(
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
