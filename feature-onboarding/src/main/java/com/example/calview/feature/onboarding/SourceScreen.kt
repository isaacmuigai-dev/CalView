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
fun SourceScreen(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    OnboardingTemplate(
        title = "Where did you hear about us?",
        progress = 1.0f,
        onBack = onBack,
        onContinue = onContinue,
        canContinue = uiState.referralSource.isNotEmpty()
    ) {
        val options = listOf("Tik Tok", "YouTube", "Google", "Play Store", "Facebook", "Friend or family")
        options.forEach { option ->
            CalAICard(
                title = option,
                isSelected = uiState.referralSource == option,
                onClick = { viewModel.onSourceSelected(option) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

