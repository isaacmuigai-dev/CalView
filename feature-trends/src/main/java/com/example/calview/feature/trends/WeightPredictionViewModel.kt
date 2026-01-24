package com.example.calview.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.prediction.WeightPredictionEngine
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.repository.WeightHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Locale

data class WeightPredictionUiState(
    val currentWeight: Float = 0f,
    val goalWeight: Float = 0f,
    val predictedWeight30Days: Float = 0f,
    val weeklyChange: Float = 0f,
    val daysToGoal: Int? = null,
    val projectedDate: String? = null,
    val trend: WeightPredictionEngine.Trend = WeightPredictionEngine.Trend.INSUFFICIENT_DATA,
    val hasEnoughData: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class WeightPredictionViewModel @Inject constructor(
    private val weightHistoryRepository: WeightHistoryRepository,
    private val userPreferences: UserPreferencesRepository,
    private val predictionEngine: WeightPredictionEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightPredictionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }
    
    fun refresh() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Get goal weight
            val goalWeight = userPreferences.goalWeight.first()
            val currentWeight = userPreferences.weight.first()
            
            // Get history
            val history = weightHistoryRepository.getAllWeightHistory().first()
            
            // Generate prediction
            val prediction = predictionEngine.predictWeight(history, goalWeight)
            
            // Format date if exists
            val dateStr = prediction.projectedDate?.let {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
            }
            
            _uiState.update {
                it.copy(
                    currentWeight = currentWeight,
                    goalWeight = goalWeight,
                    predictedWeight30Days = prediction.predictedWeight30Days,
                    weeklyChange = prediction.weeklyChangeKg,
                    daysToGoal = prediction.daysToGoal,
                    projectedDate = dateStr,
                    trend = prediction.trend,
                    hasEnoughData = prediction.trend != WeightPredictionEngine.Trend.INSUFFICIENT_DATA,
                    isLoading = false
                )
            }
        }
    }
}
