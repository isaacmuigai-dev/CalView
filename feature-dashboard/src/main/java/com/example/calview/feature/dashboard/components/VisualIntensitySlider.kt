package com.example.calview.feature.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.util.HapticsManager
import com.example.calview.core.ui.util.rememberHapticsManager

// Intensity color palette
private val IntensityCoolBlue = Color(0xFF4A90D9)
private val IntensityGreen = Color(0xFF4CAF50)
private val IntensityOrange = Color(0xFFFF9800)
private val IntensityRed = Color(0xFFE53935)
private val IntensityDeepRed = Color(0xFFB71C1C)

/**
 * A visual intensity slider that changes color based on the intensity level.
 * Blue (low) -> Green (moderate) -> Orange (high) -> Red (intense)
 * 
 * @param value Current intensity value (0.0 to 1.0)
 * @param onValueChange Callback when value changes
 * @param modifier Modifier for the component
 * @param showLabels Whether to show intensity labels
 * @param showCalories Whether to show calorie count
 * @param estimatedCalories Estimated calories (if showCalories is true)
 */
@Composable
fun VisualIntensitySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    showCalories: Boolean = false,
    estimatedCalories: Int = 0
) {
    val haptics = rememberHapticsManager()
    
    // Animate color based on intensity
    val intensityColor by animateColorAsState(
        targetValue = getIntensityColor(value),
        animationSpec = tween(300),
        label = "intensityColor"
    )
    
    // Get intensity label
    val intensityLabel = getIntensityLabel(value)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Exercise intensity control. Current level: $intensityLabel, ${(value * 100).toInt()} percent"
            }
    ) {
        // Header with intensity label and fire icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = intensityColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "Intensity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Intensity level badge
            Surface(
                color = intensityColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = intensityLabel,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                        },
                    color = intensityColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Slider with animated colors
        Slider(
            value = value,
            onValueChange = { newValue ->
                // Provide haptic feedback when crossing intensity thresholds
                val oldThreshold = (value * 4).toInt()
                val newThreshold = (newValue * 4).toInt()
                if (oldThreshold != newThreshold) {
                    haptics.tick()
                }
                onValueChange(newValue)
            },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = intensityColor,
                activeTrackColor = intensityColor,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Intensity slider. Drag to adjust exercise intensity from light to maximum"
                    stateDescription = "$intensityLabel, ${(value * 100).toInt()} percent"
                }
        )
        
        // Gradient preview bar (decorative, excluded from accessibility)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            IntensityCoolBlue,
                            IntensityGreen,
                            IntensityOrange,
                            IntensityRed
                        )
                    )
                )
                .semantics {
                    contentDescription = "Intensity color gradient from light blue to red"
                }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Labels row
        if (showLabels) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Light",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Moderate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Intense",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Calorie display
        if (showCalories && estimatedCalories > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                color = intensityColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = "Estimated calories burned: $estimatedCalories calories"
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$estimatedCalories",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = intensityColor
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "cal burned",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Get the color for a given intensity value.
 */
private fun getIntensityColor(intensity: Float): Color {
    return when {
        intensity < 0.25f -> IntensityCoolBlue
        intensity < 0.5f -> IntensityGreen
        intensity < 0.75f -> IntensityOrange
        else -> IntensityRed
    }
}

/**
 * Get a label for the intensity level.
 */
private fun getIntensityLabel(intensity: Float): String {
    return when {
        intensity < 0.2f -> "Very Light"
        intensity < 0.4f -> "Light"
        intensity < 0.6f -> "Moderate"
        intensity < 0.8f -> "Hard"
        else -> "Maximum"
    }
}

/**
 * Compact version of the intensity slider for inline use.
 */
@Composable
fun CompactIntensitySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberHapticsManager()
    val intensityLabel = getIntensityLabel(value)
    
    val intensityColor by animateColorAsState(
        targetValue = getIntensityColor(value),
        animationSpec = tween(300),
        label = "intensityColor"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Compact intensity slider. Current: $intensityLabel, ${(value * 100).toInt()} percent"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = null,
            tint = intensityColor,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.padding(4.dp))
        
        Slider(
            value = value,
            onValueChange = { newValue ->
                val oldThreshold = (value * 4).toInt()
                val newThreshold = (newValue * 4).toInt()
                if (oldThreshold != newThreshold) {
                    haptics.tick()
                }
                onValueChange(newValue)
            },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = intensityColor,
                activeTrackColor = intensityColor,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .weight(1f)
                .semantics {
                    stateDescription = "$intensityLabel, ${(value * 100).toInt()} percent"
                }
        )
        
        Spacer(modifier = Modifier.padding(4.dp))
        
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = intensityColor,
            modifier = Modifier.semantics {
                liveRegion = LiveRegionMode.Polite
            }
        )
    }
}
