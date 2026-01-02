package com.example.calview.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import kotlin.math.roundToInt

/**
 * Consolidated Goal & Preferences Screen
 * Collects: Goal, Target Weight (conditional), Pace (conditional), Diet, Rollover setting
 */
@Composable
fun GoalPreferencesScreen(
    currentStep: Int,
    totalSteps: Int,
    // Goal
    selectedGoal: String,
    onGoalSelected: (String) -> Unit,
    // Weight (for target calculations)
    currentWeightKg: Float,
    isMetric: Boolean,
    // Target weight (conditional - only for Lose/Gain)
    targetWeightKg: Float,
    onTargetWeightChanged: (Float) -> Unit,
    // Pace (conditional - only for Lose/Gain)
    weightChangePerWeek: Float,
    onPaceChanged: (Float) -> Unit,
    // Diet
    selectedDiet: String,
    onDietSelected: (String) -> Unit,
    // Rollover calories
    rolloverEnabled: Boolean,
    onRolloverChanged: (Boolean) -> Unit,
    // Add burned calories
    addCaloriesBurnedEnabled: Boolean = false,
    onAddCaloriesBurnedChanged: (Boolean) -> Unit = {},
    // Navigation
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val scrollState = rememberScrollState()
    val showWeightSection = selectedGoal == "Lose Weight" || selectedGoal == "Gain Weight"
    val isGainWeight = selectedGoal == "Gain Weight"
    val isComplete = selectedGoal.isNotEmpty()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top bar with back button and progress
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF1C1C1E),
                trackColor = Color(0xFFE5E5E5)
            )
            
            Spacer(modifier = Modifier.width(40.dp))
        }
        
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            // Title
            Text(
                text = "Your goals",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.Black
            )
            
            Text(
                text = "Set your fitness goals and preferences",
                fontFamily = Inter,
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // ===================== GOAL SECTION =====================
            GoalSectionTitle(title = "What's your goal?", emoji = "🎯")
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GoalOption(
                    title = "Lose Weight",
                    emoji = "⬇️",
                    description = "Reduce body weight",
                    isSelected = selectedGoal == "Lose Weight",
                    onClick = { onGoalSelected("Lose Weight") }
                )
                GoalOption(
                    title = "Maintain",
                    emoji = "⚖️",
                    description = "Keep current weight",
                    isSelected = selectedGoal == "Maintain",
                    onClick = { onGoalSelected("Maintain") }
                )
                GoalOption(
                    title = "Gain Weight",
                    emoji = "⬆️",
                    description = "Build muscle mass",
                    isSelected = selectedGoal == "Gain Weight",
                    onClick = { onGoalSelected("Gain Weight") }
                )
            }
            
            // ===================== TARGET WEIGHT SECTION (Conditional) =====================
            AnimatedVisibility(
                visible = showWeightSection,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    GoalSectionTitle(
                        title = if (isGainWeight) "Target weight" else "Goal weight",
                        emoji = "🎯"
                    )
                    
                    // Weight display
                    val displayWeight = if (isMetric) {
                        "${targetWeightKg.roundToInt()} kg"
                    } else {
                        "${(targetWeightKg * 2.205f).roundToInt()} lb"
                    }
                    
                    val weightDiff = if (isGainWeight) {
                        targetWeightKg - currentWeightKg
                    } else {
                        currentWeightKg - targetWeightKg
                    }
                    
                    val diffText = if (isMetric) {
                        "${weightDiff.roundToInt()} kg ${if (isGainWeight) "to gain" else "to lose"}"
                    } else {
                        "${(weightDiff * 2.205f).roundToInt()} lb ${if (isGainWeight) "to gain" else "to lose"}"
                    }
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8F8F8)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = displayWeight,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                color = Color.Black
                            )
                            Text(
                                text = diffText,
                                fontFamily = Inter,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Weight slider
                            val minWeight = if (isGainWeight) currentWeightKg else 40f
                            val maxWeight = if (isGainWeight) currentWeightKg + 30f else currentWeightKg
                            
                            Slider(
                                value = targetWeightKg,
                                onValueChange = onTargetWeightChanged,
                                valueRange = minWeight..maxWeight,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF1C1C1E),
                                    activeTrackColor = Color(0xFF1C1C1E),
                                    inactiveTrackColor = Color(0xFFE0E0E0)
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // ===================== PACE SECTION =====================
                    GoalSectionTitle(
                        title = "Weekly ${if (isGainWeight) "gain" else "loss"} pace",
                        emoji = "⏱️"
                    )
                    
                    val paceDisplay = if (isMetric) {
                        "${weightChangePerWeek} kg/week"
                    } else {
                        "${(weightChangePerWeek * 2.205f).roundToInt() / 10f} lb/week"
                    }
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8F8F8)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = paceDisplay,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Color.Black
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = when {
                                    weightChangePerWeek <= 0.3f -> "Slow & steady"
                                    weightChangePerWeek <= 0.6f -> "Recommended"
                                    else -> "Aggressive"
                                },
                                fontFamily = Inter,
                                fontSize = 14.sp,
                                color = if (weightChangePerWeek <= 0.6f) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Slider(
                                value = weightChangePerWeek,
                                onValueChange = onPaceChanged,
                                valueRange = 0.2f..1.0f,
                                steps = 3,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF1C1C1E),
                                    activeTrackColor = Color(0xFF1C1C1E),
                                    inactiveTrackColor = Color(0xFFE0E0E0)
                                )
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // ===================== DIET SECTION =====================
            GoalSectionTitle(title = "Diet preference", emoji = "🥗")
            
            Text(
                text = "Optional - helps us customize meal suggestions",
                fontFamily = Inter,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            val diets = listOf(
                "No preference" to "🍽️",
                "Keto" to "🥑",
                "Vegetarian" to "🥬",
                "Vegan" to "🌱",
                "Paleo" to "🥩",
                "Mediterranean" to "🫒"
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                diets.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { (diet, emoji) ->
                            DietChip(
                                label = diet,
                                emoji = emoji,
                                isSelected = selectedDiet == diet,
                                onClick = { onDietSelected(diet) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill empty space if odd number
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // ===================== ROLLOVER SECTION =====================
            GoalSectionTitle(title = "Rollover calories", emoji = "🔄")
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRolloverChanged(!rolloverEnabled) },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8F8F8)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable rollover",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Unused calories carry over to the next day",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                    
                    Switch(
                        checked = rolloverEnabled,
                        onCheckedChange = onRolloverChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF1C1C1E),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // ===================== ADD BURNED CALORIES SECTION =====================
            GoalSectionTitle(title = "Add burned calories", emoji = "🔥")
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddCaloriesBurnedChanged(!addCaloriesBurnedEnabled) },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8F8F8)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add to daily goal",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Calories burned from exercise are added back to your daily allowance",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                    
                    Switch(
                        checked = addCaloriesBurnedEnabled,
                        onCheckedChange = onAddCaloriesBurnedChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF1C1C1E),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Continue button
        Button(
            onClick = onContinue,
            enabled = isComplete,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1C1C1E),
                disabledContainerColor = Color(0xFFE5E5E5)
            )
        ) {
            Text(
                text = "Continue",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun GoalSectionTitle(title: String, emoji: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun GoalOption(
    title: String,
    emoji: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, Color(0xFF1C1C1E), RoundedCornerShape(16.dp))
                else Modifier
            ),
        color = if (isSelected) Color(0xFFF0F0F0) else Color(0xFFF8F8F8),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 28.sp)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = description,
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF1C1C1E),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun DietChip(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                else Modifier
            ),
        color = if (isSelected) Color(0xFFF0F0F0) else Color(0xFFF8F8F8),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontFamily = Inter,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = Color.Black
            )
        }
    }
}
