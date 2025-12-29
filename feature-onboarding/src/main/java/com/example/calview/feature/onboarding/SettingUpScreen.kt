package com.example.calview.feature.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import kotlinx.coroutines.delay

/**
 * We're setting everything up for you screen.
 * Shows animated progress percentage, gradient progress bar, 
 * and checklist of items being calculated.
 */
@Composable
fun SettingUpScreen(
    onComplete: () -> Unit
) {
    // Animated progress
    var progress by remember { mutableFloatStateOf(0f) }
    var currentStep by remember { mutableIntStateOf(0) }
    
    val steps = listOf(
        "Calories",
        "Carbs", 
        "Protein",
        "Fats",
        "Health score"
    )
    
    val statusMessages = listOf(
        "Analyzing your data...",
        "Calculating BMR formula...",
        "Applying BMR formula...",
        "Adjusting for activity level...",
        "Finalizing your plan..."
    )
    
    // Animate progress
    LaunchedEffect(Unit) {
        val totalDuration = 3000L // 3 seconds total
        val stepDuration = totalDuration / steps.size
        
        for (i in steps.indices) {
            val targetProgress = (i + 1).toFloat() / steps.size
            val startProgress = progress
            val progressIncrement = (targetProgress - startProgress) / 20
            
            // Animate within this step
            repeat(20) {
                delay(stepDuration / 20)
                progress += progressIncrement
            }
            currentStep = i + 1
        }
        
        delay(500)
        onComplete()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.15f))
        
        // Percentage display
        Text(
            text = "${(progress * 100).toInt()}%",
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 64.sp,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Title
        Text(
            text = "We're setting everything\nup for you",
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Gradient progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE5E5E5))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFE57373), // Red
                                Color(0xFF9575CD), // Purple
                                Color(0xFF64B5F6)  // Blue
                            )
                        )
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status message
        Text(
            text = statusMessages.getOrElse(currentStep.coerceIn(0, statusMessages.size - 1)) { statusMessages.last() },
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Checklist card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF8F8F8),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Daily recommendation for",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "• $step",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Checkmark if completed
                        if (index < currentStep) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1C1C1E),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Complete",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(0.3f))
    }
}
