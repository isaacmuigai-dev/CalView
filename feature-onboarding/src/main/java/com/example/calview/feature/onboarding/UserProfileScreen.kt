package com.example.calview.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.CalAIButton
import com.example.calview.core.ui.components.CalAICard
import com.example.calview.core.ui.components.StandardWheelPicker
import com.example.calview.core.ui.components.UnitToggle
import com.example.calview.core.ui.theme.Inter
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols

/**
 * Consolidated User Profile Screen that combines:
 * - Gender selection
 * - Birth date
 * - Height & Weight
 * - Workouts per week
 * - Goal selection
 * - Diet preference
 * 
 * Uses a horizontal pager with 6 steps to reduce navigation complexity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 6 })
    val scope = rememberCoroutineScope()
    
    val stepTitles = listOf(
        "Choose your Gender",
        "When were you born?",
        "Height & Weight",
        "Workouts per week",
        "What is your goal?",
        "Do you follow a specific diet?"
    )
    
    val stepSubtitles = listOf(
        "This will be used to calibrate your custom plan.",
        "This helps us calculate your daily nutrition goals.",
        "This will be taken into account when calculating your daily nutrition goals.",
        "This will be used to calibrate your custom plan.",
        "This helps us generate a plan for your calorie intake.",
        "We'll customize your meal suggestions accordingly."
    )
    
    val currentStep = pagerState.currentPage
    val progress = (currentStep + 1).toFloat() / 6f
    
    val canProceed = when (currentStep) {
        0 -> uiState.gender.isNotEmpty()
        1 -> true // Date always has default value
        2 -> true // Height/Weight always have defaults
        3 -> uiState.workoutsPerWeek.isNotEmpty()
        4 -> uiState.goal.isNotEmpty()
        5 -> uiState.dietPreference.isNotEmpty()
        else -> false
    }
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (currentStep > 0) {
                                scope.launch { pagerState.animateScrollToPage(currentStep - 1) }
                            } else {
                                onBack()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                
                // Progress indicator
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(4.dp),
                    color = Color.Black,
                    trackColor = Color(0xFFF3F3F3),
                    strokeCap = StrokeCap.Round
                )
                
                // Step indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(6) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (index == currentStep) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index <= currentStep) Color.Black 
                                    else Color(0xFFE0E0E0)
                                )
                        )
                    }
                }
            }
        },
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp)) {
                CalAIButton(
                    text = if (currentStep < 5) "Continue" else "Complete Profile",
                    onClick = {
                        if (currentStep < 5) {
                            scope.launch { pagerState.animateScrollToPage(currentStep + 1) }
                        } else {
                            onComplete()
                        }
                    },
                    enabled = canProceed
                )
            }
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stepTitles[currentStep],
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                color = Color.Black
            )
            
            Text(
                text = stepSubtitles[currentStep],
                fontFamily = Inter,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false // Disable swipe, use buttons only
            ) { page ->
                when (page) {
                    0 -> GenderStep(
                        selectedGender = uiState.gender,
                        onGenderSelected = { viewModel.onGenderSelected(it) }
                    )
                    1 -> BirthDateStep(
                        birthMonth = uiState.birthMonth,
                        birthDay = uiState.birthDay,
                        birthYear = uiState.birthYear,
                        onDateChanged = { m, d, y -> viewModel.onBirthDateChanged(m, d, y) }
                    )
                    2 -> HeightWeightStep(
                        isMetric = uiState.isMetric,
                        heightFt = uiState.heightFt,
                        heightIn = uiState.heightIn,
                        weight = uiState.weight,
                        onUnitToggle = { viewModel.onUnitToggle(it) },
                        onHeightChanged = { ft, inches -> viewModel.onHeightChanged(ft, inches) },
                        onWeightChanged = { viewModel.onWeightChanged(it) }
                    )
                    3 -> WorkoutsStep(
                        selectedWorkouts = uiState.workoutsPerWeek,
                        onWorkoutsSelected = { viewModel.onWorkoutsSelected(it) }
                    )
                    4 -> GoalStep(
                        selectedGoal = uiState.goal,
                        onGoalSelected = { viewModel.onGoalSelected(it) }
                    )
                    5 -> DietStep(
                        selectedDiet = uiState.dietPreference,
                        onDietSelected = { viewModel.onDietSelected(it) }
                    )
                }
            }
        }
    }
}

// ============ Step Components ============

@Composable
private fun GenderStep(
    selectedGender: String,
    onGenderSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        listOf("Female", "Male", "Other").forEach { option ->
            Surface(
                onClick = { onGenderSelected(option) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF9F9F9),
                border = if (selectedGender == option) 
                    androidx.compose.foundation.BorderStroke(2.dp, Color.Black) 
                else null
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = option,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun BirthDateStep(
    birthMonth: Int,
    birthDay: Int,
    birthYear: Int,
    onDateChanged: (Int, Int, Int) -> Unit
) {
    val months = DateFormatSymbols().months.toList().filter { it.isNotEmpty() }
    val days = (1..31).map { it.toString().padStart(2, '0') }
    val years = (1940..2024).map { it.toString() }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StandardWheelPicker(
            items = months,
            initialIndex = (birthMonth - 1).coerceIn(0, months.size - 1),
            onItemSelected = { onDateChanged(it + 1, birthDay, birthYear) },
            modifier = Modifier.weight(1.5f)
        )
        StandardWheelPicker(
            items = days,
            initialIndex = (birthDay - 1).coerceIn(0, days.size - 1),
            onItemSelected = { onDateChanged(birthMonth, it + 1, birthYear) },
            modifier = Modifier.weight(1f)
        )
        StandardWheelPicker(
            items = years,
            initialIndex = years.indexOf(birthYear.toString()).coerceAtLeast(0),
            onItemSelected = { onDateChanged(birthMonth, birthDay, years[it].toInt()) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HeightWeightStep(
    isMetric: Boolean,
    heightFt: Int,
    heightIn: Int,
    weight: Float,
    onUnitToggle: (Boolean) -> Unit,
    onHeightChanged: (Int, Int) -> Unit,
    onWeightChanged: (Float) -> Unit
) {
    val ftItems = (2..8).map { "$it ft" }
    val inItems = (0..11).map { "$it in" }
    val weightItems = (50..400).map { "$it lb" }
    
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
                    fontSize = 16.sp,
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
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                StandardWheelPicker(
                    items = weightItems,
                    initialIndex = (weight.toInt() - 50).coerceAtLeast(0),
                    onItemSelected = { onWeightChanged(it.toFloat() + 50) },
                    modifier = Modifier.width(100.dp)
                )
            }
        }
    }
}

@Composable
private fun WorkoutsStep(
    selectedWorkouts: String,
    onWorkoutsSelected: (String) -> Unit
) {
    val options = listOf(
        "0 - 2" to "Workouts now and then",
        "3 - 5" to "A few workouts per week",
        "6+" to "Dedicated athlete"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        options.forEach { (title, desc) ->
            CalAICard(
                title = title,
                subtitle = desc,
                isSelected = selectedWorkouts == title,
                onClick = { onWorkoutsSelected(title) }
            )
        }
    }
}

@Composable
private fun GoalStep(
    selectedGoal: String,
    onGoalSelected: (String) -> Unit
) {
    val goals = listOf("Lose Weight", "Maintain", "Gain Weight")
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        goals.forEach { goal ->
            CalAICard(
                title = goal,
                isSelected = selectedGoal == goal,
                onClick = { onGoalSelected(goal) }
            )
        }
    }
}

@Composable
private fun DietStep(
    selectedDiet: String,
    onDietSelected: (String) -> Unit
) {
    val diets = listOf(
        "Classic" to "🍗",
        "Pescatarian" to "🐟",
        "Vegetarian" to "🍎",
        "Vegan" to "🌿"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        diets.forEach { (name, icon) ->
            CalAICard(
                title = name,
                isSelected = selectedDiet == name,
                onClick = { onDietSelected(name) },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFF5F5F5), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(icon, fontSize = 20.sp)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserProfileScreenPreview() {
    // Preview with mock data
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text("User Profile Screen Preview", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        GenderStep(selectedGender = "Female", onGenderSelected = {})
    }
}
