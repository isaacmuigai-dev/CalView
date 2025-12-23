package com.example.calview.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.calview.ui.components.CalAICard
import com.example.calview.ui.components.CalAIButton
import com.example.calview.ui.screens.components.OnboardingTemplate
import com.example.calview.ui.theme.CalViewTheme
import com.example.calview.ui.viewmodels.OnboardingViewModel

@Composable
fun GoalSelectionScreen(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val goals = listOf("Lose Weight", "Maintain", "Gain Weight")

    OnboardingTemplate(
        title = "What is your goal?",
        subtitle = "This helps us generate a plan for your calorie intake.",
        progress = 0.6f,
        onBack = onBack,
        onContinue = onContinue,
        canContinue = uiState.goal.isNotEmpty()
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GoalSelectionScreenPreview() {
    CalViewTheme {
        // Note: This preview requires a ViewModel. In a real preview, you'd need to provide a mock ViewModel.
    }
}
