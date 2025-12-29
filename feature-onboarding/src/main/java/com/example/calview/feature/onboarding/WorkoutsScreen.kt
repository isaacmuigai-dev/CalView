package com.example.calview.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
 * Workouts per week selection screen matching the design.
 * Options: 0-2 (occasional), 3-5 (regular), 6+ (athlete)
 */
@Composable
fun WorkoutsScreen(
    currentStep: Int = 2,
    totalSteps: Int = 3,
    selectedWorkouts: String = "",
    onWorkoutsSelected: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "How many workouts do you do per week?",
        subtitle = "This will be used to calibrate your custom plan.",
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = selectedWorkouts.isNotEmpty()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Workout options
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WorkoutOption(
                value = "0-2",
                title = "0 - 2",
                subtitle = "Workouts now and then",
                dotCount = 1,
                isSelected = selectedWorkouts == "0-2",
                onClick = { onWorkoutsSelected("0-2") }
            )
            
            WorkoutOption(
                value = "3-5",
                title = "3 - 5",
                subtitle = "A few workouts per week",
                dotCount = 4,
                isSelected = selectedWorkouts == "3-5",
                onClick = { onWorkoutsSelected("3-5") }
            )
            
            WorkoutOption(
                value = "6+",
                title = "6+",
                subtitle = "Dedicated athlete",
                dotCount = 6,
                isSelected = selectedWorkouts == "6+",
                onClick = { onWorkoutsSelected("6+") }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun WorkoutOption(
    value: String,
    title: String,
    subtitle: String,
    dotCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
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
            // Dot icon
            WorkoutDotsIcon(
                dotCount = dotCount,
                isSelected = isSelected
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text
            Column {
                Text(
                    text = title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isSelected) Color.White else Color.Black
                )
                Text(
                    text = subtitle,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = if (isSelected) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun WorkoutDotsIcon(
    dotCount: Int,
    isSelected: Boolean
) {
    val bgColor = if (isSelected) Color.White else Color(0xFFF5F5F5)
    val dotColor = if (isSelected) Color(0xFF1C1C1E) else Color.Black
    
    Surface(
        shape = CircleShape,
        color = bgColor,
        modifier = Modifier.size(44.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            when (dotCount) {
                1 -> {
                    // Single dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(dotColor, CircleShape)
                    )
                }
                4 -> {
                    // 4 dots in 2x2 grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
                            Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
                            Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
                        }
                    }
                }
                6 -> {
                    // 6 dots in 2x3 grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(5.dp).background(dotColor, CircleShape))
                            Box(modifier = Modifier.size(5.dp).background(dotColor, CircleShape))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(5.dp).background(dotColor, CircleShape))
                            Box(modifier = Modifier.size(5.dp).background(dotColor, CircleShape))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(5.dp).background(dotColor, CircleShape))
                            Box(modifier = Modifier.size(5.dp).background(dotColor, CircleShape))
                        }
                    }
                }
            }
        }
    }
}
