package com.example.calview.core.ai

import android.graphics.Bitmap
import com.example.calview.core.ai.model.FoodAnalysisResponse
import com.example.calview.core.ai.model.FoodItem
import com.example.calview.core.ai.model.Macros
import com.example.calview.core.ai.model.NutritionalData
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.content
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
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
                
                IMPORTANT NAMING RULES:
                1. Include QUANTITY in item names (e.g., "4 limes", "2 slices of bread", "3 eggs")
                2. Be DESCRIPTIVE with food names (e.g., "white rice" not just "rice", "grilled chicken breast" not just "chicken")
                3. If multiple items of the same type, count them (e.g., "4 lime wedges" not "lime wedges")
                
                CONFIDENCE SCORING:
                - Provide a confidence score (0.0 to 1.0) for EACH detected item
                - If an item is partially hidden, has a shiny/reflective surface, or is hard to identify, use lower confidence (0.5-0.7)
                - Add a detection_note for uncertain items explaining why (e.g., "partially hidden", "shiny surface", "similar to X")
                
                List all food items specifically.
                Estimate volume in grams.
                Calculate Calories, Protein (p), Carbohydrates (c), Fats (f), Fiber (fi), Sugar (s), and Sodium (na in mg).
                
                IMPORTANT: Return all nutritional amounts (weight, calories, macros) as INTEGERS. Round to the nearest whole number. Do NOT use decimals for these values.
                
                Return ONLY a valid JSON object with this structure:
                {
                  "detected_items": [
                    {
                      "name": "string with quantity (e.g., '4 limes', '2 fried eggs')",
                      "estimated_weight_g": integer,
                      "calories": integer,
                      "macros": { "p": integer, "c": integer, "f": integer, "fi": integer, "s": integer, "na": integer },
                      "confidence": number (0.0 to 1.0),
                      "detection_note": null or "string explaining uncertainty"
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
            
            parseResponse(response.text)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun analyzeFoodText(text: String): Result<FoodAnalysisResponse> {
        return try {
            android.util.Log.d("GeminiAI", "Starting food text analysis...")
            
            val prompt = """
                Analyze the following food description and provide nutritional estimates:
                
                "$text"
                
                Estimate standard serving sizes if not specified.
                
                List all food items specifically.
                Calculate Calories, Protein (p), Carbohydrates (c), Fats (f), Fiber (fi), Sugar (s), and Sodium (na in mg).
                
                IMPORTANT: Return all nutritional amounts (weight, calories, macros) as INTEGERS.
                
                  "detected_items": [
                    {
                      "name": "string with quantity",
                      "estimated_weight_g": integer,
                      "calories": integer,
                      "macros": { "p": integer, "c": integer, "f": integer, "fi": integer, "s": integer, "na": integer },
                      "confidence": 1.0,
                      "detection_note": null
                    }
                  ],
                  "total": { "calories": integer, "protein": integer, "carbs": integer, "fats": integer, "fiber": integer, "sugar": integer, "sodium": integer },
                  "confidence_score": 1.0,
                  "health_insight": "string"
                }
                
                IMPORTANT: Return all nutritional amounts (weight, calories, macros) as INTEGERS. Round to the nearest whole number.
            """.trimIndent()

            android.util.Log.d("GeminiAI", "Sending text request to Gemini API...")
            
            val response = generativeModel.generateContent(prompt)

            android.util.Log.d("GeminiAI", "Received text response from Gemini API")
            
            parseResponse(response.text)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun generateFoodImage(description: String): String? {
        return try {
            val encodedDescription = URLEncoder.encode(description, StandardCharsets.UTF_8.toString())
            // Using polinations.ai for AI image generation (no API key required)
            // Added explicit food photography keywords for better quality
            val enhancedPrompt = URLEncoder.encode("professional food photography of $description, studio lighting, 4k, delicious, appetizing", StandardCharsets.UTF_8.toString())
            "https://image.pollinations.ai/prompt/$enhancedPrompt?nologo=true"
        } catch (e: Exception) {
            android.util.Log.e("GeminiAI", "Failed to generate image URL", e)
            null
        }
    }

    private fun parseResponse(responseText: String?): Result<FoodAnalysisResponse> {
        val text = responseText?.trim()
        if (text.isNullOrEmpty()) {
            throw Exception("Empty response from AI")
        }
        
        android.util.Log.d("GeminiAI", "Response text (first 200 chars): ${text.take(200)}")
        
        try {
            // Find the first '{' and last '}' to extract the JSON object
            val firstBrace = text.indexOf('{')
            val lastBrace = text.lastIndexOf('}')
            
            if (firstBrace == -1 || lastBrace == -1 || lastBrace <= firstBrace) {
                android.util.Log.e("GeminiAI", "No JSON object found in response: $text")
                throw Exception("No valid JSON found in AI response")
            }
            
            var jsonString = text.substring(firstBrace, lastBrace + 1)
            
            // Basic cleanup for common AI JSON mistakes
            jsonString = jsonString
                .replace(",\\s*\\}".toRegex(), "}") // Trailing commas in objects
                .replace(",\\s*\\]".toRegex(), "]") // Trailing commas in arrays

            // Check if it's an error response
            if (jsonString.contains("\"error\"") && !jsonString.contains("\"detected_items\"")) {
                try {
                    val errorObj = json.decodeFromString<com.example.calview.core.ai.model.ErrorResponse>(jsonString)
                    throw Exception(errorObj.error)
                } catch (e: Exception) {
                    throw Exception("AI reported an error or no food detected")
                }
            }

            val result = json.decodeFromString<FoodAnalysisResponse>(jsonString)
            return Result.success(result)
        } catch (e: Throwable) {
            android.util.Log.e("GeminiAI", "JSON Parsing failed", e)
            android.util.Log.e("GeminiAI", "Raw text: $text")
            throw Exception("Failed to process food data: ${e.message}")
        }
    }

    private fun handleError(e: Exception): Result<FoodAnalysisResponse> {
        android.util.Log.e("GeminiAI", "Analysis failed: ${e.message}", e)
        return Result.failure(e)
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
