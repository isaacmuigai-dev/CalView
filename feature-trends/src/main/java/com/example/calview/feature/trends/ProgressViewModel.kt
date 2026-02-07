package com.example.calview.feature.trends

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.repository.StreakFreezeRepository
import com.example.calview.core.data.repository.WeightHistoryRepository
import com.example.calview.core.data.repository.ExerciseRepository
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
import com.example.calview.core.data.notification.NotificationHandler

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

private data class ActivityUpdate(
    val healthData: com.example.calview.core.data.health.HealthData,
    val totalToday: Int,
    val manualToday: Int,
    val combinedWeekly: Double,
    val sevenDayMax: Int,
    val displayRecord: Int,
    val persistentRecord: Int,
    val manualWeeklySum: Int
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
    val caloriesBurned: Int = 0,  // Total = Health Connect + Manual Exercise
    val manualExerciseCalories: Int = 0,  // Today's manual exercise calories
    
    // Weekly data
    val weeklySteps: Long = 0,
    val weeklyCaloriesBurned: Double = 0.0,  // Health Connect only
    val weeklyExerciseCalories: Int = 0,  // 7-day manual exercise calories
    val caloriesBurnedRecord: Double = 0.0,
    val stepsRecord: Long = 0,  // Best daily steps in past 7 days
    val weeklyCalories: List<DailyCalories> = emptyList(),


    val weeklyAverageCalories: Int = 0,
    val selectedWeekOffset: Int = 0,  // 0 = this week, 1 = last week, 2 = 2 weeks ago, 3 = 3 weeks ago
    
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
    val showGoalJourney: Boolean = false,
    val hasSeenWalkthrough: Boolean = true,
    
    // Gamification & Checklist
    val userLevel: Int = 1,
    val userXp: Int = 0,
    val xpRequired: Int = 1000,
    val checklistItems: List<ChecklistItem> = emptyList()
)

data class ChecklistItem(
    val title: String,
    val status: String, // e.g., "Hit!", "2/8 glasses", "Log lunch"
    val isCompleted: Boolean,
    val icon: ImageVector
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
open class ProgressViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mealRepository: MealRepository,
    private val healthConnectManager: HealthConnectManager,
    private val selectedDateHolder: SelectedDateHolder,
    private val weightHistoryRepository: WeightHistoryRepository,
    private val predictionEngine: com.example.calview.core.data.prediction.WeightPredictionEngine,
    private val streakFreezeRepository: StreakFreezeRepository,
    private val notificationHandler: NotificationHandler,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()
    
    init {
        observeBaseMetrics()
        observeGamification()
        observeChecklist()
        observeActivityStats()
        observeMealsAndStreaks()
        observeWeightHistoryAndPrediction()
        observeStreakFreeze()
        observeWalkthroughStatus()
        observeGoalMilestones()
        observeDateChanges()
    }


    private fun observeGoalMilestones() {
        viewModelScope.launch {
            uiState.collect { state ->
                // Only notify if it's today
                if (!state.isToday) return@collect

                val todayStr = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())
                val notifiedFlags = userPreferencesRepository.notifiedDailyGoalFlags.first().split(",")
                val lastNotifiedDate = userPreferencesRepository.lastNotifiedDailyGoalDate.first()

                // If date changed, flags are effectively empty (handled in repo, but for local logic simplicity)
                val activeFlags = if (lastNotifiedDate == todayStr) notifiedFlags.toSet() else emptySet()

                // Calorie Goal
                if (!activeFlags.contains("calories") && state.todayCalories >= state.calorieGoal && state.calorieGoal > 0) {
                    userPreferencesRepository.setDailyGoalNotified(todayStr, "calories")
                    notificationHandler.showNotification(
                        id = NotificationHandler.ID_DAILY_GOAL,
                        channelId = NotificationHandler.CHANNEL_ENGAGEMENT,
                        title = "🎯 Calorie Goal Reached!",
                        message = "Great job! You've hit your daily calorie goal of ${state.calorieGoal} kcal.",
                        navigateTo = "main?tab=0"
                    )
                }

                // Protein Goal
                if (!activeFlags.contains("protein") && state.todayProtein >= state.proteinGoal && state.proteinGoal > 0) {
                    userPreferencesRepository.setDailyGoalNotified(todayStr, "protein")
                    notificationHandler.showNotification(
                        id = NotificationHandler.ID_DAILY_GOAL,
                        channelId = NotificationHandler.CHANNEL_ENGAGEMENT,
                        title = "💪 Protein Goal Hit!",
                        message = "Nice! You've reached your protein goal of ${state.proteinGoal}g today.",
                        navigateTo = "main?tab=0"
                    )
                }

                // Carbs/Fats Goals
                if (!activeFlags.contains("carbs") && state.todayCarbs >= state.carbsGoal && state.carbsGoal > 0) {
                    userPreferencesRepository.setDailyGoalNotified(todayStr, "carbs")
                    notificationHandler.showNotification(
                        id = NotificationHandler.ID_DAILY_GOAL,
                        channelId = NotificationHandler.CHANNEL_ENGAGEMENT,
                        title = "🍞 Carbs Goal Reached!",
                        message = "You've reached your carbohydrate goal for today.",
                        navigateTo = "main?tab=0"
                    )
                }
                
                if (!activeFlags.contains("fats") && state.todayFats >= state.fatsGoal && state.fatsGoal > 0) {
                    userPreferencesRepository.setDailyGoalNotified(todayStr, "fats")
                    notificationHandler.showNotification(
                        id = NotificationHandler.ID_DAILY_GOAL,
                        channelId = NotificationHandler.CHANNEL_ENGAGEMENT,
                        title = "🥑 Fats Goal Reached!",
                        message = "You've reached your fats goal for today.",
                        navigateTo = "main?tab=0"
                    )
                }

                // Max Steps Record
                if (!activeFlags.contains("steps_record") && state.todaySteps > state.stepsRecord && state.stepsRecord > 0) {
                    userPreferencesRepository.setDailyGoalNotified(todayStr, "steps_record")
                    notificationHandler.showNotification(
                        id = NotificationHandler.ID_PERSONAL_BEST,
                        channelId = NotificationHandler.CHANNEL_ENGAGEMENT,
                        title = "🏆 New Steps Record!",
                        message = "Boom! That's a new personal best for daily steps: ${state.todaySteps}!",
                        navigateTo = "progress"
                    )
                }
                
                // Personal Best (Calories Burned Record)
                if (!activeFlags.contains("burned_record") && state.caloriesBurned > state.caloriesBurnedRecord && state.caloriesBurnedRecord > 0) {
                    userPreferencesRepository.setDailyGoalNotified(todayStr, "burned_record")
                    notificationHandler.showNotification(
                        id = NotificationHandler.ID_PERSONAL_BEST,
                        channelId = NotificationHandler.CHANNEL_ENGAGEMENT,
                        title = "💎 New Calories Record!",
                        message = "Incredible! You just set a new record for calories burned: ${state.caloriesBurned} kcal!",
                        navigateTo = "progress"
                    )
                }

                // Weight Goal Hit
                val isGoalHit = when (state.userGoal.lowercase()) {
                    "lose weight", "lose" -> state.currentWeight <= state.goalWeight
                    "gain weight", "gain" -> state.currentWeight >= state.goalWeight
                    "maintain weight", "maintain" -> kotlin.math.abs(state.currentWeight - state.goalWeight) < 0.5f
                    else -> false
                }

                if (!activeFlags.contains("weight_goal") && isGoalHit && state.goalWeight > 0) {
                    userPreferencesRepository.setDailyGoalNotified(todayStr, "weight_goal")
                    notificationHandler.showNotification(
                        id = NotificationHandler.ID_DAILY_GOAL,
                        channelId = NotificationHandler.CHANNEL_ENGAGEMENT,
                        title = "🎉 Weight Goal Reached!",
                        message = "Congratulations! You've reached your target weight of ${state.goalWeight} kg.",
                        navigateTo = "progress"
                    )
                }
            }
        }
    }

    private fun observeWalkthroughStatus() {
        viewModelScope.launch {
            userPreferencesRepository.hasSeenProgressWalkthrough.collect { seen ->
                _uiState.update { it.copy(hasSeenWalkthrough = seen) }
            }
        }
    }

    private fun observeBaseMetrics() {
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
            }.combine(weightHistoryRepository.getAllWeightHistory()) { metrics, history ->
                // Fallback: If startWeight is same as current (default), try to find oldest historical weight
                val effectiveStart = if (kotlin.math.abs(metrics.startWeight - metrics.weight) < 0.1f) {
                    history.minByOrNull { it.timestamp }?.weight ?: metrics.startWeight
                } else {
                    metrics.startWeight
                }
                metrics.copy(startWeight = effectiveStart)
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
                
                val weightProgress = when (metrics.goal.trim().lowercase()) {
                    "lose weight", "lose" -> {
                        val totalToLose = start - goal
                        val progress = if (totalToLose <= 0) 1f else ((start - current) / totalToLose).coerceIn(0f, 1f)
                        progress
                    }
                    "gain weight", "gain" -> {
                        val totalToGain = goal - start
                        val progress = if (totalToGain <= 0) 1f else ((current - start) / totalToGain).coerceIn(0f, 1f)
                        progress
                    }
                    else -> 1f
                }

                // Goal Journey Calculations
                val weightDiff = kotlin.math.abs(goal - current).toInt()
                val weeklyPace = metrics.pace.coerceAtLeast(0.1f)
                val weeksToGoal = (weightDiff / weeklyPace).toInt()
                val estimatedDate = LocalDate.now().plusWeeks(weeksToGoal.toLong())
                val showJourney = metrics.goal != "Maintain" && goal > 0
                
                _uiState.update { currentState ->
                    currentState.copy(
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
                }
            }.collect { }
        }
    }

    private fun observeGamification() {
        viewModelScope.launch {
            userPreferencesRepository.userLevel.collect { level ->
                _uiState.update { it.copy(userLevel = level, xpRequired = level * 1000) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.userXp.collect { xp ->
                _uiState.update { it.copy(userXp = xp) }
            }
        }
    }

    private fun observeChecklist() {
        viewModelScope.launch {
            combine(
                uiState.map { it.todayProtein to it.proteinGoal }.distinctUntilChanged(),
                userPreferencesRepository.waterConsumed,
                userPreferencesRepository.waterDate
            ) { (protein, goal), water, waterDate ->
                val items = mutableListOf<ChecklistItem>()
                
                // 1. Protein Goal
                items.add(
                    ChecklistItem(
                        title = "Protein Goal",
                        status = if (protein >= goal) "Goal Reached!" else "${(goal - protein).toInt()}g to go",
                        isCompleted = protein >= goal,
                        icon = Icons.Default.FitnessCenter
                    )
                )

                // 2. Hydration
                val today = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val isToday = waterDate >= today
                
                val glasses = if (isToday) water / 250 else 0
                items.add(
                    ChecklistItem(
                        title = "Hydration",
                        status = "$glasses/8 servings",
                        isCompleted = glasses >= 8,
                        icon = Icons.Default.WaterDrop
                    )
                )
                
                items
            }.collect { items ->
                _uiState.update { it.copy(checklistItems = items) }
            }
        }
    }

    private fun observeActivityStats() {
        // Unified Activity Stats Collector (Source Flow based to avoid loops)
        viewModelScope.launch {
            combine(
                healthConnectManager.healthData,
                selectedDateHolder.selectedDate.flatMapLatest { date ->
                    val dateString = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)
                    exerciseRepository.getTotalCaloriesBurnedForDate(dateString)
                }.distinctUntilChanged(),
                exerciseRepository.getLastSevenDaysCalories().distinctUntilChanged(),
                userPreferencesRepository.recordBurn.distinctUntilChanged()
            ) { healthData, manualToday, manualSevenDays, persistentRecord ->

                // 1. Today's Combined Total
                val totalBurnedToday = healthData.caloriesBurned.toInt() + manualToday
                
                // 2. Weekly Combined Total
                val hcDaily = healthData.lastSevenDaysCalories
                val combinedWeekly = if (hcDaily.isNotEmpty() && hcDaily.size == manualSevenDays.size) {
                     (hcDaily.indices).sumOf { i -> hcDaily[i] + manualSevenDays[i] }
                } else {
                     healthData.weeklyCaloriesBurned + manualSevenDays.sum()
                }

                // 3. 7-Day Max
                val sevenDayMax = if (hcDaily.isNotEmpty() && hcDaily.size == manualSevenDays.size) {
                    (hcDaily.indices).maxOf { i -> hcDaily[i] + manualSevenDays[i] }.toInt()
                } else {
                    maxOf(healthData.maxCaloriesBurnedRecord.toInt(), manualSevenDays.maxOrNull()?.toInt() ?: 0)
                }
                
                // 4. Record calculation (max of everything)
                val overallRecord = maxOf(persistentRecord, totalBurnedToday, sevenDayMax)
                
                ActivityUpdate(
                    healthData = healthData,
                    totalToday = totalBurnedToday,
                    manualToday = manualToday,
                    combinedWeekly = combinedWeekly,
                    sevenDayMax = sevenDayMax,
                    displayRecord = overallRecord,
                    persistentRecord = persistentRecord,
                    manualWeeklySum = manualSevenDays.sum().toInt()
                )
            }.collect { update ->
                // Update UI State
                _uiState.update { 
                    it.copy(
                        todaySteps = update.healthData.steps.toInt(),
                        caloriesBurned = update.totalToday,
                        manualExerciseCalories = update.manualToday,
                        weeklySteps = update.healthData.weeklySteps,
                        stepsRecord = update.healthData.maxStepsRecord,
                        weeklyExerciseCalories = update.manualWeeklySum,
                        weeklyCaloriesBurned = update.combinedWeekly,
                        caloriesBurnedRecord = update.displayRecord.toDouble()
                    )
                }
                
                // Sync to Repository/Widget if record broken or stats different from persistent
                // We only update if something actually changed to avoid redundant syncs
                val currentRecord = update.displayRecord
                if (currentRecord > update.persistentRecord) {
                    userPreferencesRepository.setActivityStats(
                        caloriesBurned = update.totalToday,
                        weeklyBurn = update.combinedWeekly.toInt(),
                        recordBurn = currentRecord
                    )
                }
            }
        }
    }
        
    private fun observeDateChanges() {
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
    }

    private fun observeMealsAndStreaks() {
        // Load overall progress data (streaks, weekly trends)
        viewModelScope.launch {
            combine(
                mealRepository.getAllMeals(),
                uiState.map { it.selectedWeekOffset }.distinctUntilChanged()
            ) { meals, weekOffset ->
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
                
                // Generate weekly calorie data based on weekOffset
                val targetWeekStart = weekStart.minusWeeks(weekOffset.toLong())
                
                val weeklyCalories = (0..6).map { dayOffset ->
                    val checkDate = targetWeekStart.plusDays(dayOffset.toLong())
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

                Triple(completedDays, weeklyCalories, weeklyAvg) to mealDates
            }.collect { (weeklyData, mealDates) ->
                val (completedDays, weeklyCalories, weeklyAvg) = weeklyData
                
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
    }

    private fun observeWeightHistoryAndPrediction() {
        // Load real weight history and generate prediction
        viewModelScope.launch {

            // Combine history and goal weight to generate prediction
            combine(
                weightHistoryRepository.getAllWeightHistory(),
                userPreferencesRepository.goalWeight
            ) { historyEntities, goalWeight ->
                // Convert entities to UI model, deduplicating by date
                val uiHistory = historyEntities
                    .groupBy { 
                        java.time.Instant.ofEpochMilli(it.timestamp)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    .map { (date, entities) ->
                        val latestEntity = entities.maxByOrNull { it.timestamp } ?: entities.first()
                        WeightEntry(date = date, weight = latestEntity.weight)
                    }
                    .sortedByDescending { it.date }
                
                // Generate prediction
                val prediction = predictionEngine.predictWeight(historyEntities, goalWeight)
                val predDateStr = prediction.projectedDate?.let {
                    java.time.Instant.ofEpochMilli(it)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))
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
    }
        
    private fun observeStreakFreeze() {
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
        // Refresh health data when manually refreshing
        viewModelScope.launch {
            try {
                if (healthConnectManager.isAvailable()) {
                    healthConnectManager.readTodayData()
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    
    fun useStreakFreeze() {
        viewModelScope.launch {
            val yesterday = LocalDate.now().minusDays(1)
            streakFreezeRepository.useFreeze(yesterday)
        }
    }

    fun setHasSeenWalkthrough(seen: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setHasSeenProgressWalkthrough(seen)
        }
    }

    fun setWeekOffset(offset: Int) {
        _uiState.update { it.copy(selectedWeekOffset = offset.coerceIn(0, 3)) }
    }

}
