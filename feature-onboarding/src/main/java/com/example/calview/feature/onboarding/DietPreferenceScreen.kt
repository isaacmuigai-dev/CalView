package com.example.calview.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.calview.core.ui.components.CalAICard
import com.example.calview.core.ui.components.CalAIButton
import com.example.calview.feature.onboarding.components.OnboardingTemplate

@Composable
fun DietPreferenceScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val diets = listOf(
        DietOption("Classic", "🍗"), // Using emoji as placeholder for custom icons
        DietOption("Pescatarian", "🐟"),
        DietOption("Vegetarian", "🍎"),
        DietOption("Vegan", "🌿")
    )

    OnboardingTemplate(
        title = "Do you follow a specific diet?",
        progress = 0.8f,
        onBack = { /* Handled by NavHost */ }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            diets.forEach { diet ->
                CalAICard(
                    title = diet.name,
                    isSelected = uiState.dietPreference == diet.name,
                    onClick = { viewModel.onDietSelected(diet.name) },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFF5F5F5), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Text(diet.icon, fontSize = 20.sp)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            CalAIButton(
                text = "Continue",
                onClick = onNext,
                enabled = uiState.dietPreference.isNotEmpty()
            )
        }
    }
}

private data class DietOption(val name: String, val icon: String)

// Need to import sp from unit
import androidx.compose.ui.unit.sp
