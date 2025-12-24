package com.example.calview.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.calview.feature.onboarding.components.OnboardingTemplate

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

@Composable
fun OnboardingOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
