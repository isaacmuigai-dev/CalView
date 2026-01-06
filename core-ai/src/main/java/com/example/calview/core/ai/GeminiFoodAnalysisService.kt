package com.example.calview.core.ai

import android.graphics.Bitmap
import com.example.calview.core.ai.model.FoodAnalysisResponse
import com.example.calview.core.ai.model.FoodItem
import com.example.calview.core.ai.model.Macros
import com.example.calview.core.ai.model.NutritionalData
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.content
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GeminiFoodAnalysisService @Inject constructor(
    private val generativeModel: GenerativeModel
) : FoodAnalysisService {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun analyzeFoodImage(bitmap: Bitmap): Result<FoodAnalysisResponse> {
        return try {
            android.util.Log.d("GeminiAI", "Starting food image analysis...")
            
            val prompt = """
                Analyze the provided food image and provide nutritional estimates.
                List all food items specifically.
                Estimate volume in grams.
                Calculate Calories, Protein (p), Carbohydrates (c), Fats (f), Fiber (fi), Sugar (s), and Sodium (na in mg).
                
                IMPORTANT: Return all nutritional amounts (weight, calories, macros) as INTEGERS. Round to the nearest whole number. Do NOT use decimals for these values.
                
                Return ONLY a valid JSON object with this structure:
                {
                  "detected_items": [
                    {
                      "name": "string",
                      "estimated_weight_g": integer,
                      "calories": integer,
                      "macros": { "p": integer, "c": integer, "f": integer, "fi": integer, "s": integer, "na": integer }
                    }
                  ],
                  "total": { "calories": integer, "protein": integer, "carbs": integer, "fats": integer, "fiber": integer, "sugar": integer, "sodium": integer },
                  "confidence_score": number (0.0 to 1.0),
                  "health_insight": "string"
                }
                If no food is detected, return: {"error": "No food detected"}
            """.trimIndent()

            android.util.Log.d("GeminiAI", "Sending request to Gemini API...")
            
            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )

            android.util.Log.d("GeminiAI", "Received response from Gemini API")
            
            val responseText = response.text?.trim()
            if (responseText.isNullOrEmpty()) {
                android.util.Log.e("GeminiAI", "Empty response from AI")
                throw Exception("Empty response from AI")
            }
            
            android.util.Log.d("GeminiAI", "Response text (first 200 chars): ${responseText.take(200)}")
            
            // Extract JSON if AI surrounds it with markdown blocks
            val cleanedJson = responseText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            
            android.util.Log.d("GeminiAI", "Parsing JSON response...")
            
            val result = json.decodeFromString<FoodAnalysisResponse>(cleanedJson)
            android.util.Log.d("GeminiAI", "Successfully parsed response: ${result.detected_items.size} items detected")
            
            Result.success(result)
        } catch (e: Exception) {
            // Log the actual error for debugging
            android.util.Log.e("GeminiAI", "Food analysis failed: ${e.message}", e)
            android.util.Log.e("GeminiAI", "Exception type: ${e::class.java.simpleName}")
            
            // Return the actual error instead of mock data
            // This allows the UI to show proper error messages and retry options
            Result.failure(e)
        }
    }

    private fun createMockResponse(): FoodAnalysisResponse {
        return FoodAnalysisResponse(
            detected_items = listOf(
                FoodItem(
                    name = "Grilled Chicken Salad",
                    estimated_weight_g = 350,
                    calories = 450,
                    macros = Macros(p = 35, c = 12, f = 25)
                )
            ),
            total = NutritionalData(
                calories = 450,
                protein = 35,
                carbs = 12,
                fats = 25
            ),
            confidence_score = 0.92,
            health_insight = "Excellent choice! This meal is high in protein and contains healthy fats, making it great for muscle recovery and satiety."
        )
    }
}
