package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.ai.ExerciseParsingService
import com.example.calview.core.ai.model.ParsedExercise
import com.example.calview.core.ai.model.toIntensityFloat
import com.example.calview.core.data.billing.BillingManager
import com.example.calview.core.data.exercise.ExerciseDatabase
import com.example.calview.core.data.exercise.ExerciseTemplate
import com.example.calview.core.data.local.ExerciseEntity
import com.example.calview.core.data.local.ExerciseType
import com.example.calview.core.data.repository.ExerciseRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for exercise logging screen.
 */
data class ExerciseUiState(
    val isLoading: Boolean = false,
    val isPremium: Boolean = false,
    
    // AI parsing state
    val aiInputText: String = "",
    val isAiParsing: Boolean = false,
    val aiError: String? = null,
    val aiParsedExercises: List<ParsedExercise> = emptyList(),
    
    // Manual entry state
    val searchQuery: String = "",
    val filteredExercises: List<ExerciseTemplate> = ExerciseDatabase.exercises,
    val selectedExercise: ExerciseTemplate? = null,
    val selectedType: ExerciseType? = null,
    
    // Exercise parameters
    val durationMinutes: Int = 30,
    val intensity: Float = 0.5f,  // 0.0 to 1.0
    val sets: Int? = null,
    val reps: Int? = null,
    val weightKg: Double? = null,
    val distanceKm: Double? = null,
    val notes: String = "",
    
    // Calculated values
    val estimatedCalories: Int = 0,
    
    // User data for calculations
    val userWeightKg: Double = 70.0,
    
    // Today's exercises
    val todaysExercises: List<ExerciseEntity> = emptyList(),
    val todaysTotalCalories: Int = 0,
    
    // Success/error states
    val saveSuccess: Boolean = false,
    val saveError: String? = null
)

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val exerciseParsingService: ExerciseParsingService,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val billingManager: BillingManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()
    
    // Today's exercises from repository
    val todaysExercises = exerciseRepository.getExercisesForToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    init {
        loadInitialData()
        observeTodaysExercises()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            // Load user weight for calorie calculations
            val weight = userPreferencesRepository.weight.first()
            val isPremium = billingManager.isPremium.first()
            
            _uiState.update { state ->
                state.copy(
                    userWeightKg = weight.toDouble(),
                    isPremium = isPremium
                )
            }
        }
    }
    
    private fun observeTodaysExercises() {
        viewModelScope.launch {
            exerciseRepository.getExercisesForToday().collect { exercises ->
                _uiState.update { state ->
                    state.copy(
                        todaysExercises = exercises,
                        todaysTotalCalories = exercises.sumOf { it.caloriesBurned }
                    )
                }
            }
        }
        
        viewModelScope.launch {
            billingManager.isPremium.collect { premium ->
                _uiState.update { it.copy(isPremium = premium) }
            }
        }
    }
    
    // ==================== AI PARSING ====================
    
    fun updateAiInputText(text: String) {
        _uiState.update { it.copy(aiInputText = text, aiError = null) }
    }
    
    fun parseExerciseWithAi() {
        val text = _uiState.value.aiInputText
        if (text.isBlank()) {
            _uiState.update { it.copy(aiError = "Please enter an exercise description") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isAiParsing = true, aiError = null) }
            
            val result = exerciseParsingService.parseExerciseText(
                text = text,
                userWeightKg = _uiState.value.userWeightKg
            )
            
            result.fold(
                onSuccess = { response ->
                    _uiState.update { state ->
                        state.copy(
                            isAiParsing = false,
                            aiParsedExercises = response.detected_exercises,
                            aiError = if (response.detected_exercises.isEmpty()) 
                                "No exercises detected" else null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isAiParsing = false,
                            aiError = error.message ?: "Failed to parse exercise"
                        )
                    }
                }
            )
        }
    }
    
    fun saveAiParsedExercises() {
        val exercises = _uiState.value.aiParsedExercises
        if (exercises.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                exercises.forEach { parsed ->
                    val entity = ExerciseEntity(
                        name = parsed.name,
                        type = parseExerciseType(parsed.type),
                        durationMinutes = parsed.duration_minutes ?: 30,
                        intensity = parsed.intensity.toIntensityFloat(),
                        caloriesBurned = parsed.estimated_calories,
                        sets = parsed.sets,
                        reps = parsed.reps,
                        weightKg = parsed.weight_kg,
                        distanceKm = parsed.distance_km,
                        metValue = parsed.met_value,
                        isAiGenerated = true,
                        fitnessInsight = null
                    )
                    exerciseRepository.logExercise(entity)
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        saveSuccess = true,
                        aiInputText = "",
                        aiParsedExercises = emptyList()
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        saveError = e.message ?: "Failed to save exercises"
                    )
                }
            }
        }
    }
    
    // ==================== MANUAL ENTRY ====================
    
    fun updateSearchQuery(query: String) {
        val filtered = ExerciseDatabase.searchExercises(query)
        _uiState.update { 
            it.copy(
                searchQuery = query, 
                filteredExercises = filtered
            )
        }
    }
    
    fun filterByType(type: ExerciseType?) {
        val filtered = if (type == null) {
            ExerciseDatabase.searchExercises(_uiState.value.searchQuery)
        } else {
            ExerciseDatabase.getExercisesByType(type).filter { 
                it.name.lowercase().contains(_uiState.value.searchQuery.lowercase())
            }
        }
        _uiState.update { 
            it.copy(
                selectedType = type,
                filteredExercises = filtered
            )
        }
    }
    
    fun selectExercise(exercise: ExerciseTemplate) {
        val calories = calculateCalories(
            metValue = exercise.metValue,
            durationMinutes = _uiState.value.durationMinutes,
            intensity = _uiState.value.intensity
        )
        
        _uiState.update { 
            it.copy(
                selectedExercise = exercise,
                estimatedCalories = calories
            )
        }
    }
    
    fun clearSelectedExercise() {
        _uiState.update { 
            it.copy(
                selectedExercise = null,
                estimatedCalories = 0
            )
        }
    }
    
    fun updateDuration(minutes: Int) {
        _uiState.update { state ->
            val calories = state.selectedExercise?.let {
                calculateCalories(it.metValue, minutes, state.intensity)
            } ?: 0
            state.copy(durationMinutes = minutes, estimatedCalories = calories)
        }
    }
    
    fun updateIntensity(intensity: Float) {
        _uiState.update { state ->
            val calories = state.selectedExercise?.let {
                calculateCalories(it.metValue, state.durationMinutes, intensity)
            } ?: 0
            state.copy(intensity = intensity, estimatedCalories = calories)
        }
    }
    
    fun updateSets(sets: Int?) {
        _uiState.update { it.copy(sets = sets) }
    }
    
    fun updateReps(reps: Int?) {
        _uiState.update { it.copy(reps = reps) }
    }
    
    fun updateWeight(weightKg: Double?) {
        _uiState.update { it.copy(weightKg = weightKg) }
    }
    
    fun updateDistance(distanceKm: Double?) {
        _uiState.update { it.copy(distanceKm = distanceKm) }
    }
    
    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }
    
    fun saveManualExercise() {
        val state = _uiState.value
        val selected = state.selectedExercise ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val entity = ExerciseEntity(
                    name = selected.name,
                    type = selected.type,
                    durationMinutes = state.durationMinutes,
                    intensity = state.intensity,
                    caloriesBurned = state.estimatedCalories,
                    sets = state.sets,
                    reps = state.reps,
                    weightKg = state.weightKg,
                    distanceKm = state.distanceKm,
                    metValue = ExerciseDatabase.adjustMetForIntensity(selected.metValue, state.intensity),
                    notes = state.notes.takeIf { it.isNotBlank() },
                    isAiGenerated = false
                )
                
                exerciseRepository.logExercise(entity)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        saveSuccess = true,
                        selectedExercise = null,
                        durationMinutes = 30,
                        intensity = 0.5f,
                        sets = null,
                        reps = null,
                        weightKg = null,
                        distanceKm = null,
                        notes = "",
                        estimatedCalories = 0
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        saveError = e.message ?: "Failed to save exercise"
                    )
                }
            }
        }
    }
    
    // ==================== DELETE ====================
    
    fun deleteExercise(exercise: ExerciseEntity) {
        viewModelScope.launch {
            try {
                exerciseRepository.deleteExercise(exercise)
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = "Failed to delete exercise") }
            }
        }
    }
    
    // ==================== HELPERS ====================
    
    private fun calculateCalories(metValue: Double, durationMinutes: Int, intensity: Float): Int {
        val adjustedMet = ExerciseDatabase.adjustMetForIntensity(metValue, intensity)
        return ExerciseDatabase.calculateCalories(
            metValue = adjustedMet,
            weightKg = _uiState.value.userWeightKg,
            durationMinutes = durationMinutes
        )
    }
    
    private fun parseExerciseType(typeString: String): ExerciseType {
        return when (typeString.lowercase()) {
            "cardio" -> ExerciseType.CARDIO
            "strength" -> ExerciseType.STRENGTH
            "flexibility" -> ExerciseType.FLEXIBILITY
            "sport" -> ExerciseType.SPORT
            else -> ExerciseType.OTHER
        }
    }
    
    fun clearSaveState() {
        _uiState.update { it.copy(saveSuccess = false, saveError = null) }
    }
    
    fun clearAiError() {
        _uiState.update { it.copy(aiError = null) }
    }
}
