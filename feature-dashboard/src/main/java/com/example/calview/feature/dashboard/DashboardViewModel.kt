package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.local.AnalysisStatus
import com.example.calview.core.data.local.MealEntity
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.health.HealthConnectManager
import com.example.calview.core.data.health.HealthData
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
    
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate = _selectedDate.asStateFlow()
    
    // Water consumption (persisted)
    private val _waterConsumed = MutableStateFlow(0)
    val waterConsumed = _waterConsumed.asStateFlow()
    
    // Observe repository for water updates and date checks
    init {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.waterConsumed,
                userPreferencesRepository.waterDate
            ) { consumed, timestamp ->
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val recordParams = Calendar.getInstance().apply { timeInMillis = timestamp }
                val recordStartOfDay = recordParams.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                if (today == recordStartOfDay) {
                    _waterConsumed.value = consumed
                } else {
                    // Reset if different day
                    _waterConsumed.value = 0
                    if (consumed > 0) {
                        // Update repo to clean state for new day
                        userPreferencesRepository.setWaterConsumed(0, System.currentTimeMillis())
                    }
                }
            }.collect()
        }
    }

    // Meals for selected date (reactive to date changes)
    private val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    
    val meals = selectedDate.flatMapLatest { date ->
        val dateString = dateFormat.format(date.time)
        mealRepository.getMealsForDate(dateString)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
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
    
    val dashboardState = combine(coreState, recentUploads, stepsGoal, rolloverEnabled, rolloverAmount) { core, recent, stepGoal, rolloverOn, rolloverAmt ->
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
            sodium = sodium
        )
    }.combine(addCaloriesBackEnabled) { intermediate, addCaloriesOn ->
        Pair(intermediate, addCaloriesOn)
    }.combine(storedProteinGoal) { (intermediate, addCaloriesOn), proteinG ->
        Triple(intermediate, addCaloriesOn, proteinG)
    }.combine(storedCarbsGoal) { (intermediate, addCaloriesOn, proteinG), carbsG ->
        MacroState(intermediate, addCaloriesOn, proteinG, carbsG)
    }.combine(storedFatsGoal) { macroState, fatsG ->
        val intermediate = macroState.intermediate
        val addCaloriesOn = macroState.addCaloriesOn
        
        // Calculate burned calories added to goal (only if setting is ON)
        val burnedCalories = intermediate.core.health.caloriesBurned.toInt()
        val effectiveBurned = if (addCaloriesOn) burnedCalories else 0
        
        // Use stored macro goals or calculate from calories if not set
        val calorieGoal = intermediate.core.goal
        val proteinGoal = if (macroState.proteinGoal > 0) macroState.proteinGoal else (calorieGoal * 0.25 / 4).toInt()
        val carbsGoal = if (macroState.carbsGoal > 0) macroState.carbsGoal else (calorieGoal * 0.50 / 4).toInt()
        val fatsGoalFinal = if (fatsG > 0) fatsG else (calorieGoal * 0.25 / 9).toInt()
        
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
            caloriesBurned = burnedCalories,
            isHealthConnected = intermediate.core.health.isConnected,
            isHealthAvailable = intermediate.core.health.isAvailable,
            recentUploads = intermediate.recent,
            stepsGoal = intermediate.stepGoal,
            rolloverCaloriesEnabled = intermediate.rolloverOn,
            rolloverCaloriesAmount = intermediate.effectiveRollover,
            addCaloriesBackEnabled = addCaloriesOn,
            burnedCaloriesAdded = effectiveBurned
        )
    }.combine(allMeals) { state, allMealsList ->
        // Calculate streak detection
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        
        // Check if user logged any meal yesterday
        val yesterdayStart = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val yesterdayEnd = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val mealsYesterday = allMealsList.filter { 
            it.timestamp in yesterdayStart..yesterdayEnd && 
            it.analysisStatus == AnalysisStatus.COMPLETED 
        }
        val mealsToday = allMealsList.filter { 
            it.timestamp >= todayStart && 
            it.analysisStatus == AnalysisStatus.COMPLETED 
        }
        
        // Streak is lost if no meals logged yesterday AND there were meals before yesterday
        val hasMealsBefore = allMealsList.any { it.timestamp < yesterdayStart }
        val streakLost = mealsYesterday.isEmpty() && hasMealsBefore && mealsToday.isEmpty()
        
        // Calculate current streak
        var streak = 0
        var checkDate = Calendar.getInstance()
        while (true) {
            val dayStart = checkDate.clone() as Calendar
            dayStart.set(Calendar.HOUR_OF_DAY, 0)
            dayStart.set(Calendar.MINUTE, 0)
            dayStart.set(Calendar.SECOND, 0)
            dayStart.set(Calendar.MILLISECOND, 0)
            
            val dayEnd = checkDate.clone() as Calendar
            dayEnd.set(Calendar.HOUR_OF_DAY, 23)
            dayEnd.set(Calendar.MINUTE, 59)
            dayEnd.set(Calendar.SECOND, 59)
            dayEnd.set(Calendar.MILLISECOND, 999)
            
            val mealsOnDay = allMealsList.filter { 
                it.timestamp in dayStart.timeInMillis..dayEnd.timeInMillis &&
                it.analysisStatus == AnalysisStatus.COMPLETED
            }
            
            if (mealsOnDay.isNotEmpty()) {
                streak++
                checkDate.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        
        // Calculate completed days for the week (Sunday to Saturday)
        val weekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val completedDays = (0..6).map { dayOffset ->
            val dayStart = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
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
        
        state.copy(
            streakLost = streakLost,
            currentStreak = streak,
            completedDays = completedDays
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    
    init {
        // Check Health Connect availability and read data on init
        viewModelScope.launch {
            if (healthConnectManager.isAvailable()) {
                healthConnectManager.readTodayData()
            }
        }
        
        // Sync steps to DataStore for Widget
        viewModelScope.launch {
            healthConnectManager.healthData.collect { data ->
                if (data.steps > 0) {
                    userPreferencesRepository.setLastKnownSteps(data.steps.toInt())
                    userPreferencesRepository.setLastKnownSteps(data.steps.toInt())
                    updateWidget()
                }
            }
        }
    }
    
    fun selectDate(date: Calendar) {
        _selectedDate.value = date
    }
    
    fun addWater(amount: Int = 1) {
        val newValue = _waterConsumed.value + amount
        viewModelScope.launch {
            userPreferencesRepository.setWaterConsumed(newValue, System.currentTimeMillis())
            _waterConsumed.value = newValue // Updates immediately for UI responsiveness
            updateWidget()
        }
    }
    
    fun removeWater(amount: Int = 1) {
        val newValue = (_waterConsumed.value - amount).coerceAtLeast(0)
        viewModelScope.launch {
            userPreferencesRepository.setWaterConsumed(newValue, System.currentTimeMillis())
            _waterConsumed.value = newValue
            updateWidget()
        }
    }
    
    // Helper to trigger widget update without circular dependency
    private fun updateWidget() {
        try {
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
    
    fun onHealthPermissionsGranted() {
        viewModelScope.launch {
            healthConnectManager.onPermissionsGranted()
        }
    }
    
    fun refreshHealthData() {
        viewModelScope.launch {
            healthConnectManager.readTodayData()
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
            val imageFile = File(meal.imagePath)
            if (!imageFile.exists()) {
                return null
            }
            
            val bitmap = BitmapFactory.decodeFile(meal.imagePath) ?: return null
            
            // Re-analyze with AI
            val result = foodAnalysisService.analyzeFoodImage(bitmap)
            bitmap.recycle()
            
            result.getOrNull()?.let { response ->
                // Calculate adjustments for additional ingredients if needed
                // For now, just use the AI's new analysis multiplied by serving count
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
    val sodium: Int
)

// Helper state for macro goals combine
private data class MacroState(
    val intermediate: IntermediateState,
    val addCaloriesOn: Boolean,
    val proteinGoal: Int,
    val carbsGoal: Int
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
    val completedDays: List<Boolean> = listOf(false, false, false, false, false, false, false)
)
