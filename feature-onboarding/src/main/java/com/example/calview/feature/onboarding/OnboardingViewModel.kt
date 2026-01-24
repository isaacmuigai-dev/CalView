package com.example.calview.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.ai.NutritionRecommendationService
import com.example.calview.core.ai.UserProfile
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val nutritionRecommendationService: NutritionRecommendationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    
    val language: StateFlow<String> = userPreferencesRepository.language
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "en"
        )

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val age = currentYear - state.birthYear
            
            userPreferencesRepository.saveUserProfile(
                goal = state.goal,
                gender = state.gender,
                age = age,
                weight = state.weight,
                height = state.heightCm // Save in CM
            )
            
            // Save goal weight if set
            if (state.goalWeight > 0) {
                userPreferencesRepository.setGoalWeight(state.goalWeight)
            }
            
            // Save birth date for Personal Details screen
            val monthName = getMonthName(state.birthMonth)
            userPreferencesRepository.setBirthDate(monthName, state.birthDay, state.birthYear)
            
            userPreferencesRepository.saveRecommendedMacros(
                calories = state.recommendedCalories,
                protein = state.recommendedProtein,
                carbs = state.recommendedCarbs,
                fats = state.recommendedFats
            )
            
            // Save calorie settings for dashboard
            userPreferencesRepository.setAddCaloriesBack(state.addCaloriesBack)
            userPreferencesRepository.setRolloverExtraCalories(state.rolloverExtraCalories)
            
            // Sync widget data so widget shows updated goals
            userPreferencesRepository.syncWidgetData()
            
            userPreferencesRepository.setOnboardingComplete(true)
            onComplete()
        }
    }
    
    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "January"
        }
    }

    fun onGenderSelected(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender)
        recalculateRecommendations()
    }

    fun onWorkoutsSelected(workouts: String) {
        _uiState.value = _uiState.value.copy(workoutsPerWeek = workouts)
        recalculateRecommendations()
    }
    
    fun onGoalSelected(goal: String) {
        _uiState.value = _uiState.value.copy(goal = goal)
        recalculateRecommendations()
    }

    fun onDietSelected(diet: String) {
        _uiState.value = _uiState.value.copy(dietPreference = diet)
    }

    // Removed onUnitToggle - metric-only app

    fun onHeightChanged(ft: Int, inches: Int) {
        _uiState.value = _uiState.value.copy(heightFt = ft, heightIn = inches)
        recalculateRecommendations()
    }

    fun onWeightChanged(weight: Float) {
        _uiState.value = _uiState.value.copy(weight = weight)
        recalculateRecommendations()
    }

    fun onBirthDateChanged(month: Int, day: Int, year: Int) {
        _uiState.value = _uiState.value.copy(birthMonth = month, birthDay = day, birthYear = year)
        recalculateRecommendations()
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

    fun onReferralCodeUsed(code: String) {
        _uiState.value = _uiState.value.copy(referralCode = code)
        viewModelScope.launch {
            userPreferencesRepository.setUsedReferralCode(code)
        }
    }

    fun onReferenceSourceSelected(source: String) {
        _uiState.value = _uiState.value.copy(referralSource = source)
    }

    fun onHeightCmChanged(cm: Int) {
        _uiState.value = _uiState.value.copy(heightCm = cm)
        recalculateRecommendations()
    }

    fun onGoalWeightChanged(weight: Float) {
        _uiState.value = _uiState.value.copy(goalWeight = weight)
    }

    fun setLanguage(code: String) {
        viewModelScope.launch {
            userPreferencesRepository.setLanguage(code)
        }
    }

    fun setHasSeenFeatureIntro(seen: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setHasSeenFeatureIntro(seen)
        }
    }

    /**
     * Recalculate nutrition recommendations based on current user profile.
     * Uses AI when available, falls back to BMR calculation.
     */
    private fun recalculateRecommendations() {
        val state = _uiState.value
        
        // Only calculate if we have enough data
        if (state.gender.isEmpty() || state.goal.isEmpty() || state.workoutsPerWeek.isEmpty()) {
            return
        }
        
        _uiState.value = state.copy(isCalculating = true)
        
        viewModelScope.launch {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val age = currentYear - state.birthYear
            
            // Height already in cm if set via onHeightCmChanged, otherwise calc from ft/in
            // Prioritize heightCm if it's set (non-default/non-zero logic if needed, but here we trust the state)
            val heightCm = state.heightCm.toFloat()
            
            // Weight already in kg (metric-only app)
            val weightKg = state.weight
            
            val profile = UserProfile(
                gender = state.gender,
                age = age,
                weightKg = weightKg,
                heightCm = heightCm,
                activityLevel = state.workoutsPerWeek,
                goal = state.goal
            )
            
            nutritionRecommendationService.getRecommendations(profile)
                .onSuccess { recommendation ->
                    _uiState.value = _uiState.value.copy(
                        recommendedCalories = recommendation.calories,
                        recommendedProtein = recommendation.protein,
                        recommendedCarbs = recommendation.carbs,
                        recommendedFats = recommendation.fats,
                        isCalculating = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isCalculating = false)
                }
        }
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
    val heightCm: Int = 170, // Default 170cm
    val goalWeight: Float = 0f, // Added for persistence
    val birthMonth: Int = 2,
    val birthDay: Int = 2,
    val birthYear: Int = 1998,
    val triedOtherApps: Boolean? = null,
    val referralSource: String = "",
    val accomplishments: List<String> = emptyList(),
    val addCaloriesBack: Boolean = false,
    val rolloverExtraCalories: Boolean = false,
    val referralCode: String = "",
    // Calculated recommendation values (default to reasonable estimates)
    val recommendedCalories: Int = 2000,
    val recommendedProtein: Int = 125,
    val recommendedCarbs: Int = 225,
    val recommendedFats: Int = 55,
    val isCalculating: Boolean = false
)

