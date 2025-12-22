package com.example.calview.feature.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onGenderSelected(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun onWorkoutsSelected(workouts: String) {
        _uiState.value = _uiState.value.copy(workoutsPerWeek = workouts)
    }
    
    fun onGoalSelected(goal: String) {
        _uiState.value = _uiState.value.copy(goal = goal)
    }

    fun onDietSelected(diet: String) {
        _uiState.value = _uiState.value.copy(dietPreference = diet)
    }

    fun onUnitToggle(isMetric: Boolean) {
        _uiState.value = _uiState.value.copy(isMetric = isMetric)
    }

    fun onHeightChanged(ft: Int, inches: Int) {
        _uiState.value = _uiState.value.copy(heightFt = ft, heightIn = inches)
    }

    fun onWeightChanged(weight: Float) {
        _uiState.value = _uiState.value.copy(weight = weight)
    }

    fun onBirthDateChanged(month: Int, day: Int, year: Int) {
        _uiState.value = _uiState.value.copy(birthMonth = month, birthDay = day, birthYear = year)
    }

    fun onTriedOtherApps(tried: Boolean) {
        _uiState.value = _uiState.value.copy(triedOtherApps = tried)
    }

    fun onSourceSelected(source: String) {
        _uiState.value = _uiState.value.copy(referralSource = source)
    }

    fun onAccomplishmentsSelected(accomplishments: List<String>) {
        _uiState.value = _uiState.value.copy(accomplishments = accomplishments)
    }

    fun onAddCaloriesBackChanged(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(addCaloriesBack = enabled)
    }

    fun onRolloverExtraCaloriesChanged(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(rolloverExtraCalories = enabled)
    }

    fun onReferralCodeChanged(code: String) {
        _uiState.value = _uiState.value.copy(referralCode = code)
    }
}

data class OnboardingUiState(
    val goal: String = "",
    val gender: String = "",
    val workoutsPerWeek: String = "",
    val dietPreference: String = "",
    val isMetric: Boolean = false,
    val weight: Float = 119f,
    val heightFt: Int = 5,
    val heightIn: Int = 6,
    val heightCm: Int = 170,
    val birthMonth: Int = 2,
    val birthDay: Int = 2,
    val birthYear: Int = 1998,
    val triedOtherApps: Boolean? = null,
    val referralSource: String = "",
    val accomplishments: List<String> = emptyList(),
    val addCaloriesBack: Boolean = false,
    val rolloverExtraCalories: Boolean = false,
    val referralCode: String = "",
    // Mock recommended values
    val recommendedCalories: Int = 1705,
    val recommendedProtein: Int = 117,
    val recommendedCarbs: Int = 203,
    val recommendedFats: Int = 47
)
