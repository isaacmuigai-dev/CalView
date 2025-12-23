package com.example.calview.ai

import android.graphics.Bitmap
import com.example.calview.ai.model.FoodAnalysisResponse

interface FoodAnalysisService {
    suspend fun analyzeFoodImage(bitmap: Bitmap): Result<FoodAnalysisResponse>
}
