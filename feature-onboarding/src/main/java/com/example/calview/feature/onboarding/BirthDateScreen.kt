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
    
    BirthDateContent(
        birthMonth = uiState.birthMonth,
        birthDay = uiState.birthDay,
        birthYear = uiState.birthYear,
        onDateChanged = { m, d, y -> viewModel.onBirthDateChanged(m, d, y) },
        onNext = onNext
    )
}

@Composable
fun BirthDateContent(
    birthMonth: Int,
    birthDay: Int,
    birthYear: Int,
    onDateChanged: (Int, Int, Int) -> Unit,
    onNext: () -> Unit
) {
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
                    initialIndex = birthMonth - 1,
                    onItemSelected = { onDateChanged(it + 1, birthDay, birthYear) },
                    modifier = Modifier.weight(1.5f)
                )
                StandardWheelPicker(
                    items = days,
                    initialIndex = birthDay - 1,
                    onItemSelected = { onDateChanged(birthMonth, it + 1, birthYear) },
                    modifier = Modifier.weight(1f)
                )
                StandardWheelPicker(
                    items = years,
                    initialIndex = years.indexOf(birthYear.toString()).coerceAtLeast(0),
                    onItemSelected = { onDateChanged(birthMonth, birthDay, years[it].toInt()) },
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

import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun BirthDateScreenPreview() {
    BirthDateContent(
        birthMonth = 2,
        birthDay = 2,
        birthYear = 1998,
        onDateChanged = { _, _, _ -> },
        onNext = {}
    )
}
