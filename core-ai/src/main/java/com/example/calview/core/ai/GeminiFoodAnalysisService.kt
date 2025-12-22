package com.example.calview.core.ai

import android.graphics.Bitmap
import com.example.calview.core.ai.model.FoodAnalysisResponse
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GeminiFoodAnalysisService @Inject constructor(
    private val generativeModel: GenerativeModel
) : FoodAnalysisService {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun analyzeFoodImage(bitmap: Bitmap): Result<FoodAnalysisResponse> {
        return try {
            val prompt = """
                Analyze the provided food image and provide nutritional estimates.
                List all food items specifically.
                Estimate volume in grams.
                Calculate Calories, Protein (p), Carbohydrates (c), and Fats (f).
                Return ONLY a valid JSON object with this structure:
                {
                  "detected_items": [
                    {
                      "name": "string",
                      "estimated_weight_g": number,
                      "calories": number,
                      "macros": { "p": number, "c": number, "f": number }
                    }
                  ],
                  "total": { "calories": number, "protein": number, "carbs": number, "fats": number },
                  "confidence_score": number,
                  "health_insight": "string"
                }
                If no food is detected, return: {"error": "No food detected"}
            """.trimIndent()

            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )

            val responseText = response.text?.trim() ?: throw Exception("Empty response from AI")
            
            // Extract JSON if AI surrounds it with markdown blocks
            val cleanedJson = responseText.removePrefix("```json").removeSuffix("```").trim()
            
            val result = json.decodeFromString<FoodAnalysisResponse>(cleanedJson)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
