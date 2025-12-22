package com.example.calview.feature.scanner

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.ai.FoodAnalysisService
import com.example.calview.core.ai.model.FoodAnalysisResponse
import com.example.calview.core.data.local.MealEntity
import com.example.calview.core.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val foodAnalysisService: FoodAnalysisService,
    private val mealRepository: MealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun analyzeImage(bitmap: Bitmap) {
        _uiState.value = ScannerUiState.Loading
        viewModelScope.launch {
            foodAnalysisService.analyzeFoodImage(bitmap)
                .onSuccess { response ->
                    _uiState.value = ScannerUiState.Success(response)
                }
                .onFailure { error ->
                    _uiState.value = ScannerUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun logMeal(response: FoodAnalysisResponse) {
        viewModelScope.launch {
            val meal = MealEntity(
                name = response.detected_items.firstOrNull()?.name ?: "Unknown Meal",
                calories = response.total.calories,
                protein = response.total.protein,
                carbs = response.total.carbs,
                fats = response.total.fats
            )
            mealRepository.logMeal(meal)
            _uiState.value = ScannerUiState.Logged
        }
    }
    
    fun reset() {
        _uiState.value = ScannerUiState.Idle
    }
}

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Loading : ScannerUiState()
    data class Success(val response: FoodAnalysisResponse) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
    object Logged : ScannerUiState()
}
