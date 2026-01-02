package com.example.calview.feature.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.roundToInt

data class WeightEntry(
    val date: LocalDate,
    val weight: Float
)

data class DailyCalories(
    val day: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fats: Float
)

// Helper data classes for flow combining
private data class UserMetrics(
    val weight: Float,
    val goalWeight: Float,
    val height: Int,
    val calories: Int,
    val protein: Int
)

private data class MacroGoals(
    val carbs: Int,
    val fats: Int,
    val stepsGoal: Int
)

data class ProgressUiState(
    // User metrics
    val currentWeight: Float = 0f,
    val goalWeight: Float = 0f,
    val height: Int = 0, // in cm
    val bmi: Float = 0f,
    val bmiCategory: String = "Healthy",
    val weightProgress: Float = 0f, // 0-1
    
    // Goals
    val calorieGoal: Int = 2000,
    val proteinGoal: Int = 120,
    val carbsGoal: Int = 200,
    val fatsGoal: Int = 65,
    val stepsGoal: Int = 10000,
    
    // Today's progress
    val todayCalories: Int = 0,
    val todayProtein: Float = 0f,
    val todayCarbs: Float = 0f,
    val todayFats: Float = 0f,
    val todaySteps: Int = 0,
    val caloriesBurned: Int = 0,
    
    // Weekly data
    val weeklyCalories: List<DailyCalories> = emptyList(),
    val weeklyAverageCalories: Int = 0,
    
    // Streak
    val dayStreak: Int = 0,
    val bestStreak: Int = 0, // Best ever streak
    val completedDays: List<Boolean> = listOf(false, false, false, false, false, false, false),
    
    // Weight history
    val weightHistory: List<WeightEntry> = emptyList(),
    
    // Loading state
    val isLoading: Boolean = true
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mealRepository: MealRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()
    
    init {
        loadProgressData()
    }
    
    private fun loadProgressData() {
        viewModelScope.launch {
            // Combine user metrics flows (split to avoid 5+ flow limitation)
            combine(
                userPreferencesRepository.weight,
                userPreferencesRepository.goalWeight,
                userPreferencesRepository.height,
                userPreferencesRepository.recommendedCalories,
                userPreferencesRepository.recommendedProtein
            ) { weight, goalWeight, height, calories, protein ->
                UserMetrics(weight, goalWeight, height, calories, protein)
            }.combine(
                combine(
                    userPreferencesRepository.recommendedCarbs,
                    userPreferencesRepository.recommendedFats,
                    userPreferencesRepository.dailyStepsGoal
                ) { carbs, fats, stepsGoal ->
                    MacroGoals(carbs, fats, stepsGoal)
                }
            ) { metrics, macros ->
                // Calculate BMI
                val heightM = metrics.height / 100f
                val bmi = if (heightM > 0) metrics.weight / (heightM * heightM) else 0f
                
                // Determine BMI category
                val bmiCategory = when {
                    bmi < 18.5f -> "Underweight"
                    bmi < 25f -> "Healthy"
                    bmi < 30f -> "Overweight"
                    else -> "Obese"
                }
                
                // Calculate weight progress
                val weightProgress = if (metrics.goalWeight > 0 && metrics.weight > metrics.goalWeight) {
                    ((metrics.weight - metrics.goalWeight) / metrics.weight).coerceIn(0f, 1f)
                } else if (metrics.goalWeight > 0 && metrics.weight < metrics.goalWeight) {
                    ((metrics.goalWeight - metrics.weight) / metrics.goalWeight).coerceIn(0f, 1f)
                } else if (metrics.goalWeight > 0) {
                    1f // At goal
                } else {
                    0f
                }
                
                ProgressUiState(
                    currentWeight = metrics.weight,
                    goalWeight = metrics.goalWeight,
                    height = metrics.height,
                    bmi = bmi,
                    bmiCategory = bmiCategory,
                    weightProgress = weightProgress,
                    calorieGoal = metrics.calories,
                    proteinGoal = metrics.protein,
                    carbsGoal = macros.carbs,
                    fatsGoal = macros.fats,
                    stepsGoal = macros.stepsGoal,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.update { currentState ->
                    currentState.copy(
                        currentWeight = state.currentWeight,
                        goalWeight = state.goalWeight,
                        height = state.height,
                        bmi = state.bmi,
                        bmiCategory = state.bmiCategory,
                        weightProgress = state.weightProgress,
                        calorieGoal = state.calorieGoal,
                        proteinGoal = state.proteinGoal,
                        carbsGoal = state.carbsGoal,
                        fatsGoal = state.fatsGoal,
                        stepsGoal = state.stepsGoal,
                        isLoading = false
                    )
                }
            }
        }

        
        // Load today's meal data
        viewModelScope.launch {
            mealRepository.getMealsForToday().collect { meals ->
                val todayCalories = meals.sumOf { it.calories }
                val todayProtein = meals.sumOf { it.protein.toDouble() }.toFloat()
                val todayCarbs = meals.sumOf { it.carbs.toDouble() }.toFloat()
                val todayFats = meals.sumOf { it.fats.toDouble() }.toFloat()
                
                _uiState.update { it.copy(
                    todayCalories = todayCalories,
                    todayProtein = todayProtein,
                    todayCarbs = todayCarbs,
                    todayFats = todayFats
                )}
            }
        }
        
        // Calculate streak (simplified - based on logged meals)
        viewModelScope.launch {
            mealRepository.getAllMeals().collect { meals ->
                // Group meals by date and check consecutive days
                val today = LocalDate.now()
                val mealDates = meals.map { meal ->
                    LocalDate.ofEpochDay(meal.timestamp / (24 * 60 * 60 * 1000))
                }.distinct().sortedDescending()
                
                var streak = 0
                var currentDate = today
                for (date in mealDates) {
                    if (date == currentDate || date == currentDate.minusDays(1)) {
                        streak++
                        currentDate = date
                    } else {
                        break
                    }
                }
                
                // Calculate completed days for the week
                val weekStart = today.minusDays(today.dayOfWeek.value.toLong() % 7)
                val completedDays = (0..6).map { dayOffset ->
                    val checkDate = weekStart.plusDays(dayOffset.toLong())
                    mealDates.contains(checkDate)
                }
                
                // Generate weekly calorie data
                val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                val weeklyCalories = (0..6).map { dayOffset ->
                    val checkDate = weekStart.plusDays(dayOffset.toLong())
                    val dayMeals = meals.filter { meal ->
                        LocalDate.ofEpochDay(meal.timestamp / (24 * 60 * 60 * 1000)) == checkDate
                    }
                    DailyCalories(
                        day = weekDays[dayOffset],
                        calories = dayMeals.sumOf { it.calories },
                        protein = dayMeals.sumOf { it.protein.toDouble() }.toFloat(),
                        carbs = dayMeals.sumOf { it.carbs.toDouble() }.toFloat(),
                        fats = dayMeals.sumOf { it.fats.toDouble() }.toFloat()
                    )
                }
                
                val weeklyAvg = weeklyCalories.filter { it.calories > 0 }
                    .map { it.calories }
                    .average()
                    .takeIf { !it.isNaN() }?.roundToInt() ?: 0
                
                // Calculate best streak ever
                var bestStreak = streak
                var tempStreak = 0
                var lastDate: LocalDate? = null
                for (date in mealDates.sortedDescending()) {
                    if (lastDate == null || lastDate.minusDays(1) == date) {
                        tempStreak++
                    } else {
                        bestStreak = maxOf(bestStreak, tempStreak)
                        tempStreak = 1
                    }
                    lastDate = date
                }
                bestStreak = maxOf(bestStreak, tempStreak)
                
                _uiState.update { it.copy(
                    dayStreak = streak.coerceAtLeast(1),
                    bestStreak = bestStreak.coerceAtLeast(1),
                    completedDays = completedDays,
                    weeklyCalories = weeklyCalories,
                    weeklyAverageCalories = weeklyAvg
                )}
            }
        }
        
        // Generate sample weight history (in real app, this would come from a WeightHistory table)
        viewModelScope.launch {
            userPreferencesRepository.weight.first().let { currentWeight ->
                val history = (6 downTo 0).map { daysAgo ->
                    WeightEntry(
                        date = LocalDate.now().minusDays(daysAgo.toLong()),
                        weight = currentWeight + (daysAgo * 0.3f) // Simulated weight loss
                    )
                }
                _uiState.update { it.copy(weightHistory = history) }
            }
        }
    }
    
    fun refreshData() {
        _uiState.update { it.copy(isLoading = true) }
        loadProgressData()
    }
}
