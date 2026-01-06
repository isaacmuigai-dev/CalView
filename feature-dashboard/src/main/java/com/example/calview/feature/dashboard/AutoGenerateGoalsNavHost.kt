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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
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
    // Metric-only app (kg/cm)
    var heightCm by remember { mutableIntStateOf(initialHeightCm) }
    var weightKg by remember { mutableIntStateOf(initialWeightKg.toInt()) }
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
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Auto Generate Goals",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // ===================== HEIGHT & WEIGHT SECTION =====================
            AutoGenSectionTitle(title = "Height & Weight", emoji = "📏")
            
            // Metric inputs - Dropdown selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AutoGenDropdownSelector(
                    label = "Height",
                    value = "$heightCm cm",
                    options = (100..220).map { "$it cm" },
                    onValueSelected = { 
                        heightCm = it.replace(" cm", "").toInt()
                        // recalculateGoals() // Assuming recalculateGoals() is defined elsewhere or will be added
                    },
                    modifier = Modifier.weight(1f)
                )
                AutoGenDropdownSelector(
                    label = "Weight",
                    value = "$weightKg kg",
                    options = (30..200).map { "$it kg" },
                    onValueSelected = { 
                        weightKg = it.replace(" kg", "").toInt()
                        // recalculateGoals() // Assuming recalculateGoals() is defined elsewhere or will be added
                    },
                    modifier = Modifier.weight(1f)
                )
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
                    
                    val displayWeight = "${targetWeightKg.roundToInt()} kg"
                    
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
                                fontSize = 32.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            val minWeight = if (isGainWeight) weightKg.toFloat() else 40f
                            val maxWeight = if (isGainWeight) weightKg + 30f else weightKg.toFloat()
                            
                            Slider(
                                value = targetWeightKg.coerceIn(minWeight, maxWeight),
                                onValueChange = { targetWeightKg = it },
                                valueRange = minWeight..maxWeight,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.onBackground,
                                    activeTrackColor = MaterialTheme.colorScheme.onBackground,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Pace section (matches onboarding design)
                    AutoGenSectionTitle(title = "How fast do you want to reach your goal?", emoji = "⏱️")
                    
                    Text(
                        text = "${if (isGainWeight) "Gain" else "Lose"} weight speed per week",
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .semantics { contentDescription = "Weekly pace: $paceDisplay per week" }
                    )
                    
                    // Animal emoji row (matches onboarding design)
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
                            weightChangePerWeek = rounded
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
                    
                    // Scale labels (updated for 0.1 - 1.5 range)
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
                fontSize = 16.sp,
                color = Color.White
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
            color = MaterialTheme.colorScheme.onBackground
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
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            fontFamily = Inter,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { newValue ->
                newValue.toIntOrNull()?.let { onValueChange(it) }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            suffix = { Text(suffix, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoGenDropdownSelector(
    label: String,
    value: String,
    options: List<String>,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontFamily = Inter,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                singleLine = true
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
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
                color = MaterialTheme.colorScheme.onSurface,
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
