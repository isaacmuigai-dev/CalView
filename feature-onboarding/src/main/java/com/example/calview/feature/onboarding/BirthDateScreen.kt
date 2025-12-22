package com.example.calview.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calview.core.ui.components.CalAIButton
import com.example.calview.core.ui.components.StandardWheelPicker
import com.example.calview.feature.onboarding.components.OnboardingTemplate
import java.text.DateFormatSymbols

@Composable
fun BirthDateScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val months = DateFormatSymbols().months.toList().filter { it.isNotEmpty() }
    val days = (1..31).map { it.toString().padStart(2, '0') }
    val years = (1940..2024).map { it.toString() }

    OnboardingTemplate(
        title = "When were you born?",
        subtitle = "This will be taken into account when calculating your daily nutrition goals.",
        progress = 0.4f,
        onBack = { /* Handled by NavHost */ }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StandardWheelPicker(
                    items = months,
                    initialIndex = uiState.birthMonth - 1,
                    onItemSelected = { viewModel.onBirthDateChanged(it + 1, uiState.birthDay, uiState.birthYear) },
                    modifier = Modifier.weight(1.5f)
                )
                StandardWheelPicker(
                    items = days,
                    initialIndex = uiState.birthDay - 1,
                    onItemSelected = { viewModel.onBirthDateChanged(uiState.birthMonth, it + 1, uiState.birthYear) },
                    modifier = Modifier.weight(1f)
                )
                StandardWheelPicker(
                    items = years,
                    initialIndex = years.indexOf(uiState.birthYear.toString()),
                    onItemSelected = { viewModel.onBirthDateChanged(uiState.birthMonth, uiState.birthDay, years[it].toInt()) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            CalAIButton(
                text = "Continue",
                onClick = onNext
            )
        }
    }
}
