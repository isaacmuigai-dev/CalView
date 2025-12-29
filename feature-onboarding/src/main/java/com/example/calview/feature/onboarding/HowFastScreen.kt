package com.example.calview.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import com.example.calview.feature.onboarding.components.OnboardingScreenLayout

/**
 * How fast do you want to reach your goal screen.
 * Slider with sloth, rabbit, and cheetah icons.
 * Shows "Slow and Steady" / "Recommended" / warning labels.
 * Works for both Lose Weight and Gain Weight goals.
 */
@Composable
fun HowFastScreen(
    currentStep: Int,
    totalSteps: Int,
    isKg: Boolean = true,
    weightChangePerWeek: Float = 0.8f, // in kg
    isGainWeight: Boolean = false, // New parameter for Gain/Lose mode
    onWeightChangeChanged: (Float) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val minChange = 0.1f
    val maxChange = 1.5f
    
    val displayWeight = if (isKg) weightChangePerWeek else weightChangePerWeek * 2.205f
    val unit = if (isKg) "kg" else "lbs"
    val speedLabel = if (isGainWeight) "Gain weight speed per week" else "Loose weight speed per week"
    
    // Determine position on slider for icon highlighting
    val sliderPosition = (weightChangePerWeek - minChange) / (maxChange - minChange)
    val isSlothActive = sliderPosition < 0.35f
    val isRabbitActive = sliderPosition >= 0.35f && sliderPosition < 0.7f
    val isCheetahActive = sliderPosition >= 0.7f
    
    // Status message based on speed
    val statusMessage = when {
        weightChangePerWeek <= 0.3f -> "Slow and Steady"
        weightChangePerWeek <= 1.0f -> "Recommended"
        else -> "You may feel very tired and develop loose skin"
    }
    
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "How fast do you want to reach your goal?",
        subtitle = null,
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = true
    ) {
        Spacer(modifier = Modifier.weight(0.25f))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Label (dynamic)
            Text(
                text = speedLabel,
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weight display
            Text(
                text = String.format("%.1f %s", displayWeight, unit),
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Animal icons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sloth (slow) - orange when active
                Text(
                    text = "🦥",
                    fontSize = 40.sp,
                    modifier = Modifier.alpha(if (isSlothActive) 1f else 0.3f)
                )
                
                // Rabbit (medium) - orange when active
                Text(
                    text = "🐇",
                    fontSize = 40.sp,
                    modifier = Modifier.alpha(if (isRabbitActive) 1f else 0.3f)
                )
                
                // Cheetah/Dog (fast) - dark/gray
                Text(
                    text = "🐕‍🦺",
                    fontSize = 40.sp,
                    modifier = Modifier.alpha(if (isCheetahActive) 1f else 0.3f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Slider
            Slider(
                value = weightChangePerWeek,
                onValueChange = { onWeightChangeChanged(it) },
                valueRange = minChange..maxChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF1C1C1E),
                    activeTrackColor = Color(0xFF1C1C1E),
                    inactiveTrackColor = Color(0xFFE8E8FF)
                )
            )
            
            // Scale labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isKg) "0.1 kg" else "0.2 lbs",
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = if (isKg) "0.8 kg" else "1.8 lbs",
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = if (isKg) "1.5 kg" else "3.3 lbs",
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Status message pill
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF5F5F5),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = statusMessage,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(0.4f))
    }
}
