package com.example.calview.ui.screens

import androidx.compose.foundation.layout.Column
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
        Column {
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
}

@Preview(showBackground = true)
@Composable
fun SourceScreenPreview() {
    CalViewTheme {
        // Note: This preview requires a ViewModel. In a real preview, you'd need to provide a mock ViewModel.
    }
}
