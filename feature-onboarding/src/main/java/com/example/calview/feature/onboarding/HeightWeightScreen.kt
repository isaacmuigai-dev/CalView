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
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HeightWeightScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    HeightWeightContent(
        isMetric = uiState.isMetric,
        heightFt = uiState.heightFt,
        heightIn = uiState.heightIn,
        weight = uiState.weight,
        onUnitToggle = { viewModel.onUnitToggle(it) },
        onHeightChanged = { ft, inches -> viewModel.onHeightChanged(ft, inches) },
        onWeightChanged = { viewModel.onWeightChanged(it) },
        onNext = onNext
    )
}

@Composable
fun HeightWeightContent(
    isMetric: Boolean,
    heightFt: Int,
    heightIn: Int,
    weight: Float,
    onUnitToggle: (Boolean) -> Unit,
    onHeightChanged: (Int, Int) -> Unit,
    onWeightChanged: (Float) -> Unit,
    onNext: () -> Unit
) {
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
                isMetric = isMetric,
                onToggle = onUnitToggle,
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
                            initialIndex = (heightFt - 2).coerceAtLeast(0),
                            onItemSelected = { onHeightChanged(it + 2, heightIn) },
                            modifier = Modifier.width(80.dp)
                        )
                        StandardWheelPicker(
                            items = inItems,
                            initialIndex = heightIn.coerceIn(0, 11),
                            onItemSelected = { onHeightChanged(heightFt, it) },
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
                        initialIndex = weightItems.indexOf("${weight.toInt()} lb").coerceAtLeast(0),
                        onItemSelected = { onWeightChanged(it.toFloat() + 50) },
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

// imports moved to top

@Preview(showBackground = true)
@Composable
fun HeightWeightScreenPreview() {
    HeightWeightContent(
        isMetric = false,
        heightFt = 5,
        heightIn = 6,
        weight = 150f,
        onUnitToggle = {},
        onHeightChanged = { _, _ -> },
        onWeightChanged = {},
        onNext = {}
    )
}
