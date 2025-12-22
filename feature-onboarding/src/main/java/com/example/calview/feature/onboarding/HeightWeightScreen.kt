package com.example.calview.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.CalAIButton
import com.example.calview.core.ui.components.StandardWheelPicker
import com.example.calview.core.ui.components.UnitToggle
import com.example.calview.core.ui.theme.Inter
import com.example.calview.feature.onboarding.components.OnboardingTemplate

@Composable
fun HeightWeightScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val ftItems = (2..8).map { "$it ft" }
    val inItems = (0..11).map { "$it in" }
    val weightItems = (50..400).map { "$it lb" }

    OnboardingTemplate(
        title = "Height & Weight",
        subtitle = "This will be taken into account when calculating your daily nutrition goals.",
        progress = 0.5f,
        onBack = { /* Handled by NavHost */ }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UnitToggle(
                isMetric = uiState.isMetric,
                onToggle = { viewModel.onUnitToggle(it) },
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Height",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row {
                        StandardWheelPicker(
                            items = ftItems,
                            initialIndex = uiState.heightFt - 2,
                            onItemSelected = { viewModel.onHeightChanged(it + 2, uiState.heightIn) },
                            modifier = Modifier.width(80.dp)
                        )
                        StandardWheelPicker(
                            items = inItems,
                            initialIndex = uiState.heightIn,
                            onItemSelected = { viewModel.onHeightChanged(uiState.heightFt, it) },
                            modifier = Modifier.width(80.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Weight",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    StandardWheelPicker(
                        items = weightItems,
                        initialIndex = weightItems.indexOf("${uiState.weight.toInt()} lb"),
                        onItemSelected = { viewModel.onWeightChanged(it.toFloat() + 50) },
                        modifier = Modifier.width(100.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            CalAIButton(
                text = "Continue",
                onClick = onNext
            )
        }
    }
}
