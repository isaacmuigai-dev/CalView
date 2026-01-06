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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val foodAnalysisService: FoodAnalysisService,
    private val mealRepository: MealRepository,
    private val userPreferencesRepository: com.example.calview.core.data.repository.UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    // Camera tutorial state
    val hasSeenCameraTutorial: StateFlow<Boolean> = 
        userPreferencesRepository.hasSeenCameraTutorial
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    fun markTutorialSeen() {
        viewModelScope.launch {
            userPreferencesRepository.setHasSeenCameraTutorial(true)
        }
    }
    
    // Currently analyzing meal ID
    private var currentMealId: Long? = null

    fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
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
                
                // 4. Run the AI analysis in a non-cancellable context
                // This ensures the analysis completes even if the user navigates away
                withContext(NonCancellable + Dispatchers.IO) {
                    try {
                        // Update progress - starting analysis
                        updateProgress(25f)
                        android.util.Log.d("ScannerVM", "Starting AI analysis...")
                        
                        // Perform actual AI analysis with timeout
                        updateProgress(50f)
                        
                        val result = try {
                            kotlinx.coroutines.withTimeout(60000L) { // 60 second timeout
                                foodAnalysisService.analyzeFoodImage(bitmap)
                            }
                        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                            android.util.Log.e("ScannerVM", "AI analysis timed out after 60 seconds")
                            Result.failure(Exception("Analysis timed out. Please try again."))
                        }
                        
                        // Update progress - processing results
                        updateProgress(75f)
                        android.util.Log.d("ScannerVM", "AI analysis completed, processing results...")
                        
                        result.onSuccess { response ->
                            android.util.Log.d("ScannerVM", "AI analysis successful: ${response.detected_items.size} items detected")
                            // Update meal with analysis results
                            currentMealId?.let { id ->
                                val updatedMeal = MealEntity(
                                    id = id,
                                    name = response.detected_items.firstOrNull()?.name ?: "Unknown Food",
                                    calories = response.total.calories,
                                    protein = response.total.protein,
                                    carbs = response.total.carbs,
                                    fats = response.total.fats,
                                    fiber = response.total.fiber,
                                    sugar = response.total.sugar,
                                    sodium = response.total.sodium,
                                    imagePath = imagePath,
                                    analysisStatus = AnalysisStatus.COMPLETED,
                                    analysisProgress = 100f,
                                    healthInsight = response.health_insight
                                )
                                mealRepository.updateMeal(updatedMeal)
                            }
                            _uiState.value = ScannerUiState.Success(response)
                        }.onFailure { error ->
                            android.util.Log.e("ScannerVM", "AI analysis failed: ${error.message}", error)
                            // Mark analysis as failed
                            currentMealId?.let { id ->
                                mealRepository.getMealById(id)?.let { meal ->
                                    mealRepository.updateMeal(
                                        meal.copy(
                                            analysisStatus = AnalysisStatus.FAILED,
                                            name = "Analysis Failed",
                                            analysisProgress = 0f
                                        )
                                    )
                                }
                            }
                            _uiState.value = ScannerUiState.Error(error.message ?: "Food analysis failed. Please try again.")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ScannerVM", "Error during AI analysis: ${e.message}", e)
                        // Mark analysis as failed
                        currentMealId?.let { id ->
                            try {
                                mealRepository.getMealById(id)?.let { meal ->
                                    mealRepository.updateMeal(
                                        meal.copy(
                                            analysisStatus = AnalysisStatus.FAILED,
                                            name = "Analysis Failed",
                                            analysisProgress = 0f
                                        )
                                    )
                                }
                            } catch (_: Exception) { /* Ignore nested errors */ }
                        }
                        _uiState.value = ScannerUiState.Error(e.message ?: "Food analysis failed. Please try again.")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ScannerVM", "Unexpected error in analyzeImage: ${e.message}", e)
                // Handle any unexpected exceptions
                currentMealId?.let { id ->
                    try {
                        mealRepository.getMealById(id)?.let { meal ->
                            mealRepository.updateMeal(
                                meal.copy(
                                    analysisStatus = AnalysisStatus.FAILED,
                                    name = "Analysis Failed",
                                    analysisProgress = 0f
                                )
                            )
                        }
                    } catch (_: Exception) { /* Ignore nested errors */ }
                }
                _uiState.value = ScannerUiState.Error(e.message ?: "An unexpected error occurred")
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
    
    /**
     * Directly logs a meal to the repository without image analysis.
     * Used for manual food logging from the My Meals screen.
     */
    suspend fun logMealDirectly(meal: MealEntity): Long {
        return mealRepository.logMeal(meal)
    }
    
    /**
     * Get all meals as a Flow for the My Meals screen.
     */
    fun getAllMealsFlow() = mealRepository.getAllMeals()
    
    /**
     * Delete a meal by ID.
     */
    fun deleteMeal(mealId: Long) {
        viewModelScope.launch {
            mealRepository.deleteMealById(mealId)
        }
    }
    
    /**
     * Create a custom meal manually.
     */
    fun createCustomMeal(name: String, calories: Int, protein: Int, carbs: Int, fats: Int) {
        viewModelScope.launch {
            val meal = MealEntity(
                name = name,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats,
                analysisStatus = AnalysisStatus.COMPLETED,
                analysisProgress = 100f
            )
            mealRepository.logMeal(meal)
        }
    }
    
    /**
     * Look up product by barcode using OpenFoodFacts API.
     */
    fun lookupBarcode(barcode: String) {
        viewModelScope.launch {
            _uiState.value = ScannerUiState.Loading
            
            val product = OpenFoodFactsService.getProductByBarcode(barcode)
            
            if (product != null) {
                _uiState.value = ScannerUiState.BarcodeResult(product)
            } else {
                _uiState.value = ScannerUiState.Error("Product not found for barcode: $barcode")
            }
        }
    }
    
    /**
     * Log a product from barcode scan.
     */
    fun logBarcodeProduct(product: ProductInfo) {
        viewModelScope.launch {
            val meal = MealEntity(
                name = product.name,
                calories = product.calories,
                protein = product.protein.toInt(),
                carbs = product.carbs.toInt(),
                fats = product.fats.toInt(),
                analysisStatus = AnalysisStatus.COMPLETED,
                analysisProgress = 100f
            )
            mealRepository.logMeal(meal)
            _uiState.value = ScannerUiState.Logged
        }
    }
    
    /**
     * Parse nutrition from OCR text and show result.
     */
    fun parseNutritionFromText(text: String) {
        val parsed = NutritionLabelParser.parseNutritionLabel(text)
        
        if (parsed.isValid) {
            _uiState.value = ScannerUiState.OcrResult(parsed)
        } else {
            _uiState.value = ScannerUiState.Error("Could not extract nutrition information from label")
        }
    }
    
    /**
     * Log nutrition from OCR result.
     */
    fun logOcrNutrition(nutrition: ParsedNutrition, name: String = "Food Label") {
        viewModelScope.launch {
            val meal = MealEntity(
                name = name,
                calories = nutrition.calories,
                protein = nutrition.protein.toInt(),
                carbs = nutrition.carbs.toInt(),
                fats = nutrition.fats.toInt(),
                analysisStatus = AnalysisStatus.COMPLETED,
                analysisProgress = 100f
            )
            mealRepository.logMeal(meal)
            _uiState.value = ScannerUiState.Logged
        }
    }
}

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Loading : ScannerUiState()
    object NavigateToDashboard : ScannerUiState()
    data class Success(val response: FoodAnalysisResponse) : ScannerUiState()
    data class BarcodeResult(val product: ProductInfo) : ScannerUiState()
    data class OcrResult(val nutrition: ParsedNutrition) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
    object Logged : ScannerUiState()
}

