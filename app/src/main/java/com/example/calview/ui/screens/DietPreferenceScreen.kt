package com.example.calview.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.calview.ui.components.CalAICard
import com.example.calview.ui.components.CalAIButton
import com.example.calview.ui.screens.components.OnboardingTemplate
import com.example.calview.ui.theme.CalViewTheme
import com.example.calview.ui.viewmodels.OnboardingViewModel

@Composable
fun DietPreferenceScreen(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    data class DietOption(val name: String, val icon: String)

    val diets = listOf(
        DietOption("Classic", "🍗"), // Using emoji as placeholder for custom icons
        DietOption("Pescatarian", "🐟"),
        DietOption("Vegetarian", "🍎"),
        DietOption("Vegan", "🌿")
    )

    OnboardingTemplate(
        title = "Do you follow a specific diet?",
        progress = 0.8f,
        onBack = onBack,
        onContinue = onContinue,
        canContinue = uiState.dietPreference.isNotEmpty()
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
                            Text(diet.icon, fontSize = 20.sp)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DietPreferenceScreenPreview() {
    CalViewTheme {
        // Note: This preview requires a ViewModel. In a real preview, you'd need to provide a mock ViewModel.
    }
}
