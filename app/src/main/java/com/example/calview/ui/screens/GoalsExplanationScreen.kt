package com.example.calview.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.calview.ui.components.CalAICard
import com.example.calview.ui.components.CalorieRing
import com.example.calview.ui.screens.components.OnboardingTemplate

@Composable
fun GoalsExplanationScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    OnboardingTemplate(
        title = "How to reach your goals:",
        progress = 0.99f, // Positioning it near the end or after plan ready
        onBack = onBack,
        onContinue = onContinue
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GoalBenefitCard(
                icon = Icons.Filled.Egg,
                iconTint = Color(0xFFD64D50),
                text = "Get your weekly life score and improve your routine."
            )

            GoalBenefitCard(
                icon = Icons.Filled.Eco,
                iconTint = Color(0xFF6A9B7E),
                text = "Track your food"
            )

            GoalBenefitCard(
                customIcon = {
                    Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                        CalorieRing(
                            consumed = 0,
                            goal = 1,
                        )
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.Black
                        )
                    }
                },
                text = "Follow your daily calorie recommendation"
            )

            GoalBenefitCard(
                customIcon = {
                    Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                        // Triple ring placeholder
                        repeat(3) { i ->
                            CalorieRing(
                                consumed = 0,
                                goal = 1,
                                modifier = Modifier.padding((i * 4).dp)
                            )
                        }
                    }
                },
                text = "Balance your carbs, protein, fat"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GoalsExplanationScreenPreview() {
    GoalsExplanationScreen(
        onContinue = {},
        onBack = {}
    )
}

@Composable
fun GoalBenefitCard(
    icon: ImageVector? = null,
    iconTint: Color = Color.Black,
    customIcon: (@Composable () -> Unit)? = null,
    text: String
) {
    CalAICard {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (customIcon != null) {
                customIcon()
            } else if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                lineHeight = 24.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}
