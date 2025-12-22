package com.example.calview.core.ai

import android.graphics.Bitmap
import com.example.calview.core.ai.model.FoodAnalysisResponse

interface FoodAnalysisService {
    suspend fun analyzeFoodImage(bitmap: Bitmap): Result<FoodAnalysisResponse>
}
