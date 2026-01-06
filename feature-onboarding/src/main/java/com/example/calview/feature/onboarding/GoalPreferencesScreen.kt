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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
            .background(MaterialTheme.colorScheme.background)
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
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
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Set your fitness goals and preferences",
                fontFamily = Inter,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    // Display weight only in kg
                    val displayWeight = "${targetWeightKg.roundToInt()} kg"
                    
                    val weightDiff = if (isGainWeight) {
                        targetWeightKg - currentWeightKg
                    } else {
                        currentWeightKg - targetWeightKg
                    }
                    
                    val diffText = "${weightDiff.roundToInt()} kg ${if (isGainWeight) "to gain" else "to lose"}"
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = diffText,
                                fontFamily = Inter,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // ===================== PACE SECTION =====================
                    GoalSectionTitle(
                        title = "How fast do you want to reach your goal?",
                        emoji = "⏱️"
                    )
                    
                    // Current vs Target weight display
                    Text(
                        text = "${currentWeightKg.roundToInt()} kg → $displayWeight",
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Current value display - ensure exactly one decimal place
                    // Use integer math to avoid floating-point precision issues
                    val paceInt = (weightChangePerWeek * 10 + 0.5f).toInt().coerceIn(1, 15)
                    val roundedPace = paceInt / 10f
                    val paceDisplay = "${paceInt / 10}.${paceInt % 10} kg"
                    
                    Text(
                        text = paceDisplay,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .semantics { contentDescription = "Weekly pace: $paceDisplay per week" }
                    )
                    
                    // Animal emoji row (matches auto-generate design)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sloth - slow (0.1 - 0.5 kg)
                        Text(
                            text = "🦥",
                            fontSize = if (roundedPace <= 0.5f) 32.sp else 24.sp,
                            modifier = Modifier.alpha(if (roundedPace <= 0.5f) 1f else 0.5f)
                        )
                        
                        // Hamster - medium pace (0.6 - 1.0 kg)
                        Text(
                            text = "🐹",
                            fontSize = if (roundedPace in 0.6f..1.0f) 32.sp else 24.sp,
                            modifier = Modifier.alpha(if (roundedPace in 0.6f..1.0f) 1f else 0.5f)
                        )
                        
                        // Rabbit - fast pace (1.1 - 1.5 kg)
                        Text(
                            text = "🐇",
                            fontSize = if (roundedPace >= 1.1f) 32.sp else 24.sp,
                            modifier = Modifier.alpha(if (roundedPace >= 1.1f) 1f else 0.5f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Slider with 0.1 kg increments (0.1, 0.2, 0.3... 1.5)
                    Slider(
                        value = weightChangePerWeek,
                        onValueChange = { newValue ->
                            // Round to nearest 0.1 using proper rounding
                            val rounded = ((newValue * 10 + 0.5f).toInt().coerceIn(1, 15)) / 10f
                            onPaceChanged(rounded)
                        },
                        valueRange = 0.1f..1.5f,
                        steps = 13, // 14 positions: 0.1, 0.2, 0.3... 1.5 (steps = positions - 2)
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Weight change pace slider, from 0.1 to 1.5 kg per week" },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.onBackground,
                            activeTrackColor = MaterialTheme.colorScheme.onBackground,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    
                    // Scale labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "0.1 kg",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "0.8 kg",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "1.5 kg",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Dynamic description label
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = when {
                                weightChangePerWeek <= 0.3f -> "Slow and Steady"
                                weightChangePerWeek <= 0.5f -> "Moderate pace"
                                weightChangePerWeek <= 1.0f -> "Recommended"
                                weightChangePerWeek <= 1.3f -> "Fast pace"
                                else -> "You may feel very tired and develop loose skin"
                            },
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = when {
                                weightChangePerWeek <= 1.0f -> MaterialTheme.colorScheme.onSurface
                                else -> Color(0xFFFF5722) // Warning color
                            },
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        )
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
                color = MaterialTheme.colorScheme.surfaceVariant
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Unused calories carry over to the next day",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = rolloverEnabled,
                        onCheckedChange = onRolloverChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
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
                color = MaterialTheme.colorScheme.surfaceVariant
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Calories burned from exercise are added back to your daily allowance",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = addCaloriesBurnedEnabled,
                        onCheckedChange = onAddCaloriesBurnedChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
            color = MaterialTheme.colorScheme.onBackground
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
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                else Modifier
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
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
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                else Modifier
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
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
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
