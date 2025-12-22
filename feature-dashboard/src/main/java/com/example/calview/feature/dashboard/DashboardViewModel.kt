package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.local.MealEntity
import com.example.calview.core.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val mealRepository: MealRepository
) : ViewModel() {

    private val _dailyGoal = MutableStateFlow(2000)
    val dailyGoal = _dailyGoal.asStateFlow()

    val meals = mealRepository.getMealsForToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardState = combine(meals, dailyGoal) { currentMeals, goal ->
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
            meals = currentMeals
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())
}

data class DashboardState(
    val consumedCalories: Int = 0,
    val remainingCalories: Int = 0,
    val goalCalories: Int = 2000,
    val proteinG: Int = 0,
    val carbsG: Int = 0,
    val fatsG: Int = 0,
    val meals: List<MealEntity> = emptyList()
)
