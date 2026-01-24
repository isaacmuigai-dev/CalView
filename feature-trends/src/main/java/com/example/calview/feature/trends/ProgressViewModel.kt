package com.example.calview.feature.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.repository.StreakFreezeRepository
import com.example.calview.core.data.repository.WeightHistoryRepository
import com.example.calview.core.data.health.HealthConnectManager
import com.example.calview.core.data.state.SelectedDateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt
import com.example.calview.feature.trends.R

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
    val startWeight: Float,
    val height: Int,
    val calories: Int,
    val protein: Int,
    val goal: String,
    val pace: Float
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
    val bmiCategory: Int = R.string.bmi_healthy,
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
    val todayFiber: Float = 0f,
    val todaySugar: Float = 0f,
    val todaySodium: Float = 0f,
    val todaySteps: Int = 0,
    val caloriesBurned: Int = 0,
    
    // Weekly data
    val weeklySteps: Long = 0,
    val weeklyCaloriesBurned: Double = 0.0,
    val caloriesBurnedRecord: Double = 0.0,
    val stepsRecord: Long = 0,  // Best daily steps in past 7 days
    val weeklyCalories: List<DailyCalories> = emptyList(),
    val weeklyAverageCalories: Int = 0,
    
    // Streak
    val dayStreak: Int = 0,
    val bestStreak: Int = 0, // Best ever streak
    val completedDays: List<Boolean> = listOf(false, false, false, false, false, false, false),
    
    // Weight history
    val weightHistory: List<WeightEntry> = emptyList(),
    
    // Weight Prediction
    val predictedWeight30Days: Float = 0f,
    val predictedDate: String? = null,
    val predictionTrend: com.example.calview.core.data.prediction.WeightPredictionEngine.Trend = com.example.calview.core.data.prediction.WeightPredictionEngine.Trend.INSUFFICIENT_DATA,
    
    // Selected date display
    val selectedDate: LocalDate = LocalDate.now(),
    val isToday: Boolean = true,
    
    // Streak Freeze
    val remainingFreezes: Int = 0,
    val maxFreezes: Int = 2,
    val yesterdayMissed: Boolean = false,
    
    // Loading state
    val isLoading: Boolean = true,
    
    // Goal Journey
    val userGoal: String = "Maintain",
    val weeklyPace: Float = 0.5f,
    val weeksToGoal: Int = 0,
    val weightDiff: Int = 0,
    val estimatedGoalDate: LocalDate = LocalDate.now(),
    val showGoalJourney: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mealRepository: MealRepository,
    private val healthConnectManager: HealthConnectManager,
    private val selectedDateHolder: SelectedDateHolder,
    private val weightHistoryRepository: WeightHistoryRepository,
    private val predictionEngine: com.example.calview.core.data.prediction.WeightPredictionEngine,
    private val streakFreezeRepository: StreakFreezeRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()
    
    init {
        loadProgressData()
    }

    private fun loadProgressData() {
        viewModelScope.launch {
            // Combine user metrics flows (chaining to handle 5+ flows)
            combine(
                userPreferencesRepository.weight,
                userPreferencesRepository.goalWeight,
                userPreferencesRepository.startWeight,
                userPreferencesRepository.height,
                userPreferencesRepository.recommendedCalories,
            ) { weight, goalWeight, startWeight, height, calories ->
                UserMetrics(weight, goalWeight, startWeight, height, calories, 0, "", 0f)
            }.combine(userPreferencesRepository.recommendedProtein) { metrics, protein ->
                metrics.copy(protein = protein)
            }.combine(userPreferencesRepository.userGoal) { metrics, goal ->
                metrics.copy(goal = goal)
            }.combine(userPreferencesRepository.weightChangePerWeek) { metrics, pace ->
                metrics.copy(pace = pace)
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
                    bmi < 18.5f -> R.string.bmi_underweight
                    bmi < 25f -> R.string.bmi_healthy
                    bmi < 30f -> R.string.bmi_overweight
                    else -> R.string.bmi_obese
                }
                
                // Calculate weight progress
                val start = if (metrics.startWeight > 0) metrics.startWeight else metrics.weight
                val current = metrics.weight
                val goal = metrics.goalWeight
                
                val weightProgress = if (start == goal) {
                    1f
                } else if (goal < start) {
                    ((start - current) / (start - goal)).coerceIn(0f, 1f)
                } else {
                    ((current - start) / (goal - start)).coerceIn(0f, 1f)
                }

                // Goal Journey Calculations
                val weightDiff = kotlin.math.abs(goal - current).toInt()
                val weeklyPace = metrics.pace.coerceAtLeast(0.1f)
                val weeksToGoal = (weightDiff / weeklyPace).toInt()
                val estimatedDate = LocalDate.now().plusWeeks(weeksToGoal.toLong())
                val showJourney = metrics.goal != "Maintain" && goal > 0
                
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
                    userGoal = metrics.goal,
                    weeklyPace = metrics.pace,
                    weeksToGoal = weeksToGoal,
                    weightDiff = weightDiff,
                    estimatedGoalDate = estimatedDate,
                    showGoalJourney = showJourney,
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
                        userGoal = state.userGoal,
                        weeklyPace = state.weeklyPace,
                        weeksToGoal = state.weeksToGoal,
                        weightDiff = state.weightDiff,
                        estimatedGoalDate = state.estimatedGoalDate,
                        showGoalJourney = state.showGoalJourney,
                        isLoading = false
                    )
                }
            }
        }

        // Collect Health Connect data
        viewModelScope.launch {
            healthConnectManager.healthData.collect { healthData ->
                // Sync to widget whenever we get fresh data
                userPreferencesRepository.setActivityStats(
                    caloriesBurned = healthData.caloriesBurned.toInt(),
                    weeklyBurn = healthData.weeklyCaloriesBurned.toInt(),
                    recordBurn = healthData.maxCaloriesBurnedRecord.toInt()
                )
                
                _uiState.update { 
                    it.copy(
                        todaySteps = healthData.steps.toInt(),
                        caloriesBurned = healthData.caloriesBurned.toInt(),
                        weeklySteps = healthData.weeklySteps,
                        weeklyCaloriesBurned = healthData.weeklyCaloriesBurned,
                        caloriesBurnedRecord = healthData.maxCaloriesBurnedRecord,
                        stepsRecord = healthData.maxStepsRecord
                    )
                }
            }
        }
        
        // Observe shared selected date and refresh health data
        viewModelScope.launch {
            selectedDateHolder.selectedDate.collect { date ->
                _uiState.update { it.copy(
                    selectedDate = date,
                    isToday = date == LocalDate.now()
                )}
                if (healthConnectManager.isAvailable()) {
                    healthConnectManager.readDataForDate(date)
                }
            }
        }

        
        // Load meal data for selected date (reactive to date changes)
        viewModelScope.launch {
            selectedDateHolder.selectedDate.flatMapLatest { date ->
                val dateString = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)
                mealRepository.getMealsForDate(dateString)
            }.collect { meals ->
                val todayCalories = meals.sumOf { it.calories }
                val todayProtein = meals.sumOf { it.protein.toDouble() }.toFloat()
                val todayCarbs = meals.sumOf { it.carbs.toDouble() }.toFloat()
                val todayFats = meals.sumOf { it.fats.toDouble() }.toFloat()
                val todayFiber = meals.sumOf { it.fiber.toDouble() }.toFloat()
                val todaySugar = meals.sumOf { it.sugar.toDouble() }.toFloat()
                val todaySodium = meals.sumOf { it.sodium.toDouble() }.toFloat()
                
                _uiState.update { it.copy(
                    todayCalories = todayCalories,
                    todayProtein = todayProtein,
                    todayCarbs = todayCarbs,
                    todayFats = todayFats,
                    todayFiber = todayFiber,
                    todaySugar = todaySugar,
                    todaySodium = todaySodium
                )}
            }
        }
        // Load overall progress data (streaks, weekly trends)
        viewModelScope.launch {
            mealRepository.getAllMeals().collect { meals ->
                val mealDates = meals.map { meal ->
                    java.time.Instant.ofEpochMilli(meal.timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                }.distinct().sortedDescending()

                // Calculate completed days for the week
                val today = LocalDate.now()
                val weekStart = today.minusDays(today.dayOfWeek.value.toLong() % 7)
                
                val completedDays = (0..6).map { dayOffset ->
                    val checkDate = weekStart.plusDays(dayOffset.toLong())
                    mealDates.contains(checkDate)
                }
                
                // Generate weekly calorie data
                val weeklyCalories = (0..6).map { dayOffset ->
                    val checkDate = weekStart.plusDays(dayOffset.toLong())
                    val dayName = checkDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    val dayMeals = meals.filter { meal ->
                         java.time.Instant.ofEpochMilli(meal.timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate() == checkDate
                    }
                    DailyCalories(
                        day = dayName,
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

                _uiState.update { it.copy(
                    completedDays = completedDays,
                    weeklyCalories = weeklyCalories,
                    weeklyAverageCalories = weeklyAvg
                )}

                // Centralized streak logic
                streakFreezeRepository.getStreakData(mealDates).flatMapLatest { streak ->
                    streakFreezeRepository.calculateBestStreak(mealDates, streak).map { best ->
                        Pair(streak, best)
                    }
                }.collect { (streak, best) ->
                    _uiState.update { it.copy(
                        dayStreak = streak,
                        bestStreak = best
                    )}
                }
            }
        }
        
        // Load real weight history and generate prediction
        viewModelScope.launch {
            // Combine history and goal weight to generate prediction
            combine(
                weightHistoryRepository.getAllWeightHistory(),
                userPreferencesRepository.goalWeight
            ) { historyEntities, goalWeight ->
                // Convert entities to UI model
                val uiHistory = historyEntities.map { entity ->
                     WeightEntry(
                         date = java.time.Instant.ofEpochMilli(entity.timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                         weight = entity.weight
                     )
                }.sortedByDescending { it.date }
                
                // Generate prediction
                val prediction = predictionEngine.predictWeight(historyEntities, goalWeight)
                val predDateStr = prediction.projectedDate?.let {
                    java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(it)
                }
                
                Triple(uiHistory, prediction, predDateStr)
            }.collect { (history, prediction, dateStr) ->
                _uiState.update { 
                    it.copy(
                        weightHistory = history,
                        predictedWeight30Days = prediction.predictedWeight30Days,
                        predictedDate = dateStr,
                        predictionTrend = prediction.trend
                    )
                }
            }
        }
        
        // Load streak freeze data
        viewModelScope.launch {
            streakFreezeRepository.ensureStreakFreezeInitialized()
            streakFreezeRepository.observeCurrentMonthFreezes().collect { freeze ->
                val remaining = freeze?.let { (it.maxFreezes - it.freezesUsed).coerceAtLeast(0) } ?: 2
                val max = freeze?.maxFreezes ?: 2
                
                // Check if yesterday was missed (no meals logged) AND not already frozen
                val yesterday = LocalDate.now().minusDays(1)
                val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
                val yesterdayStr = yesterday.format(formatter)
                
                val isFrozen = freeze?.frozenDates?.split(",")?.contains(yesterdayStr) == true
                
                val yesterdayMeals = mealRepository.getMealsForDate(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd").format(yesterday)
                ).first()
                
                // Missed only if NO meals AND NOT frozen
                val missedYesterday = yesterdayMeals.isEmpty() && !isFrozen
                
                _uiState.update { 
                    it.copy(
                        remainingFreezes = remaining,
                        maxFreezes = max,
                        yesterdayMissed = missedYesterday
                    )
                }
            }
        }
    }
    
    fun refreshData() {
        _uiState.update { it.copy(isLoading = true) }
        loadProgressData()
        // Refresh health data when manually refreshing
        viewModelScope.launch {
            if (healthConnectManager.isAvailable()) {
                healthConnectManager.readTodayData()
            }
        }
    }
    
    fun useStreakFreeze() {
        viewModelScope.launch {
            val yesterday = LocalDate.now().minusDays(1)
            streakFreezeRepository.useFreeze(yesterday)
        }
    }
}
