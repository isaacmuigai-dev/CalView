package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Personal Details screen - manages all user profile data
 */
@HiltViewModel
class PersonalDetailsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Combine all personal details into a single state
    val uiState: StateFlow<PersonalDetailsState> = combine(
        combine(
            userPreferencesRepository.weight,
            userPreferencesRepository.height,
            userPreferencesRepository.gender,
            userPreferencesRepository.goalWeight,
            userPreferencesRepository.dailyStepsGoal
        ) { weight, height, gender, goalWeight, stepsGoal ->
            PersonalDetailsState(
                currentWeight = weight,
                height = height,
                gender = gender,
                goalWeight = goalWeight,
                dailyStepsGoal = stepsGoal
            )
        },
        userPreferencesRepository.age
    ) { state, age ->
        state.copy(age = age)
    }.combine(
        combine(
            userPreferencesRepository.birthMonth,
            userPreferencesRepository.birthDay,
            userPreferencesRepository.birthYear
        ) { month, day, year ->
            Triple(month, day, year)
        }
    ) { state, birthDate ->
        state.copy(
            birthMonth = birthDate.first,
            birthDay = birthDate.second,
            birthYear = birthDate.third
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PersonalDetailsState()
    )

    fun updateDailyStepsGoal(steps: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setDailyStepsGoal(steps)
        }
    }

    fun updateGoalWeight(weight: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setGoalWeight(weight)
            
            // Derive and save userGoal based on comparison with current weight
            val currentWeight = uiState.value.currentWeight
            val goal = when {
                weight < currentWeight - 1 -> "Lose Weight"
                weight > currentWeight + 1 -> "Gain Weight"
                else -> "Maintain"
            }
            userPreferencesRepository.saveUserProfile(
                goal = goal,
                gender = uiState.value.gender,
                age = uiState.value.age,
                weight = currentWeight,
                height = uiState.value.height
            )
        }
    }

    fun updateWeight(weight: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setWeight(weight)
        }
    }

    fun updateHeight(heightCm: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setHeight(heightCm)
        }
    }

    fun updateGender(gender: String) {
        viewModelScope.launch {
            userPreferencesRepository.setGender(gender)
        }
    }

    fun updateBirthDate(month: String, day: Int, year: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setBirthDate(month, day, year)
        }
    }
}

data class PersonalDetailsState(
    val currentWeight: Float = 0f,  // in kg
    val height: Int = 0,  // in cm
    val age: Int = 0,
    val gender: String = "",
    val goalWeight: Float = 0f,  // in kg
    val dailyStepsGoal: Int = 10000,
    val birthMonth: String = "January",
    val birthDay: Int = 1,
    val birthYear: Int = 2000
) {
    // Formatted date string for display
    val formattedBirthDate: String
        get() = String.format("%02d/%02d/%d", 
            getMonthNumber(birthMonth), birthDay, birthYear)
    
    // Formatted height for display (cm)
    val formattedHeight: String
        get() {
            if (height == 0) return ""
            return "$height cm"
        }
    
    private fun getMonthNumber(month: String): Int {
        return when (month) {
            "January" -> 1
            "February" -> 2
            "March" -> 3
            "April" -> 4
            "May" -> 5
            "June" -> 6
            "July" -> 7
            "August" -> 8
            "September" -> 9
            "October" -> 10
            "November" -> 11
            "December" -> 12
            else -> 1
        }
    }
}
