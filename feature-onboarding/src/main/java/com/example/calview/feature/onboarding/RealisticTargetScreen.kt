package com.example.calview.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import com.example.calview.feature.onboarding.components.OnboardingScreenLayout

/**
 * Realistic target screen showing calculated weight change goal.
 * Displays: "Losing/Gaining X.X kg is a realistic target. it's not hard at all!"
 * Weight is highlighted in orange.
 */
@Composable
fun RealisticTargetScreen(
    currentStep: Int,
    totalSteps: Int,
    currentWeightKg: Float,
    desiredWeightKg: Float,
    isKg: Boolean = true,
    isGainWeight: Boolean = false, // New parameter for Gain/Lose mode
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    // Calculate weight difference (absolute value)
    val weightDifference = if (isGainWeight) {
        desiredWeightKg - currentWeightKg // Gain: desired > current
    } else {
        currentWeightKg - desiredWeightKg // Lose: current > desired
    }
    val displayWeight = if (isKg) weightDifference else weightDifference * 2.205f
    val unit = if (isKg) "kg" else "lbs"
    val actionWord = if (isGainWeight) "Gaining" else "Losing"
    
    // Orange color for weight highlight
    val orangeColor = Color(0xFFE8A87C)
    
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "",
        subtitle = null,
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = true
    ) {
        Spacer(modifier = Modifier.weight(0.4f))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main message with orange highlighted weight
            Text(
                text = buildAnnotatedString {
                    append("$actionWord ")
                    withStyle(style = SpanStyle(color = orangeColor, fontWeight = FontWeight.Bold)) {
                        append(String.format("%.1f %s", displayWeight.coerceAtLeast(0f), unit))
                    }
                    append(" is a realistic target. it's not hard at all!")
                },
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 42.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Subtitle
            Text(
                text = "90% of users say that the change is obvious after using CalViewAI and it is not easy to rebound.",
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
        
        Spacer(modifier = Modifier.weight(0.5f))
    }
}
