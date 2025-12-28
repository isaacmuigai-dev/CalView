package com.example.calview.feature.scanner

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.ai.FoodAnalysisService
import com.example.calview.core.ai.model.FoodAnalysisResponse
import com.example.calview.core.data.local.AnalysisStatus
import com.example.calview.core.data.local.MealEntity
import com.example.calview.core.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val foodAnalysisService: FoodAnalysisService,
    private val mealRepository: MealRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    // Currently analyzing meal ID
    private var currentMealId: Long? = null

    fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            // 1. Save image to internal storage
            val imagePath = saveImageToStorage(bitmap)
            
            // 2. Create meal with ANALYZING status
            val placeholderMeal = MealEntity(
                name = "Analyzing...",
                calories = 0,
                protein = 0,
                carbs = 0,
                fats = 0,
                imagePath = imagePath,
                analysisStatus = AnalysisStatus.ANALYZING,
                analysisProgress = 0f
            )
            currentMealId = mealRepository.logMeal(placeholderMeal)
            
            // 3. Signal that we should navigate to dashboard
            _uiState.value = ScannerUiState.NavigateToDashboard
            
            // 4. Simulate progress updates while analyzing
            launch {
                updateProgress(25f)
                delay(500)
                updateProgress(50f)
                delay(500)
                updateProgress(75f)
            }
            
            // 5. Perform actual AI analysis
            foodAnalysisService.analyzeFoodImage(bitmap)
                .onSuccess { response ->
                    // Update meal with analysis results
                    currentMealId?.let { id ->
                        val updatedMeal = MealEntity(
                            id = id,
                            name = response.detected_items.firstOrNull()?.name ?: "Unknown Food",
                            calories = response.total.calories,
                            protein = response.total.protein,
                            carbs = response.total.carbs,
                            fats = response.total.fats,
                            imagePath = imagePath,
                            analysisStatus = AnalysisStatus.COMPLETED,
                            analysisProgress = 100f,
                            healthInsight = response.health_insight
                        )
                        mealRepository.updateMeal(updatedMeal)
                    }
                    _uiState.value = ScannerUiState.Success(response)
                }
                .onFailure { error ->
                    // Mark analysis as failed
                    currentMealId?.let { id ->
                        mealRepository.getMealById(id)?.let { meal ->
                            mealRepository.updateMeal(
                                meal.copy(
                                    analysisStatus = AnalysisStatus.FAILED,
                                    name = "Analysis Failed"
                                )
                            )
                        }
                    }
                    _uiState.value = ScannerUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
    
    private suspend fun updateProgress(progress: Float) {
        currentMealId?.let { id ->
            mealRepository.getMealById(id)?.let { meal ->
                mealRepository.updateMeal(meal.copy(analysisProgress = progress))
            }
        }
    }
    
    private fun saveImageToStorage(bitmap: Bitmap): String {
        val filename = "food_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return file.absolutePath
    }

    fun logMeal(response: FoodAnalysisResponse) {
        // Meal is already logged with results, just reset state
        viewModelScope.launch {
            _uiState.value = ScannerUiState.Logged
        }
    }
    
    fun reset() {
        _uiState.value = ScannerUiState.Idle
        currentMealId = null
    }
}

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Loading : ScannerUiState()
    object NavigateToDashboard : ScannerUiState()  // New state for navigation
    data class Success(val response: FoodAnalysisResponse) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
    object Logged : ScannerUiState()
}
