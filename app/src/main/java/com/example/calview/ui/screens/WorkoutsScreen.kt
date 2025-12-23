package com.example.calview.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.calview.ui.components.CalAICard
import com.example.calview.ui.screens.components.OnboardingTemplate
import com.example.calview.ui.theme.CalViewTheme
import com.example.calview.ui.viewmodels.OnboardingViewModel

@Composable
fun WorkoutsScreen(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    OnboardingTemplate(
        title = "How many workouts do you do per week?",
        subtitle = "This will be used to calibrate your custom plan.",
        progress = 0.55f,
        onBack = onBack,
        onContinue = onContinue,
        canContinue = uiState.workoutsPerWeek.isNotEmpty()
    ) {
        val options = listOf(
            "0 - 2" to "Workouts now and then",
            "3 - 5" to "A few workouts per week",
            "6+" to "Dedicated athlete"
        )
        options.forEach { (title, desc) ->
            CalAICard(
                title = title,
                subtitle = desc,
                isSelected = uiState.workoutsPerWeek == title,
                onClick = { viewModel.onWorkoutsSelected(title) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutsScreenPreview() {
    CalViewTheme {
        // Note: This preview requires a ViewModel. In a real preview, you'd need to provide a mock ViewModel.
    }
}
