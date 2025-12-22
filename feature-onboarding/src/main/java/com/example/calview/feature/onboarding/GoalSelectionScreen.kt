package com.example.calview.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calview.core.ui.components.CalAICard
import com.example.calview.core.ui.components.CalAIButton
import com.example.calview.feature.onboarding.components.OnboardingTemplate

@Composable
fun GoalSelectionScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val goals = listOf("Lose Weight", "Maintain", "Gain Weight")

    OnboardingTemplate(
        title = "What is your goal?",
        subtitle = "This helps us generate a plan for your calorie intake.",
        progress = 0.6f,
        onBack = { /* Handled by NavHost */ }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            goals.forEach { goal ->
                CalAICard(
                    title = goal,
                    isSelected = uiState.goal == goal,
                    onClick = { viewModel.onGoalSelected(goal) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            CalAIButton(
                text = "Continue",
                onClick = onNext,
                enabled = uiState.goal.isNotEmpty()
            )
        }
    }
}
