package com.example.calview.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Restaurant
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
 * What's stopping you from reaching your goals screen.
 * Multiple choice selection with icons.
 */
@Composable
fun WhatsStoppingYouScreen(
    currentStep: Int,
    totalSteps: Int,
    selectedObstacle: String = "",
    onObstacleSelected: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "What's stopping you from reaching your goals?",
        subtitle = null,
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = selectedObstacle.isNotEmpty()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ObstacleOption(
                label = "Lack of consistency",
                icon = Icons.Filled.BarChart,
                isSelected = selectedObstacle == "Lack of consistency",
                onClick = { onObstacleSelected("Lack of consistency") }
            )
            
            ObstacleOption(
                label = "Unhealthy eating habits",
                icon = Icons.Filled.Fastfood,
                isSelected = selectedObstacle == "Unhealthy eating habits",
                onClick = { onObstacleSelected("Unhealthy eating habits") }
            )
            
            ObstacleOption(
                label = "Lack of supports",
                icon = Icons.Filled.Handshake,
                isSelected = selectedObstacle == "Lack of supports",
                onClick = { onObstacleSelected("Lack of supports") }
            )
            
            ObstacleOption(
                label = "Busy schedule",
                icon = Icons.Filled.CalendarMonth,
                isSelected = selectedObstacle == "Busy schedule",
                onClick = { onObstacleSelected("Busy schedule") }
            )
            
            ObstacleOption(
                label = "Lack of meal inspiration",
                icon = Icons.Filled.Restaurant,
                isSelected = selectedObstacle == "Lack of meal inspiration",
                onClick = { onObstacleSelected("Lack of meal inspiration") }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ObstacleOption(
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
