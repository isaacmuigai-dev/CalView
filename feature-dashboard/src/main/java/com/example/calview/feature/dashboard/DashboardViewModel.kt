package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.local.AnalysisStatus
import com.example.calview.core.data.local.MealEntity
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.health.HealthConnectManager
import com.example.calview.core.data.health.HealthData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    val healthConnectManager: HealthConnectManager
) : ViewModel() {

    // Load goal from preferences, defaulting to 2000 if not set or 0
    val dailyGoal = userPreferencesRepository.recommendedCalories
        .map { if (it > 0) it else 2000 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)
    
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate = _selectedDate.asStateFlow()
    
    private val _waterConsumed = MutableStateFlow(0)
    val waterConsumed = _waterConsumed.asStateFlow()

    val meals = mealRepository.getMealsForToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
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
    
    val dashboardState = combine(coreState, recentUploads) { core, recent ->
        // Only count completed meals for calorie totals
        val completedMeals = core.meals.filter { 
            it.analysisStatus == AnalysisStatus.COMPLETED 
        }
        val consumed = completedMeals.sumOf { it.calories }
        val protein = completedMeals.sumOf { it.protein }
        val carbs = completedMeals.sumOf { it.carbs }
        val fats = completedMeals.sumOf { it.fats }
        
        DashboardState(
            consumedCalories = consumed,
            remainingCalories = (core.goal - consumed).coerceAtLeast(0),
            goalCalories = core.goal,
            proteinG = protein,
            carbsG = carbs,
            fatsG = fats,
            meals = core.meals,
            selectedDate = core.date,
            waterConsumed = core.water,
            steps = core.health.steps,
            caloriesBurned = core.health.caloriesBurned.toInt(),
            isHealthConnected = core.health.isConnected,
            isHealthAvailable = core.health.isAvailable,
            recentUploads = recent
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())
    
    init {
        // Check Health Connect availability and read data on init
        viewModelScope.launch {
            if (healthConnectManager.isAvailable()) {
                healthConnectManager.readTodayData()
            }
        }
    }
    
    fun selectDate(date: Calendar) {
        _selectedDate.value = date
    }
    
    fun addWater(amount: Int = 8) {
        _waterConsumed.value += amount
    }
    
    fun removeWater(amount: Int = 8) {
        _waterConsumed.value = (_waterConsumed.value - amount).coerceAtLeast(0)
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
}

// Internal data class for intermediate combine state
private data class CoreState(
    val meals: List<MealEntity>,
    val goal: Int,
    val date: Calendar,
    val water: Int,
    val health: HealthData
)

data class DashboardState(
    val consumedCalories: Int = 0,
    val remainingCalories: Int = 0,
    val goalCalories: Int = 2000,
    val proteinG: Int = 0,
    val carbsG: Int = 0,
    val fatsG: Int = 0,
    val meals: List<MealEntity> = emptyList(),
    val selectedDate: Calendar = Calendar.getInstance(),
    val waterConsumed: Int = 0,
    val steps: Long = 0,
    val caloriesBurned: Int = 0,
    val isHealthConnected: Boolean = false,
    val isHealthAvailable: Boolean = false,
    val recentUploads: List<MealEntity> = emptyList()
)
