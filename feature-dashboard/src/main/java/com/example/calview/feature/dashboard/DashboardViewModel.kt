package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.repository.StreakFreezeRepository
import com.example.calview.core.data.local.AnalysisStatus
import com.example.calview.core.data.local.MealEntity
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.health.HealthConnectManager
import com.example.calview.core.data.health.HealthData
import com.example.calview.core.data.state.SelectedDateHolder
import com.example.calview.core.ai.FoodAnalysisService
import android.content.Context
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    val healthConnectManager: HealthConnectManager,
    private val foodAnalysisService: FoodAnalysisService,
    private val coachMessageGenerator: com.example.calview.core.data.coach.CoachMessageGenerator,
    private val smartCoachService: com.example.calview.core.data.coach.SmartCoachService,
    private val selectedDateHolder: SelectedDateHolder,
    private val waterReminderRepository: com.example.calview.core.data.repository.WaterReminderRepository,
    private val streakFreezeRepository: StreakFreezeRepository,
    private val dailyLogRepository: com.example.calview.core.data.repository.DailyLogRepository,
    private val exerciseRepository: com.example.calview.core.data.repository.ExerciseRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Load goal from preferences, defaulting to 2000 if not set or 0
    val dailyGoal = userPreferencesRepository.recommendedCalories
        .map { if (it > 0) it else 2000 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)
    
    // Load steps goal from preferences, defaulting to 10000
    val stepsGoal = userPreferencesRepository.dailyStepsGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000)
    
    // Rollover calories preferences
    private val rolloverEnabled = userPreferencesRepository.rolloverExtraCalories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    private val rolloverAmount = userPreferencesRepository.rolloverCaloriesAmount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    // Add burned calories back preference
    private val addCaloriesBackEnabled = userPreferencesRepository.addCaloriesBack
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    // Macro goals from user preferences
    private val storedProteinGoal = userPreferencesRepository.recommendedProtein
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    private val storedCarbsGoal = userPreferencesRepository.recommendedCarbs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    private val storedFatsGoal = userPreferencesRepository.recommendedFats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val hasSeenWalkthrough = userPreferencesRepository.hasSeenDashboardWalkthrough
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val lastActivityTimestamp = userPreferencesRepository.lastActivityTimestamp
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    
    // Smart coach tip state
    private val _smartCoachTip = MutableStateFlow<com.example.calview.core.data.coach.CoachMessageGenerator.CoachTip?>(null)
    private val smartCoachTip = _smartCoachTip.asStateFlow()

    fun setHasSeenWalkthrough(seen: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setHasSeenDashboardWalkthrough(seen)
        }
    }

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate = _selectedDate.asStateFlow()
    
    // Water consumption (persisted)
    val waterConsumed = selectedDate.flatMapLatest { date ->
        val dateString = dateFormat.format(date.time)
        dailyLogRepository.getLogForDate(dateString).map { it?.waterIntake ?: 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    init {
        viewModelScope.launch {
            checkRollover()
            
            // Initial health data sync
            if (healthConnectManager.isAvailable()) {
                healthConnectManager.readTodayData()
            }
            
            // Mark activity timestamp after a small delay to allow coach tip generation
            kotlinx.coroutines.delay(1000)
            userPreferencesRepository.setLastActivityTimestamp(System.currentTimeMillis())
        }
        
        // Sync steps to DataStore for Widget
        viewModelScope.launch {
            healthConnectManager.healthData.collect { userPreferencesRepository.setLastKnownSteps(it.steps.toInt()) }
        }
        
        // Smart Coach: Generate initial tip and refresh periodically
        viewModelScope.launch {
            // Wait for dashboard state to be populated
            kotlinx.coroutines.delay(2000)
            refreshSmartCoachTip()
        }
    }
    
    /**
     * Refresh the smart coach tip by analyzing current app data.
     * Called on init and can be triggered when significant data changes.
     */
    private suspend fun refreshSmartCoachTip() {
        try {
            val state = dashboardState.value
            val tip = smartCoachService.getCurrentOrGenerateTip(
                caloriesConsumed = state.consumedCalories,
                caloriesGoal = state.goalCalories,
                proteinConsumed = state.proteinG,
                proteinGoal = state.proteinGoal,
                carbsConsumed = state.carbsG,
                carbsGoal = state.carbsGoal,
                fatsConsumed = state.fatsG,
                fatsGoal = state.fatsGoal,
                waterConsumed = state.waterConsumed,
                steps = state.steps,
                stepsGoal = state.stepsGoal,
                caloriesBurned = state.caloriesBurned,
                currentStreak = state.currentStreak,
                healthScore = state.healthScore
            )
            _smartCoachTip.value = tip
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun checkRollover() {
        val isRolloverEnabled = userPreferencesRepository.rolloverExtraCalories.first()
        if (!isRolloverEnabled) return

        val lastRolloverDate = userPreferencesRepository.lastRolloverDate.first()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayMillis = today.timeInMillis

        // If we haven't calculated rollover for today yet
        if (lastRolloverDate < todayMillis) {
            // Calculate for Yesterday
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            val yesterdayDateString = dateFormat.format(yesterday.time)
            
            // Get yesterday's meals
            val yesterdayMeals = mealRepository.getMealsForDate(yesterdayDateString).first()
            val completedMeals = yesterdayMeals.filter { it.analysisStatus == AnalysisStatus.COMPLETED }
            val consumed = completedMeals.sumOf { it.calories }
            
            // Get usage goal (using current goal as proxy for yesterday's goal)
            val goal = userPreferencesRepository.recommendedCalories.first()
            
            val remaining = (goal - consumed).coerceAtLeast(0)
            val rolloverAmount = remaining.coerceIn(0, 200) // Max 200 as per requirement
            
            // Save rollover amount and mark today as done
            userPreferencesRepository.setRolloverCaloriesAmount(rolloverAmount)
            userPreferencesRepository.setLastRolloverDate(todayMillis)
        }
    }

    // Meals for selected date (reactive to date changes)
    private val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    
    val meals = selectedDate.flatMapLatest { date ->
        val dateString = dateFormat.format(date.time)
        mealRepository.getMealsForDate(dateString)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Exercises for selected date
    val exercises = selectedDate.flatMapLatest { date ->
        val dateString = dateFormat.format(date.time)
        exerciseRepository.getExercisesForDate(dateString)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Manual exercise calories for the selected date
    val manualExerciseCalories = selectedDate.flatMapLatest { date ->
        val dateString = dateFormat.format(date.time)
        exerciseRepository.getTotalCaloriesBurnedForDate(dateString)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    // Recent uploads for the "Recently uploaded" section
    val recentUploads = mealRepository.getRecentUploads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Health Connect data
    val healthData = healthConnectManager.healthData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HealthData())

    // Use typed combine (5 flows max with explicit type parameters)
    private val coreState = combine(
        meals, dailyGoal, selectedDate, waterConsumed, healthData
    ) { currentMeals, goal, date, water, health ->
        CoreState(currentMeals, goal, date, water, health)
    }
    
    // Get all meals for streak calculation
    private val allMeals = mealRepository.getAllMeals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val dashboardState = combine(
        coreState, 
        recentUploads, 
        stepsGoal, 
        rolloverEnabled, 
        rolloverAmount,
        userPreferencesRepository.waterServingSize,
        manualExerciseCalories,
        exercises
    ) { flows ->
        val core = flows[0] as CoreState
        val recent = flows[1] as List<MealEntity>
        val stepGoal = flows[2] as Int
        val rolloverOn = flows[3] as Boolean
        val rolloverAmt = flows[4] as Int
        val servingSize = flows[5] as Int
        val exerciseCals = flows[6] as Int
        @Suppress("UNCHECKED_CAST")
        val exercisesList = flows[7] as List<com.example.calview.core.data.local.ExerciseEntity>
        
        // Only count completed meals for calorie totals
        val completedMeals = core.meals.filter { 
            it.analysisStatus == AnalysisStatus.COMPLETED 
        }
        val consumed = completedMeals.sumOf { it.calories }
        val protein = completedMeals.sumOf { it.protein }
        val carbs = completedMeals.sumOf { it.carbs }
        val fats = completedMeals.sumOf { it.fats }
        val fiber = completedMeals.sumOf { it.fiber }
        val sugar = completedMeals.sumOf { it.sugar }
        val sodium = completedMeals.sumOf { it.sodium }
        
        // Calculate effective rollover (0 if disabled)
        val effectiveRollover = if (rolloverOn) rolloverAmt else 0
        
        // Store intermediate state
        IntermediateState(
            consumed = consumed,
            effectiveRollover = effectiveRollover,
            core = core,
            recent = recent,
            stepGoal = stepGoal,
            rolloverOn = rolloverOn,
            protein = protein,
            carbs = carbs,
            fats = fats,
            fiber = fiber,
            sugar = sugar,
            sodium = sodium,
            waterServingSize = servingSize,
            manualExerciseCalories = exerciseCals,
            exercises = exercisesList
        )
    }.combine(waterReminderRepository.observeSettings()) { intermediate, waterSettings ->
         Pair(intermediate, waterSettings)
    }.combine(addCaloriesBackEnabled) { (intermediate, waterSettings), addCaloriesOn ->
        Triple(intermediate, waterSettings, addCaloriesOn)
    }.combine(storedProteinGoal) { (intermediate, waterSettings, addCaloriesOn), proteinG ->
        MacroState(intermediate, addCaloriesOn, proteinG, 0, waterSettings) // Temp struct
    }.combine(storedCarbsGoal) { macroState, carbsG ->
        // Repackage to include carbs
        macroState.copy(carbsGoal = carbsG)
    }.combine(storedFatsGoal) { macroState, fatsG ->
        val intermediate = macroState.intermediate
        val addCaloriesOn = macroState.addCaloriesOn
        val waterSettings = macroState.waterSettings
        
        // Calculate burned calories: Health Connect + Manual Exercise logs
        val healthConnectBurned = intermediate.core.health.caloriesBurned.toInt()
        val manualBurned = intermediate.manualExerciseCalories
        val totalBurnedCalories = healthConnectBurned + manualBurned
        val effectiveBurned = if (addCaloriesOn) totalBurnedCalories else 0
        
        // Use stored macro goals or calculate from calories if not set
        val calorieGoal = intermediate.core.goal
        val proteinGoal = if (macroState.proteinGoal > 0) macroState.proteinGoal else (calorieGoal * 0.25 / 4).toInt()
        val carbsGoal = if (macroState.carbsGoal > 0) macroState.carbsGoal else (calorieGoal * 0.50 / 4).toInt()
        val fatsGoalFinal = if (fatsG > 0) fatsG else (calorieGoal * 0.25 / 9).toInt()
        
        val coachTip = coachMessageGenerator.generateMacroTip(
            proteinRemaining = proteinGoal - intermediate.protein,
            carbsRemaining = carbsGoal - intermediate.carbs,
            fatsRemaining = fatsGoalFinal - intermediate.fats,
            caloriesRemaining = intermediate.core.goal + intermediate.effectiveRollover + effectiveBurned - intermediate.consumed
        )

        DashboardState(
            consumedCalories = intermediate.consumed,
            remainingCalories = (intermediate.core.goal + intermediate.effectiveRollover + effectiveBurned - intermediate.consumed).coerceAtLeast(0),
            goalCalories = intermediate.core.goal,
            proteinG = intermediate.protein,
            carbsG = intermediate.carbs,
            fatsG = intermediate.fats,
            fiberG = intermediate.fiber,
            sugarG = intermediate.sugar,
            sodiumG = intermediate.sodium,
            proteinGoal = proteinGoal,
            carbsGoal = carbsGoal,
            fatsGoal = fatsGoalFinal,
            meals = intermediate.core.meals,
            selectedDate = intermediate.core.date,
            waterConsumed = intermediate.core.water,
            steps = intermediate.core.health.steps,
            caloriesBurned = totalBurnedCalories,
            isHealthConnected = intermediate.core.health.isConnected,
            isHealthAvailable = intermediate.core.health.isAvailable,
            recentUploads = intermediate.recent,
            stepsGoal = intermediate.stepGoal,
            rolloverCaloriesEnabled = intermediate.rolloverOn,
            rolloverCaloriesAmount = intermediate.effectiveRollover,
            addCaloriesBackEnabled = addCaloriesOn,
            burnedCaloriesAdded = effectiveBurned,
            coachTip = coachTip,
            // Water Reminder Settings
            waterReminderEnabled = waterSettings?.enabled ?: false,
            waterReminderIntervalHours = waterSettings?.intervalHours ?: 2,
            waterReminderStartHour = waterSettings?.startHour ?: 8,
            waterReminderEndHour = waterSettings?.endHour ?: 22,
            waterReminderDailyGoalMl = waterSettings?.dailyGoalMl ?: 2500,
            waterServingSize = intermediate.waterServingSize,
            // Exercise tracking
            exercises = intermediate.exercises,
            manualExerciseCalories = intermediate.manualExerciseCalories
        )
    }.combine(userPreferencesRepository.hasSeenDashboardWalkthrough) { state, hasSeen ->
        state.copy(hasSeenWalkthrough = hasSeen)
    }.flatMapLatest { state ->
        allMeals.flatMapLatest { allMealsList ->
            val mealDates = allMealsList.filter { it.analysisStatus == AnalysisStatus.COMPLETED }
                .map { 
                    java.time.Instant.ofEpochMilli(it.timestamp)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate() 
                }
                .distinct()

            streakFreezeRepository.getStreakData(mealDates).map { streak ->
                // Calculate streak lost detection
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val yesterdayStart = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val mealsToday = allMealsList.filter { 
                    it.timestamp >= todayStart && 
                    it.analysisStatus == AnalysisStatus.COMPLETED 
                }
                
                // Streak is lost if it's 0 AND there were meals before yesterday AND no meals today
                val hasMealsBefore = allMealsList.any { it.timestamp < yesterdayStart }
                val streakLost = streak == 0 && hasMealsBefore && mealsToday.isEmpty()

                // Calculate completed days for the week (Sunday to Saturday)
                val weekStartForCompleted = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val completedDays = (0..6).map { dayOffset ->
                    val dayStart = (weekStartForCompleted.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
                    dayStart.set(Calendar.HOUR_OF_DAY, 0)
                    val dayEnd = (dayStart.clone() as Calendar)
                    dayEnd.set(Calendar.HOUR_OF_DAY, 23)
                    dayEnd.set(Calendar.MINUTE, 59)
                    dayEnd.set(Calendar.SECOND, 59)
                    dayEnd.set(Calendar.MILLISECOND, 999)
                    
                    allMealsList.any { 
                        it.timestamp in dayStart.timeInMillis..dayEnd.timeInMillis &&
                        it.analysisStatus == AnalysisStatus.COMPLETED
                    }
                }
                
                // Calculate health score based on nutrition data
                val healthScore = calculateHealthScore(
                    caloriesConsumed = state.consumedCalories,
                    caloriesGoal = state.goalCalories,
                    proteinConsumed = state.proteinG,
                    proteinGoal = state.proteinGoal,
                    carbsConsumed = state.carbsG,
                    carbsGoal = state.carbsGoal,
                    fatsConsumed = state.fatsG,
                    fatsGoal = state.fatsGoal,
                    waterConsumed = state.waterConsumed,
                    steps = state.steps.toInt(),
                    stepsGoal = state.stepsGoal
                )
                
                // Generate dynamic recommendation
                val healthRecommendation = generateHealthRecommendation(
                    caloriesConsumed = state.consumedCalories,
                    caloriesGoal = state.goalCalories,
                    proteinConsumed = state.proteinG,
                    proteinGoal = state.proteinGoal,
                    carbsConsumed = state.carbsG,
                    carbsGoal = state.carbsGoal,
                    fatsConsumed = state.fatsG,
                    fatsGoal = state.fatsGoal
                )
                
                val finalState = state.copy(
                    streakLost = streakLost,
                    currentStreak = streak,
                    completedDays = completedDays,
                    healthScore = healthScore,
                    healthRecommendation = healthRecommendation,
                    allMealDates = allMealsList.filter { it.analysisStatus == AnalysisStatus.COMPLETED }.map { it.timestamp }
                )

                // Generate health score tip using the persisted last activity time
                val coachTip = coachMessageGenerator.generateHealthScoreTip(
                    currentScore = finalState.healthScore,
                    previousScore = 0, // In future turn we could persist previous score
                    lastActivityTimestamp = lastActivityTimestamp.value
                )

                // If health coach has a high-priority tip (like welcome back), use it
                if (coachTip.emoji == "👋") {
                   finalState.copy(coachTip = coachTip)
                } else {
                   finalState
                }
            }
        }
    }.scan(DashboardState()) { previous, current ->
        // Detect health score change
        if (previous.healthScore != 0 && current.healthScore != previous.healthScore) {
             val tip = coachMessageGenerator.generateHealthScoreTip(
                 currentScore = current.healthScore,
                 previousScore = previous.healthScore,
                 lastActivityTimestamp = lastActivityTimestamp.value
             )
             current.copy(coachTip = tip)
        } else {
             current
        }
    }.combine(smartCoachTip) { state, smartTip ->
        // Use smart coach tip when available, otherwise use the default tip
        if (smartTip != null) {
            state.copy(coachTip = smartTip)
        } else {
            state
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())


    
    fun selectDate(date: Calendar) {
        _selectedDate.value = date
        // Fetch health data for the selected date
        viewModelScope.launch {
            val localDate = java.time.LocalDate.of(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1, // Calendar months are 0-indexed
                date.get(Calendar.DAY_OF_MONTH)
            )
            // Update shared date holder so Progress screen can also use it
            selectedDateHolder.setDate(localDate)
            healthConnectManager.readDataForDate(localDate)
        }
    }
    
    fun addWater(amount: Int? = null) {
        viewModelScope.launch {
            val servingSize = amount ?: userPreferencesRepository.waterServingSize.first()
            val dateString = dateFormat.format(_selectedDate.value.time)
            val currentWater = waterConsumed.value
            val newValue = currentWater + servingSize
            
            // Update daily log for historical tracking
            dailyLogRepository.updateWater(dateString, newValue)
            
            // If today, also update preferences for the widget
            val todayDateString = dateFormat.format(Calendar.getInstance().time)
            if (dateString == todayDateString) {
                userPreferencesRepository.setWaterConsumed(newValue, System.currentTimeMillis())
                updateWidget()
            }
        }
    }
    
    fun refreshHealthData() {
        viewModelScope.launch {
            if (healthConnectManager.isAvailable()) {
                healthConnectManager.readTodayData()
            }
            // Also refresh the smart coach tip
            kotlinx.coroutines.delay(500) // Wait for data to update
            refreshSmartCoachTip()
        }
    }
    
    fun removeWater(amount: Int? = null) {
        viewModelScope.launch {
            val servingSize = amount ?: userPreferencesRepository.waterServingSize.first()
            val dateString = dateFormat.format(_selectedDate.value.time)
            val currentWater = waterConsumed.value
            val newValue = (currentWater - servingSize).coerceAtLeast(0)
            
            // Update daily log for historical tracking
            dailyLogRepository.updateWater(dateString, newValue)
            
            // If today, also update preferences for the widget
            val todayDateString = dateFormat.format(Calendar.getInstance().time)
            if (dateString == todayDateString) {
                userPreferencesRepository.setWaterConsumed(newValue, System.currentTimeMillis())
                updateWidget()
            }
        }
    }
    
    // Helper to trigger widget update without circular dependency
    private fun updateWidget() {
        viewModelScope.launch {
            try {
                // First sync data to SharedPreferences for widget access
                userPreferencesRepository.syncWidgetData()
                
                // Then trigger widget refresh
                val widgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                val widgetComponent = android.content.ComponentName(context.packageName, "com.example.calview.widget.CaloriesWidgetProvider")
                val widgetIds = widgetManager.getAppWidgetIds(widgetComponent)
                
                if (widgetIds.isNotEmpty()) {
                    val updateIntent = android.content.Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    updateIntent.component = widgetComponent
                    updateIntent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                    context.sendBroadcast(updateIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun onHealthPermissionsGranted() {
        viewModelScope.launch {
            healthConnectManager.onPermissionsGranted()
        }
    }
    

    
    /**
     * Update a meal (e.g., after editing in FoodDetailScreen)
     */
    fun updateMeal(meal: MealEntity) {
        viewModelScope.launch {
            try {
                mealRepository.updateMeal(meal)
                updateWidget()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Delete a meal
     */
    fun deleteMeal(meal: MealEntity) {
        viewModelScope.launch {
            try {
                mealRepository.deleteMeal(meal)
                updateWidget()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Recalibrate a meal using AI with additional ingredients
     * @param meal Original meal entity
     * @param additionalIngredients List of ingredients user added that AI missed
     * @param servingCount Number of servings
     * @return Updated MealEntity with recalculated nutrition, or null if failed
     */
    /**
     * Recalibrate a meal using AI with additional ingredients that were missed.
     * @param meal Original meal entity
     * @param additionalIngredients List of ingredients user added that AI missed
     * @param servingCount Number of servings
     * @return Updated MealEntity with recalculated nutrition, or null if failed
     */
    suspend fun recalibrateMeal(
        meal: MealEntity,
        additionalIngredients: List<String>,
        servingCount: Int
    ): MealEntity? {
        return try {
            // If no image path, just multiply by serving count
            if (meal.imagePath == null) {
                return meal.copy(
                    calories = meal.calories * servingCount,
                    protein = meal.protein * servingCount,
                    carbs = meal.carbs * servingCount,
                    fats = meal.fats * servingCount,
                    fiber = meal.fiber * servingCount,
                    sugar = meal.sugar * servingCount,
                    sodium = meal.sodium * servingCount
                )
            }
            
            // Reload the image from path
            val imageFile = File(meal.imagePath!!)
            if (!imageFile.exists()) {
                return null
            }
            
            val bitmap = BitmapFactory.decodeFile(meal.imagePath) ?: return null
            
            // Re-analyze with AI, including any additional ingredients the user specified
            val result = foodAnalysisService.analyzeFoodImageWithIngredients(
                bitmap = bitmap,
                additionalIngredients = additionalIngredients
            )
            bitmap.recycle()
            
            result.getOrNull()?.let { response ->
                // Use the AI's new analysis (which includes additional ingredients) multiplied by serving count
                val total = response.total
                meal.copy(
                    calories = total.calories * servingCount,
                    protein = total.protein * servingCount,
                    carbs = total.carbs * servingCount,
                    fats = total.fats * servingCount,
                    fiber = (total.fiber ?: meal.fiber) * servingCount,
                    sugar = (total.sugar ?: meal.sugar) * servingCount,
                    sodium = (total.sodium ?: meal.sodium) * servingCount,
                    healthInsight = response.health_insight
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Calculate health score (0-10) based on nutrition adherence, activity, and hydration.
     */
    private fun calculateHealthScore(
        caloriesConsumed: Int,
        caloriesGoal: Int,
        proteinConsumed: Int,
        proteinGoal: Int,
        carbsConsumed: Int,
        carbsGoal: Int,
        fatsConsumed: Int,
        fatsGoal: Int,
        waterConsumed: Int,
        steps: Int,
        stepsGoal: Int
    ): Int {
        if (caloriesGoal <= 0) return 0
        
        var score = 0f
        
        // Calorie adherence (max 3 points)
        val calorieRatio = caloriesConsumed.toFloat() / caloriesGoal
        score += when {
            calorieRatio in 0.85f..1.15f -> 3f  // Within 15% of goal
            calorieRatio in 0.70f..1.30f -> 2f  // Within 30% of goal
            calorieRatio in 0.50f..1.50f -> 1f  // Within 50% of goal
            else -> 0f
        }
        
        // Macro balance (max 3 points - 1 per macro)
        if (proteinGoal > 0) {
            val proteinRatio = proteinConsumed.toFloat() / proteinGoal
            if (proteinRatio in 0.70f..1.30f) score += 1f
        }
        if (carbsGoal > 0) {
            val carbsRatio = carbsConsumed.toFloat() / carbsGoal
            if (carbsRatio in 0.70f..1.30f) score += 1f
        }
        if (fatsGoal > 0) {
            val fatsRatio = fatsConsumed.toFloat() / fatsGoal
            if (fatsRatio in 0.70f..1.30f) score += 1f
        }
        
        // Hydration (max 2 points) - 8 glasses target
        val waterTarget = 8
        val glasses = waterConsumed / 8
        score += when {
            glasses >= waterTarget -> 2f
            glasses >= waterTarget / 2 -> 1f
            else -> 0f
        }
        
        // Activity (max 2 points)
        if (stepsGoal > 0) {
            val stepsRatio = steps.toFloat() / stepsGoal
            score += when {
                stepsRatio >= 1f -> 2f
                stepsRatio >= 0.5f -> 1f
                else -> 0f
            }
        }
        
        return score.coerceIn(0f, 10f).toInt()
    }
    
    /**
     * Generate a personalized health recommendation based on current nutrition status.
     */
    private fun generateHealthRecommendation(
        caloriesConsumed: Int,
        caloriesGoal: Int,
        proteinConsumed: Int,
        proteinGoal: Int,
        carbsConsumed: Int,
        carbsGoal: Int,
        fatsConsumed: Int,
        fatsGoal: Int
    ): String {
        if (caloriesGoal <= 0 || caloriesConsumed == 0) {
            return "Track your meals to get personalized health insights."
        }
        
        val issues = mutableListOf<String>()
        val onTrack = mutableListOf<String>()
        
        // Check calories
        val caloriePercent = (caloriesConsumed.toFloat() / caloriesGoal * 100).toInt()
        when {
            caloriePercent < 70 -> issues.add("low in calories")
            caloriePercent > 130 -> issues.add("over your calorie goal")
            else -> onTrack.add("calories")
        }
        
        // Check protein
        if (proteinGoal > 0) {
            val proteinPercent = (proteinConsumed.toFloat() / proteinGoal * 100).toInt()
            when {
                proteinPercent < 70 -> issues.add("low in protein")
                proteinPercent > 130 -> issues.add("high in protein")
                else -> onTrack.add("protein")
            }
        }
        
        // Check carbs
        if (carbsGoal > 0) {
            val carbsPercent = (carbsConsumed.toFloat() / carbsGoal * 100).toInt()
            when {
                carbsPercent < 70 -> issues.add("low in carbs")
                carbsPercent > 130 -> issues.add("high in carbs")
                else -> onTrack.add("carbs")
            }
        }
        
        // Check fats
        if (fatsGoal > 0) {
            val fatsPercent = (fatsConsumed.toFloat() / fatsGoal * 100).toInt()
            when {
                fatsPercent < 70 -> issues.add("low in fats")
                fatsPercent > 130 -> issues.add("high in fats")
                else -> onTrack.add("fats")
            }
        }
        
        // Build recommendation
        return buildString {
            if (onTrack.isNotEmpty()) {
                append(onTrack.joinToString(" and ").replaceFirstChar { it.uppercase() })
                append(" are on track. ")
            }
            if (issues.isNotEmpty()) {
                append("You're ")
                append(issues.joinToString(" and "))
                append(", which can ")
                // Add specific advice based on issues
                when {
                    issues.any { it.contains("low in calories") } && issues.any { it.contains("protein") } ->
                        append("slow weight loss and impact muscle retention.")
                    issues.any { it.contains("over your calorie") } ->
                        append("lead to weight gain over time.")
                    issues.any { it.contains("low in protein") } ->
                        append("affect muscle recovery and satiety.")
                    issues.any { it.contains("high in fats") } ->
                        append("increase calorie intake quickly.")
                    else ->
                        append("affect your energy and progress.")
                }
            } else if (onTrack.size >= 3) {
                append("Great job! Keep up the balanced nutrition.")
            }
        }.ifEmpty { "Keep tracking to see your nutrition insights." }
    }

    // Water Reminder Updates
    fun setWaterReminderEnabled(enabled: Boolean) {
        viewModelScope.launch { waterReminderRepository.setEnabled(enabled) }
    }
    
    fun setWaterReminderInterval(hours: Int) {
        viewModelScope.launch { waterReminderRepository.setInterval(hours) }
    }
    
    fun setWaterReminderDailyGoal(ml: Int) {
        viewModelScope.launch { waterReminderRepository.setDailyGoal(ml) }
    }

    fun setWaterServingSize(flOz: Int) {
        viewModelScope.launch { userPreferencesRepository.setWaterServingSize(flOz) }
    }
}

// Internal data class for intermediate combine state
private data class CoreState(
    val meals: List<MealEntity>,
    val goal: Int,
    val date: Calendar,
    val water: Int,
    val health: HealthData
)

// Intermediate state for two-stage combine
private data class IntermediateState(
    val consumed: Int,
    val effectiveRollover: Int,
    val core: CoreState,
    val recent: List<MealEntity>,
    val stepGoal: Int,
    val rolloverOn: Boolean,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val fiber: Int,
    val sugar: Int,
    val sodium: Int,
    val waterServingSize: Int,
    val manualExerciseCalories: Int = 0,
    val exercises: List<com.example.calview.core.data.local.ExerciseEntity> = emptyList()
)

private data class MacroState(
    val intermediate: IntermediateState,
    val addCaloriesOn: Boolean,
    val proteinGoal: Int,
    val carbsGoal: Int,
    val waterSettings: com.example.calview.core.data.local.WaterReminderSettingsEntity?
)



data class DashboardState(
    val consumedCalories: Int = 0,
    val remainingCalories: Int = 0,
    val goalCalories: Int = 2000,
    val proteinG: Int = 0,
    val carbsG: Int = 0,
    val fatsG: Int = 0,
    val fiberG: Int = 0,
    val sugarG: Int = 0,
    val sodiumG: Int = 0,
    // Macro goals calculated from calorie goal
    val proteinGoal: Int = 125, // Default for 2000 cal (25% protein)
    val carbsGoal: Int = 250,   // Default for 2000 cal (50% carbs)
    val fatsGoal: Int = 56,     // Default for 2000 cal (25% fats)
    val fiberGoal: Int = 38,    // Daily recommended fiber
    val sugarGoal: Int = 64,    // Approx 200-400 cal from sugar
    val sodiumGoal: Int = 2300, // mg, FDA recommended limit
    val meals: List<MealEntity> = emptyList(),
    val selectedDate: Calendar = Calendar.getInstance(),
    val waterConsumed: Int = 0,
    val steps: Long = 0,
    val caloriesBurned: Int = 0,
    val isHealthConnected: Boolean = false,
    val isHealthAvailable: Boolean = false,
    val recentUploads: List<MealEntity> = emptyList(),
    val stepsGoal: Int = 10000,
    val rolloverCaloriesEnabled: Boolean = false,
    val rolloverCaloriesAmount: Int = 0,
    // Add calories burned back setting
    val addCaloriesBackEnabled: Boolean = false,
    val burnedCaloriesAdded: Int = 0,
    // Streak tracking
    val streakLost: Boolean = false,
    val currentStreak: Int = 0,
    val completedDays: List<Boolean> = listOf(false, false, false, false, false, false, false),
    // Health score and AI recommendation
    val healthScore: Int = 0,
    val healthRecommendation: String = "Track your meals to get personalized health insights.",
    val coachTip: com.example.calview.core.data.coach.CoachMessageGenerator.CoachTip? = null,
    // Water Reminder Settings
    val waterReminderEnabled: Boolean = false,
    val waterReminderIntervalHours: Int = 2,
    val waterReminderStartHour: Int = 8,
    val waterReminderEndHour: Int = 22,
    val waterReminderDailyGoalMl: Int = 2500,
    val waterServingSize: Int = 8,
    val hasSeenWalkthrough: Boolean = true,
    val allMealDates: List<Long> = emptyList(),
    // Exercise tracking
    val exercises: List<com.example.calview.core.data.local.ExerciseEntity> = emptyList(),
    val manualExerciseCalories: Int = 0
)


