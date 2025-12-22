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
import com.example.calview.feature.onboarding.components.OnboardingTemplate

@Composable
fun AccomplishmentsScreen(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val options = listOf(
        "Eat and live healthier" to Icons.Default.Apple,
        "Boost my energy and mood" to Icons.Default.LightMode,
        "Stay motivated and consistent" to Icons.Default.FitnessCenter,
        "Feel better about my body" to Icons.Default.SelfImprovement
    )

    OnboardingTemplate(
        title = "What would you like to accomplish?",
        progress = 0.15f, // Early in the flow
        onBack = onBack,
        onContinue = onContinue,
        canContinue = uiState.accomplishments.isNotEmpty()
    ) {
        options.forEach { (text, icon) ->
            val isSelected = uiState.accomplishments.contains(text)
            CalAICard(
                title = text,
                isSelected = isSelected,
                onClick = {
                    val current = uiState.accomplishments.toMutableList()
                    if (isSelected) current.remove(text) else current.add(text)
                    viewModel.onAccomplishmentsSelected(current)
                },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
