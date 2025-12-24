package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.local.MealEntity
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val userPreferencesRepository: UserPreferencesRepository
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

    val dashboardState = combine(meals, dailyGoal, selectedDate, waterConsumed) { currentMeals, goal, date, water ->
        val consumed = currentMeals.sumOf { it.calories }
        val protein = currentMeals.sumOf { it.protein }
        val carbs = currentMeals.sumOf { it.carbs }
        val fats = currentMeals.sumOf { it.fats }
        
        DashboardState(
            consumedCalories = consumed,
            remainingCalories = (goal - consumed).coerceAtLeast(0),
            goalCalories = goal,
            proteinG = protein,
            carbsG = carbs,
            fatsG = fats,
            meals = currentMeals,
            selectedDate = date,
            waterConsumed = water
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())
    
    fun selectDate(date: Calendar) {
        _selectedDate.value = date
    }
    
    fun addWater(amount: Int = 8) {
        _waterConsumed.value += amount
    }
    
    fun removeWater(amount: Int = 8) {
        _waterConsumed.value = (_waterConsumed.value - amount).coerceAtLeast(0)
    }
}

data class DashboardState(
    val consumedCalories: Int = 0,
    val remainingCalories: Int = 0,
    val goalCalories: Int = 2000,
    val proteinG: Int = 0,
    val carbsG: Int = 0,
    val fatsG: Int = 0,
    val meals: List<MealEntity> = emptyList(),
    val selectedDate: Calendar = Calendar.getInstance(),
    val waterConsumed: Int = 0
)

