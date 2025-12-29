package com.example.calview.feature.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import com.example.calview.feature.onboarding.components.OnboardingScreenLayout

/**
 * Desired weight input screen with lbs/kg toggle and horizontal ruler scale picker.
 * Shows "Lose Weight" or "Gain Weight" label based on goal.
 */
@Composable
fun DesiredWeightScreen(
    currentStep: Int,
    totalSteps: Int,
    currentWeightKg: Float,
    isKg: Boolean = true,
    desiredWeightKg: Float,
    isGainWeight: Boolean = false, // New parameter to toggle Gain/Lose mode
    onUnitToggle: (Boolean) -> Unit,
    onWeightChanged: (Float) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val displayWeight = if (isKg) desiredWeightKg else desiredWeightKg * 2.205f
    val unit = if (isKg) "kg" else "lbs"
    val goalLabel = if (isGainWeight) "Gain Weight" else "Lose Weight"
    
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "What is your desired weight?",
        subtitle = null,
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = true
    ) {
        Spacer(modifier = Modifier.weight(0.2f))
        
        // Unit toggle (lbs / kg)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF5F5F5),
                modifier = Modifier.height(44.dp)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UnitButton(
                        text = "lbs",
                        isSelected = !isKg,
                        onClick = { onUnitToggle(false) }
                    )
                    UnitButton(
                        text = "Kg",
                        isSelected = isKg,
                        onClick = { onUnitToggle(true) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Goal label (dynamic)
        Text(
            text = goalLabel,
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Weight display
        Text(
            text = String.format("%.1f %s", displayWeight, unit),
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            color = Color.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Ruler scale picker
        RulerScalePicker(
            value = displayWeight,
            minValue = if (isKg) 40f else 88f,
            maxValue = if (isKg) 150f else 330f,
            isKg = isKg,
            onValueChange = { newValue ->
                val kgValue = if (isKg) newValue else newValue / 2.205f
                onWeightChanged(kgValue)
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.weight(0.5f))
    }
}

@Composable
private fun UnitButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = Modifier
            .height(36.dp)
            .widthIn(min = 56.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = text,
                fontFamily = Inter,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                fontSize = 14.sp,
                color = if (isSelected) Color.Black else Color.Gray
            )
        }
    }
}

@Composable
private fun RulerScalePicker(
    value: Float,
    minValue: Float,
    maxValue: Float,
    isKg: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Center indicator triangle
        Canvas(
            modifier = Modifier
                .width(16.dp)
                .height(12.dp)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path, Color(0xFF1C1C1E), style = Fill)
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Ruler with tick marks
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val sensitivity = 0.1f
                        val delta = -dragAmount * sensitivity / density.density
                        val newValue = (value + delta).coerceIn(minValue, maxValue)
                        onValueChange(newValue)
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F8F8))
            ) {
                val tickSpacing = size.width / 40f
                val centerX = size.width / 2
                val totalRange = maxValue - minValue
                val pixelsPerUnit = size.width / 20f
                
                // Calculate offset based on current value
                val valueOffset = ((value - minValue) / totalRange) * size.width - centerX
                
                // Draw tick marks
                for (i in -40..80) {
                    val tickValue = minValue + (i * totalRange / 40f)
                    val x = centerX + (i * tickSpacing) - (valueOffset * tickSpacing / pixelsPerUnit)
                    
                    if (x in 0f..size.width) {
                        val isMajor = (tickValue.toInt() % 5 == 0)
                        val tickHeight = if (isMajor) 30f else 15f
                        val tickColor = if (x in (centerX - 100)..(centerX + 100)) 
                            Color(0xFF1C1C1E) else Color(0xFFCCCCCC)
                        
                        drawLine(
                            color = tickColor,
                            start = Offset(x, size.height - tickHeight),
                            end = Offset(x, size.height),
                            strokeWidth = if (isMajor) 2f else 1f
                        )
                    }
                }
                
                // Center line (selected value indicator)
                drawLine(
                    color = Color(0xFF1C1C1E),
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, size.height),
                    strokeWidth = 3f
                )
            }
        }
    }
}
