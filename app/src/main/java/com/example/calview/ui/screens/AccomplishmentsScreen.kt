package com.example.calview.ui.screens

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.calview.ui.components.CalAICard
import com.example.calview.ui.screens.components.OnboardingTemplate
import com.example.calview.ui.viewmodels.OnboardingViewModel

@Composable
fun AccomplishmentsScreen(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    AccomplishmentsContent(
        selectedAccomplishments = uiState.accomplishments,
        onAccomplishmentToggle = { text ->
            val current = uiState.accomplishments.toMutableList()
            if (current.contains(text)) current.remove(text) else current.add(text)
            viewModel.onAccomplishmentsSelected(current)
        },
        onBack = onBack,
        onContinue = onContinue
    )
}

@Composable
fun AccomplishmentsContent(
    selectedAccomplishments: List<String>,
    onAccomplishmentToggle: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val options = listOf(
        "Eat and live healthier" to Icons.Filled.Eco,
        "Boost my energy and mood" to Icons.Filled.LightMode,
        "Stay motivated and consistent" to Icons.Filled.FitnessCenter,
        "Feel better about my body" to Icons.Filled.SelfImprovement
    )

    OnboardingTemplate(
        title = "What would you like to accomplish?",
        progress = 0.15f,
        onBack = onBack,
        onContinue = onContinue,
        canContinue = selectedAccomplishments.isNotEmpty()
    ) {
        options.forEach { (text, icon) ->
            val isSelected = selectedAccomplishments.contains(text)
            CalAICard(
                title = text,
                isSelected = isSelected,
                onClick = { onAccomplishmentToggle(text) },
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

@Preview(showBackground = true)
@Composable
fun AccomplishmentsScreenPreview() {
    AccomplishmentsContent(
        selectedAccomplishments = listOf("Eat and live healthier"),
        onAccomplishmentToggle = {},
        onBack = {},
        onContinue = {}
    )
}
