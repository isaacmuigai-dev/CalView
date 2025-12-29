package com.example.calview.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EggAlt
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SetMeal
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
 * Specific diet selection screen.
 * Options: Classic, Pescatarian, Vegetarian, Vegan
 */
@Composable
fun SpecificDietScreen(
    currentStep: Int,
    totalSteps: Int,
    selectedDiet: String = "",
    onDietSelected: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "Do you follow a specific diet?",
        subtitle = null,
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = selectedDiet.isNotEmpty()
    ) {
        Spacer(modifier = Modifier.weight(0.15f))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DietOption(
                label = "Classic",
                icon = Icons.Filled.Restaurant,
                isSelected = selectedDiet == "Classic",
                onClick = { onDietSelected("Classic") }
            )
            
            DietOption(
                label = "Pescatarian",
                icon = Icons.Filled.SetMeal,
                isSelected = selectedDiet == "Pescatarian",
                onClick = { onDietSelected("Pescatarian") }
            )
            
            DietOption(
                label = "Vegetarian",
                icon = Icons.Filled.EggAlt,
                isSelected = selectedDiet == "Vegetarian",
                onClick = { onDietSelected("Vegetarian") }
            )
            
            DietOption(
                label = "Vegan",
                icon = Icons.Filled.Grass,
                isSelected = selectedDiet == "Vegan",
                onClick = { onDietSelected("Vegan") }
            )
        }
        
        Spacer(modifier = Modifier.weight(0.6f))
    }
}

@Composable
private fun DietOption(
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
