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
    
    // Progress animation job
    private var progressAnimationJob: kotlinx.coroutines.Job? = null
    
    // Analysis stages with progress ranges and messages
    // Now goes all the way to 100% so user sees complete progress while waiting for AI
    private val analysisStages = listOf(
        AnalysisStage(0f, 15f, "Analysing..."),
        AnalysisStage(15f, 35f, "Identifying food items..."),
        AnalysisStage(35f, 55f, "Breaking down components..."),
        AnalysisStage(55f, 75f, "Calculating nutrition..."),
        AnalysisStage(75f, 90f, "Finalizing..."),
        AnalysisStage(90f, 100f, "Almost done!")
    )
    
    private data class AnalysisStage(
        val startProgress: Float,
        val endProgress: Float,
        val message: String
    )

    fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                // 1. Save image to internal storage (on IO thread to avoid blocking UI)
                val imagePath = withContext(Dispatchers.IO) {
                    saveImageToStorage(bitmap)
                }
                
                // 2. Create meal with ANALYZING status
                val placeholderMeal = MealEntity(
                    name = "Analyzing...",
                    calories = 0,
                    protein = 0,
                    carbs = 0,
                    fats = 0,
                    imagePath = imagePath,
                    analysisStatus = AnalysisStatus.ANALYZING,
                    analysisProgress = 0f,
                    analysisStatusMessage = "Starting analysis..."
                )
                
                // Capture ID in local variable to survive reset()
                val activeMealId = mealRepository.logMeal(placeholderMeal)
                currentMealId = activeMealId
                updateWidget()
                
                // 3. Signal that we should show redirection message
                _uiState.value = ScannerUiState.Redirecting(activeMealId)
                
                // 5. Run the AI analysis in a non-cancellable context
                // This ensures the analysis completes even if the user navigates away
                withContext(NonCancellable + Dispatchers.IO) {
                    // Start progress animation using a proper CoroutineScope
                    progressAnimationJob = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                        try {
                            animateProgressStages(activeMealId)
                        } catch (e: Exception) {
                            android.util.Log.e("ScannerVM", "Progress animation failed", e)
                        }
                    }

                    try {
                        android.util.Log.d("ScannerVM", "Starting AI analysis in background...")
                        
                        // Force initial progress update
                        updateProgressWithMessage(activeMealId, 0f, "Preparing analysis...")
                        
                        val result = try {
                            kotlinx.coroutines.withTimeout(60000L) { // 60 second timeout
                                foodAnalysisService.analyzeFoodImage(bitmap)
                            }
                        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                            android.util.Log.e("ScannerVM", "AI analysis timed out after 60 seconds")
                            Result.failure(Exception("Analysis timed out. Please try again."))
                        }
                        
                        // Stop progress animation
                        progressAnimationJob?.cancel()
                        
                        android.util.Log.d("ScannerVM", "AI analysis completed (result received), processing results...")
                        
                        result.onSuccess { response ->
                            android.util.Log.d("ScannerVM", "AI analysis successful: ${response.detected_items.size} items detected")
                            // Update meal with analysis results
                            // Combine all detected item names into a single descriptive name
                            val combinedName = when (response.detected_items.size) {
                                0 -> "Unknown Food"
                                1 -> response.detected_items.first().name
                                2 -> "${response.detected_items[0].name} & ${response.detected_items[1].name}"
                                else -> {
                                    val allButLast = response.detected_items.dropLast(1).joinToString(", ") { it.name }
                                    val last = response.detected_items.last().name
                                    "$allButLast & $last"
                                }
                            }
                            // Create JSON of detected items for UI display
                            val detectedItemsJsonStr = try {
                                kotlinx.serialization.json.Json.encodeToString(
                                    kotlinx.serialization.builtins.ListSerializer(
                                        com.example.calview.core.ai.model.FoodItem.serializer()
                                    ),
                                    response.detected_items
                                )
                            } catch (e: Exception) { null }
                            
                            // Progress is already at 100%, just show completion briefly
                            // Small delay to let user see "Complete!" message
                            delay(300)
                            
                            val updatedMeal = MealEntity(
                                id = activeMealId,
                                name = combinedName,
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
                                analysisStatusMessage = "Complete!",
                                healthInsight = response.health_insight,
                                confidenceScore = (response.confidence_score * 100).toFloat(),
                                detectedItemsJson = detectedItemsJsonStr
                            )
                            mealRepository.updateMeal(updatedMeal)
                            updateWidget()
                            
                            _uiState.value = ScannerUiState.Redirecting(activeMealId)
                        }.onFailure { error ->
                            android.util.Log.e("ScannerVM", "AI analysis failed: ${error.message}", error)
                            // Mark analysis as failed
                            mealRepository.getMealById(activeMealId)?.let { meal ->
                                mealRepository.updateMeal(
                                    meal.copy(
                                        analysisStatus = AnalysisStatus.FAILED,
                                        name = "Analysis Failed",
                                        analysisProgress = 0f,
                                        analysisStatusMessage = "Failed"
                                    )
                                )
                            }
                            _uiState.value = ScannerUiState.Error(error.message ?: "Food analysis failed. Please try again.")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ScannerVM", "Error during AI analysis: ${e.message}", e)
                        progressAnimationJob?.cancel()
                        // Mark analysis as failed
                        try {
                            mealRepository.getMealById(activeMealId)?.let { meal ->
                                mealRepository.updateMeal(
                                    meal.copy(
                                        analysisStatus = AnalysisStatus.FAILED,
                                        name = "Analysis Failed",
                                        analysisProgress = 0f,
                                        analysisStatusMessage = "Failed"
                                    )
                                )
                            }
                        } catch (_: Exception) { /* Ignore nested errors */ }
                        _uiState.value = ScannerUiState.Error(e.message ?: "Food analysis failed. Please try again.")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ScannerVM", "Unexpected error in analyzeImage: ${e.message}", e)
                progressAnimationJob?.cancel()
                // Handle any unexpected exceptions
                // We don't have activeMealId guaranteed here if it failed before logging
                _uiState.value = ScannerUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
    fun analyzeSpokenText(text: String) {
        viewModelScope.launch {
            try {
                // 1. Create a placeholder meal first
                val placeholderMeal = MealEntity(
                    name = "Processing Voice...",
                    calories = 0,
                    protein = 0,
                    carbs = 0,
                    fats = 0,
                    imagePath = null,
                    analysisStatus = AnalysisStatus.ANALYZING,
                    analysisProgress = 0f,
                    analysisStatusMessage = "Analyzing voice input..."
                )
                // Capture ID to local variable to survive reset()
                val activeMealId = mealRepository.logMeal(placeholderMeal)
                currentMealId = activeMealId
                updateWidget()
                
                // 2. Show redirection message
                _uiState.value = ScannerUiState.Redirecting(activeMealId)
                
                withContext(NonCancellable + Dispatchers.IO) {
                    // Start progress animation using a proper CoroutineScope
                    progressAnimationJob = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch { animateProgressStages(activeMealId) }
                    
                    try {
                        android.util.Log.d("ScannerVM", "Starting voice analysis...")
                        
                        val result = try {
                            kotlinx.coroutines.withTimeout(60000L) { // 60 second timeout like image analysis
                                foodAnalysisService.analyzeFoodText(text)
                            }
                        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                            android.util.Log.e("ScannerVM", "Voice analysis timed out after 60 seconds")
                            Result.failure(Exception("Analysis timed out. Please try again."))
                        }
                        
                        progressAnimationJob?.cancel()
                        
                        android.util.Log.d("ScannerVM", "Voice analysis completed, processing results...")
                        
                        result.onSuccess { response ->
                            android.util.Log.d("ScannerVM", "Voice analysis successful: ${response.detected_items.size} items detected")
                            
                            // Formulate name
                            val combinedName = when (response.detected_items.size) {
                                0 -> "Voice Entry"
                                1 -> response.detected_items.first().name
                                else -> {
                                    val allButLast = response.detected_items.dropLast(1).joinToString(", ") { it.name }
                                    "${allButLast} & ${response.detected_items.last().name}"
                                }
                            }
                            
                            val detectedItemsJsonStr = try {
                                kotlinx.serialization.json.Json.encodeToString(
                                    kotlinx.serialization.builtins.ListSerializer(
                                        com.example.calview.core.ai.model.FoodItem.serializer()
                                    ),
                                    response.detected_items
                                )
                            } catch (e: Exception) { null }
                            
                            // Small delay to let user see completion
                            delay(300)

                            // Generate AI Image for the food
                            val generatedImageUrl = foodAnalysisService.generateFoodImage(combinedName)

                            val updatedMeal = MealEntity(
                                id = activeMealId,
                                name = combinedName,
                                calories = response.total.calories,
                                protein = response.total.protein,
                                carbs = response.total.carbs,
                                fats = response.total.fats,
                                fiber = response.total.fiber,
                                sugar = response.total.sugar,
                                sodium = response.total.sodium,
                                // Use generated image URL (via imagePath field which handles strings)
                                // We store it in imagePath. The UI (AsyncImage) should handle it.
                                imagePath = generatedImageUrl, 
                                analysisStatus = AnalysisStatus.COMPLETED,
                                analysisProgress = 100f,
                                analysisStatusMessage = "Complete!",
                                healthInsight = response.health_insight,
                                confidenceScore = 100f,
                                detectedItemsJson = detectedItemsJsonStr
                            )
                            mealRepository.updateMeal(updatedMeal)
                            updateWidget()
                            
                            _uiState.value = ScannerUiState.Redirecting(activeMealId)
                            
                        }.onFailure { error ->
                            android.util.Log.e("ScannerVM", "Voice analysis failed: ${error.message}", error)
                            // Mark analysis as failed (like image analysis)
                            mealRepository.getMealById(activeMealId)?.let { meal ->
                                mealRepository.updateMeal(
                                    meal.copy(
                                        analysisStatus = AnalysisStatus.FAILED,
                                        name = "Analysis Failed",
                                        analysisProgress = 0f,
                                        analysisStatusMessage = "Failed"
                                    )
                                )
                            }
                            _uiState.value = ScannerUiState.Error(error.message ?: "Analysis failed")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ScannerVM", "Error during voice analysis: ${e.message}", e)
                        progressAnimationJob?.cancel()
                        // Mark analysis as failed
                        try {
                            mealRepository.getMealById(activeMealId)?.let { meal ->
                                mealRepository.updateMeal(
                                    meal.copy(
                                        analysisStatus = AnalysisStatus.FAILED,
                                        name = "Analysis Failed",
                                        analysisProgress = 0f,
                                        analysisStatusMessage = "Failed"
                                    )
                                )
                            }
                        } catch (_: Exception) { /* Ignore nested errors */ }
                        _uiState.value = ScannerUiState.Error(e.message ?: "Voice analysis failed. Please try again.")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ScannerVM", "Unexpected error in analyzeSpokenText: ${e.message}", e)
                progressAnimationJob?.cancel()
                // Use default error reporting if we can't update meal
                _uiState.value = ScannerUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
    
    /**
     * Animates progress through stages with smooth increments.
     * Each stage has a message and progress range.
     * Progress animates to 100% and then holds there while waiting for AI results.
     */
    private suspend fun animateProgressStages(mealId: Long) {
        android.util.Log.d("ScannerVM", "Starting progress animation")
        for (stage in analysisStages) {
            val steps = 15 // More increments for smoother animation
            val stepDuration = 200L // Faster updates (200ms) for responsive feel
            val progressPerStep = (stage.endProgress - stage.startProgress) / steps
            
            // Update message at stage start
            updateProgressWithMessage(mealId, stage.startProgress, stage.message)
            
            for (i in 1..steps) {
                delay(stepDuration)
                val progress = stage.startProgress + (progressPerStep * i)
                updateProgressWithMessage(mealId, progress, stage.message)
            }
        }
        
        // Hold at 100% while waiting for AI to complete
        // This is the expected behavior - user sees 100% and waits for results
        android.util.Log.d("ScannerVM", "Reached 100% progress, waiting for results")
        while (true) {
            delay(500)
            updateProgressWithMessage(mealId, 100f, "Waiting for results...")
        }
    }
    
    private suspend fun updateProgressWithMessage(mealId: Long, progress: Float, message: String) {
        try {
            mealRepository.getMealById(mealId)?.let { meal ->
                if (meal.analysisStatus == AnalysisStatus.ANALYZING) {
                    mealRepository.updateMeal(
                        meal.copy(
                            analysisProgress = progress,
                            analysisStatusMessage = message
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ScannerVM", "Error updating progress: ${e.message}")
        }
    }
    
    /**
     * Smoothly animates progress from current value to 100% over ~1.5 seconds.
     * Called after AI analysis completes to provide a polished finish experience.
     */
    private suspend fun animateProgressToCompletion(mealId: Long) {
        val meal = mealRepository.getMealById(mealId) ?: return
        val currentProgress = meal.analysisProgress
        
        // Calculate steps needed to reach 100%
        val targetProgress = 100f
        val progressRemaining = targetProgress - currentProgress
        
        if (progressRemaining <= 0) return // Already at or past 100%
        
        // Animate in 10 steps over ~1.5 seconds
        val steps = 10
        val stepDelay = 150L // 150ms per step = 1500ms total
        val progressPerStep = progressRemaining / steps
        
        val finishMessages = listOf(
            "Finalizing results...",
            "Calculating nutrition...",
            "Almost done!",
            "Complete!"
        )
        
        for (i in 1..steps) {
            delay(stepDelay)
            val newProgress = (currentProgress + (progressPerStep * i)).coerceAtMost(100f)
            val messageIndex = ((i - 1) * finishMessages.size / steps).coerceIn(0, finishMessages.lastIndex)
            
            mealRepository.getMealById(mealId)?.let { currentMeal ->
                if (currentMeal.analysisStatus == AnalysisStatus.ANALYZING) {
                    mealRepository.updateMeal(
                        currentMeal.copy(
                            analysisProgress = newProgress,
                            analysisStatusMessage = finishMessages[messageIndex]
                        )
                    )
                }
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
        val id = mealRepository.logMeal(meal)
        updateWidget()
        return id
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
            updateWidget()
        }
    }
    
    /**
     * Log a quick meal from AR HUD mode.
     * Creates a meal with estimated values from real-time detection.
     */
    fun logQuickMeal(name: String, calories: Int, imageBytes: ByteArray?) {
        viewModelScope.launch {
            // Estimate macros based on typical ratios (rough estimates)
            val protein = (calories * 0.20 / 4).toInt() // 20% protein
            val carbs = (calories * 0.50 / 4).toInt()   // 50% carbs
            val fats = (calories * 0.30 / 9).toInt()    // 30% fats
            
            val meal = MealEntity(
                name = name.ifBlank { "Quick Meal" },
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats,
                imagePath = null, // No image for quick meal
                analysisStatus = AnalysisStatus.COMPLETED,
                analysisProgress = 100f,
                confidenceScore = 60f // Lower confidence for quick AR detection
            )
            val id = mealRepository.logMeal(meal)
            updateWidget()
            _uiState.value = ScannerUiState.Redirecting(id)
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
            val id = mealRepository.logMeal(meal)
            updateWidget()
            _uiState.value = ScannerUiState.Redirecting(id)
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
            val id = mealRepository.logMeal(meal)
            updateWidget()
            _uiState.value = ScannerUiState.Redirecting(id)
        }
    }
    
    /**
     * Trigger widget update using reflection to avoid circular dependency
     * app (Widget) -> feature-scanner (ViewModel) -> app (Widget)
     */
    private fun updateWidget() {
        try {
            // First ensure data is synced to SharedPreferences
            viewModelScope.launch {
                try {
                    userPreferencesRepository.syncWidgetData()
                } catch (e: Exception) {
                    android.util.Log.e("ScannerViewModel", "Error syncing widget data", e)
                }
                
                // Then trigger the update broadcast
                try {
                    val widgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                    val widgetClass = Class.forName("com.example.calview.widget.CaloriesWidgetProvider")
                    val widgetComponent = android.content.ComponentName(context, widgetClass)
                    val widgetIds = widgetManager.getAppWidgetIds(widgetComponent)
                    
                    if (widgetIds.isNotEmpty()) {
                        val intent = android.content.Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                            component = widgetComponent
                            putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                        }
                        context.sendBroadcast(intent)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ScannerViewModel", "Error updating widget via reflection", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ScannerViewModel", "Error launching widget update coroutine", e)
        }
    }
}

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Loading : ScannerUiState()
    data class NavigateToDashboard(val mealId: Long? = null) : ScannerUiState()
    data class Redirecting(val mealId: Long? = null) : ScannerUiState()
    data class Success(val response: FoodAnalysisResponse) : ScannerUiState()
    data class BarcodeResult(val product: ProductInfo) : ScannerUiState()
    data class OcrResult(val nutrition: ParsedNutrition) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
    object Logged : ScannerUiState()
}

