package com.example.calview.feature.dashboard

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import kotlin.math.roundToInt

/**
 * Data class holding all collected data for goal generation.
 */
data class GoalGenerationData(
    val workoutsPerWeek: String = "",
    val heightCm: Int = 170,
    val weightKg: Float = 70f,
    val goal: String = "", // "Lose Weight", "Maintain", "Gain Weight"
    val desiredWeightKg: Float = 70f,
    val weightChangePerWeek: Float = 0.5f,
    val gender: String = "Male",
    val age: Int = 25
)

/**
 * Calculated nutrition goals.
 */
data class NutritionGoals(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val fiber: Int = 38,
    val sugar: Int = 65,
    val sodium: Int = 2300
)

/**
 * Calculate nutrition goals based on collected data.
 */
fun calculateNutritionGoals(data: GoalGenerationData): NutritionGoals {
    // Calculate BMR using Mifflin-St Jeor equation
    val bmr = if (data.gender == "Female") {
        10 * data.weightKg + 6.25 * data.heightCm - 5 * data.age - 161
    } else {
        10 * data.weightKg + 6.25 * data.heightCm - 5 * data.age + 5
    }
    
    // Activity multiplier based on workout frequency
    val activityMultiplier = when (data.workoutsPerWeek) {
        "0-2" -> 1.2
        "3-5" -> 1.55
        "6+" -> 1.725
        else -> 1.4
    }
    
    // Calculate TDEE
    val tdee = bmr * activityMultiplier
    
    // Calculate calorie goal based on weight change goal
    // ~1100 calories deficit/surplus per kg weight change per week
    val calorieAdjustment = (data.weightChangePerWeek * 1100).toInt()
    
    val calorieGoal = when (data.goal) {
        "Lose Weight" -> (tdee - calorieAdjustment).toInt().coerceAtLeast(1200)
        "Gain Weight" -> (tdee + calorieAdjustment).toInt()
        else -> tdee.toInt() // Maintain
    }
    
    // Macro distribution: Protein 30%, Carbs 40%, Fat 30%
    // Protein: 4 cal/g, Carbs: 4 cal/g, Fat: 9 cal/g
    val proteinCalories = (calorieGoal * 0.30).toInt()
    val carbsCalories = (calorieGoal * 0.40).toInt()
    val fatsCalories = (calorieGoal * 0.30).toInt()
    
    return NutritionGoals(
        calories = calorieGoal,
        protein = proteinCalories / 4, // g
        carbs = carbsCalories / 4,     // g
        fats = fatsCalories / 9        // g
    )
}

/**
 * Consolidated Auto Generate Goals screen.
 * Single scrollable screen that collects all information needed to calculate nutrition goals.
 */
@Composable
fun AutoGenerateGoalsNavHost(
    initialGender: String = "Male",
    initialAge: Int = 25,
    initialHeightCm: Int = 170,
    initialWeightKg: Float = 70f,
    onBack: () -> Unit,
    onComplete: (NutritionGoals) -> Unit
) {
    AutoGenerateGoalsScreen(
        initialGender = initialGender,
        initialAge = initialAge,
        initialHeightCm = initialHeightCm,
        initialWeightKg = initialWeightKg,
        onBack = onBack,
        onComplete = onComplete
    )
}

/**
 * Single consolidated screen for auto-generating goals.
 */
@Composable
fun AutoGenerateGoalsScreen(
    initialGender: String = "Male",
    initialAge: Int = 25,
    initialHeightCm: Int = 170,
    initialWeightKg: Float = 70f,
    onBack: () -> Unit,
    onComplete: (NutritionGoals) -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Form state
    var isMetric by remember { mutableStateOf(true) }
    var heightCm by remember { mutableIntStateOf(initialHeightCm) }
    var heightFeet by remember { mutableIntStateOf((initialHeightCm / 30.48).toInt()) }
    var heightInches by remember { mutableIntStateOf(((initialHeightCm / 2.54) % 12).toInt()) }
    var weightKg by remember { mutableIntStateOf(initialWeightKg.toInt()) }
    var weightLb by remember { mutableIntStateOf((initialWeightKg * 2.205).toInt()) }
    var selectedWorkouts by remember { mutableStateOf("") }
    var selectedGoal by remember { mutableStateOf("") }
    var targetWeightKg by remember { mutableFloatStateOf(initialWeightKg) }
    var weightChangePerWeek by remember { mutableFloatStateOf(0.5f) }
    
    val isComplete = selectedWorkouts.isNotEmpty() && selectedGoal.isNotEmpty()
    val showWeightSection = selectedGoal == "Lose Weight" || selectedGoal == "Gain Weight"
    val isGainWeight = selectedGoal == "Gain Weight"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Top bar
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
            
            Text(
                text = "Auto Generate Goals",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )
        }
        
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Fill in your information to calculate personalized nutrition goals",
                fontFamily = Inter,
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // ===================== HEIGHT & WEIGHT SECTION =====================
            AutoGenSectionTitle(title = "Height & Weight", emoji = "📏")
            
            // Metric/Imperial toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AutoGenUnitToggle(
                    label = "Imperial",
                    isSelected = !isMetric,
                    onClick = { isMetric = false }
                )
                Spacer(modifier = Modifier.width(12.dp))
                AutoGenUnitToggle(
                    label = "Metric",
                    isSelected = isMetric,
                    onClick = { isMetric = true }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isMetric) {
                // Metric inputs - Height (cm) and Weight (kg)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AutoGenNumberInput(
                        label = "Height",
                        value = heightCm,
                        suffix = "cm",
                        onValueChange = { heightCm = it },
                        modifier = Modifier.weight(1f)
                    )
                    AutoGenNumberInput(
                        label = "Weight",
                        value = weightKg,
                        suffix = "kg",
                        onValueChange = { weightKg = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Imperial inputs - Height (ft/in) and Weight (lb)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    AutoGenNumberInput(
                        label = "Height",
                        value = heightFeet,
                        suffix = "ft",
                        onValueChange = { 
                            heightFeet = it
                            heightCm = ((heightFeet * 12 + heightInches) * 2.54).toInt()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AutoGenNumberInput(
                        label = " ",
                        value = heightInches,
                        suffix = "in",
                        onValueChange = { 
                            heightInches = it.coerceIn(0, 11)
                            heightCm = ((heightFeet * 12 + heightInches) * 2.54).toInt()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AutoGenNumberInput(
                        label = "Weight",
                        value = weightLb,
                        suffix = "lb",
                        onValueChange = { 
                            weightLb = it
                            weightKg = (it / 2.205).toInt()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // ===================== ACTIVITY LEVEL SECTION =====================
            AutoGenSectionTitle(title = "Activity Level", emoji = "🏃")
            
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AutoGenActivityOption(
                    label = "0-2 workouts/week",
                    description = "Sedentary",
                    isSelected = selectedWorkouts == "0-2",
                    onClick = { selectedWorkouts = "0-2" }
                )
                AutoGenActivityOption(
                    label = "3-5 workouts/week",
                    description = "Moderately active",
                    isSelected = selectedWorkouts == "3-5",
                    onClick = { selectedWorkouts = "3-5" }
                )
                AutoGenActivityOption(
                    label = "6+ workouts/week",
                    description = "Very active",
                    isSelected = selectedWorkouts == "6+",
                    onClick = { selectedWorkouts = "6+" }
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // ===================== GOAL SECTION =====================
            AutoGenSectionTitle(title = "What's your goal?", emoji = "🎯")
            
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AutoGenGoalOption(
                    title = "Lose Weight",
                    emoji = "⬇️",
                    isSelected = selectedGoal == "Lose Weight",
                    onClick = { selectedGoal = "Lose Weight" }
                )
                AutoGenGoalOption(
                    title = "Maintain",
                    emoji = "⚖️",
                    isSelected = selectedGoal == "Maintain",
                    onClick = { selectedGoal = "Maintain" }
                )
                AutoGenGoalOption(
                    title = "Gain Weight",
                    emoji = "⬆️",
                    isSelected = selectedGoal == "Gain Weight",
                    onClick = { selectedGoal = "Gain Weight" }
                )
            }
            
            // ===================== TARGET WEIGHT (Conditional) =====================
            AnimatedVisibility(
                visible = showWeightSection,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    AutoGenSectionTitle(
                        title = if (isGainWeight) "Target Weight" else "Goal Weight",
                        emoji = "🎯"
                    )
                    
                    val displayWeight = if (isMetric) {
                        "${targetWeightKg.roundToInt()} kg"
                    } else {
                        "${(targetWeightKg * 2.205f).roundToInt()} lb"
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
                                fontSize = 32.sp,
                                color = Color.Black
                            )
                            
                            val minWeight = if (isGainWeight) weightKg.toFloat() else 40f
                            val maxWeight = if (isGainWeight) weightKg + 30f else weightKg.toFloat()
                            
                            Slider(
                                value = targetWeightKg.coerceIn(minWeight, maxWeight),
                                onValueChange = { targetWeightKg = it },
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
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Pace slider
                    AutoGenSectionTitle(title = "Weekly Pace", emoji = "⏱️")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8F8F8)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val paceDisplay = if (isMetric) {
                                "${String.format("%.1f", weightChangePerWeek)} kg/week"
                            } else {
                                "${String.format("%.1f", weightChangePerWeek * 2.205f)} lb/week"
                            }
                            
                            Text(
                                text = paceDisplay,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.Black
                            )
                            
                            Slider(
                                value = weightChangePerWeek,
                                onValueChange = { weightChangePerWeek = it },
                                valueRange = 0.2f..1.0f,
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
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Generate button
        Button(
            onClick = {
                val data = GoalGenerationData(
                    workoutsPerWeek = selectedWorkouts,
                    heightCm = heightCm,
                    weightKg = weightKg.toFloat(),
                    goal = selectedGoal,
                    desiredWeightKg = targetWeightKg,
                    weightChangePerWeek = if (showWeightSection) weightChangePerWeek else 0f,
                    gender = initialGender,
                    age = initialAge
                )
                val goals = calculateNutritionGoals(data)
                onComplete(goals)
            },
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
                text = "Auto Generate Goals",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun AutoGenSectionTitle(title: String, emoji: String) {
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
private fun AutoGenUnitToggle(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFF1C1C1E) else Color(0xFFF5F5F5),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            fontFamily = Inter,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else Color.Black,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun AutoGenNumberInput(
    label: String,
    value: Int,
    suffix: String,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontFamily = Inter,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { newValue ->
                newValue.toIntOrNull()?.let { onValueChange(it) }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            suffix = { Text(suffix, color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1C1C1E),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            ),
            singleLine = true
        )
    }
}

@Composable
private fun AutoGenActivityOption(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
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
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
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
private fun AutoGenGoalOption(
    title: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
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
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
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
