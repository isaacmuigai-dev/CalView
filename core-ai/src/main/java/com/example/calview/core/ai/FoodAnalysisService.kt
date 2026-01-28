package com.example.calview.core.ai

import android.graphics.Bitmap
import com.example.calview.core.ai.model.FoodAnalysisResponse

interface FoodAnalysisService {
    suspend fun analyzeFoodImage(bitmap: Bitmap): Result<FoodAnalysisResponse>
    
    /**
     * Analyze food image with additional ingredients that the user has specified
     * were missed by the initial AI analysis.
     * 
     * @param bitmap The food image to analyze
     * @param additionalIngredients List of ingredient names the user says are in the image
     * @return Analysis result including the additional ingredients
     */
    suspend fun analyzeFoodImageWithIngredients(
        bitmap: Bitmap, 
        additionalIngredients: List<String>
    ): Result<FoodAnalysisResponse>
    
    suspend fun analyzeFoodText(text: String): Result<FoodAnalysisResponse>
    suspend fun generateFoodImage(description: String): String?
}
