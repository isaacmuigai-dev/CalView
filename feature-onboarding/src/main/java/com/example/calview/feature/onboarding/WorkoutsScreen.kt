package com.example.calview.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.CalAICard
import com.example.calview.feature.onboarding.components.OnboardingTemplate

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

