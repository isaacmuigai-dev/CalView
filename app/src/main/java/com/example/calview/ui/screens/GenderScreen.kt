package com.example.calview.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.ui.components.OnboardingOption
import com.example.calview.ui.screens.components.OnboardingTemplate
import com.example.calview.ui.viewmodels.OnboardingViewModel

@Composable
fun GenderScreen(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    GenderContent(
        selectedGender = uiState.gender,
        onGenderSelected = { viewModel.onGenderSelected(it) },
        onBack = onBack,
        onContinue = onContinue
    )
}

@Composable
fun GenderContent(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    OnboardingTemplate(
        title = "Choose your Gender",
        subtitle = "This will be used to calibrate your custom plan.",
        progress = 0.3f,
        onBack = onBack,
        onContinue = onContinue,
        canContinue = selectedGender.isNotEmpty()
    ) {
        val options = listOf("Female", "Male", "Other")
        options.forEach { option ->
            OnboardingOption(
                text = option,
                isSelected = selectedGender == option,
                onClick = { onGenderSelected(option) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GenderScreenPreview() {
    GenderContent(
        selectedGender = "Female",
        onGenderSelected = {},
        onBack = {},
        onContinue = {}
    )
}
