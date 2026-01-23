package com.example.calview.core.ai.voice

import com.google.firebase.ai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class for parsed food item from voice input
 */
@Serializable
data class ParsedFoodItem(
    val name: String,
    val quantity: Float = 1f,
    val unit: String = "serving",
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    // Add optional fields that might come from AI but handle them safely
    val sugar: Int = 0,
    val fiber: Int = 0,
    val sodium: Int = 0
)

/**
 * AI-powered voice food parser using Gemini.
 * Converts natural language like "I had a chicken sandwich and a coffee" 
 * into structured food data.
 */
@Singleton
class VoiceFoodParser @Inject constructor(
    private val generativeModel: GenerativeModel
) {
    
    companion object {
        private val PARSE_PROMPT = """
You are a nutrition assistant. Parse the following spoken input into food items with estimated nutrition data.

Input: "%s"

Return a JSON array of food items. Each item should have:
- name: Food name (string)
- quantity: Amount (number, default 1)
- unit: Unit of measurement (string like "piece", "cup", "g", "oz", default "serving")
- calories: Estimated calories (integer)
- protein: Protein in grams (integer)
- carbs: Carbohydrates in grams (integer)
- fat: Fat in grams (integer)

Example response for "I had 2 eggs and a slice of toast":
[
  {"name": "Eggs", "quantity": 2, "unit": "piece", "calories": 156, "protein": 12, "carbs": 1, "fat": 11},
  {"name": "Toast", "quantity": 1, "unit": "slice", "calories": 79, "protein": 3, "carbs": 15, "fat": 1}
]

Respond ONLY with the JSON array, no other text.
        """.trimIndent()
        
        private val jsonParser = Json { 
            ignoreUnknownKeys = true 
            isLenient = true 
            coerceInputValues = true
        }
    }
    
    /**
     * Parse voice input into food items
     * @param transcript The spoken input to parse (will be sanitized and validated)
     * @return Result containing list of parsed food items or error
     */
    suspend fun parseVoiceInput(transcript: String): Result<List<ParsedFoodItem>> {
        return withContext(Dispatchers.IO) {
            try {
                // Security: Validate and sanitize input
                val sanitizedTranscript = sanitizeInput(transcript)
                if (sanitizedTranscript.isBlank()) {
                    return@withContext Result.failure(Exception("Empty or invalid input"))
                }
                
                val prompt = PARSE_PROMPT.format(sanitizedTranscript)
                val response = generativeModel.generateContent(prompt)
                val text = response.text ?: return@withContext Result.failure(Exception("Empty response from AI"))
                
                // Clean and parse JSON response
                val cleanedJson = text
                    .trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
                
                val foods = try {
                    val parsed = jsonParser.decodeFromString<List<ParsedFoodItem>>(cleanedJson)
                    // Security: Validate parsed nutrition values
                    parsed.map { validateAndClampNutrition(it) }
                } catch (e: Exception) {
                    emptyList()
                }
                
                Result.success(foods)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sanitize user input to prevent injection attacks
     */
    private fun sanitizeInput(input: String): String {
        // Limit length to prevent abuse
        val maxLength = 500
        val trimmed = if (input.length > maxLength) input.take(maxLength) else input
        
        // Remove potentially dangerous characters and control characters
        return trimmed
            .replace(Regex("[\\x00-\\x1F\\x7F]"), "") // Remove control characters
            .replace(Regex("[{}\\[\\]\"'`]"), "") // Remove JSON-like characters from user input
            .trim()
    }
    
    /**
     * Validate and clamp nutrition values to reasonable ranges
     */
    private fun validateAndClampNutrition(item: ParsedFoodItem): ParsedFoodItem {
        return item.copy(
            name = item.name.take(100), // Limit name length
            quantity = item.quantity.coerceIn(0.01f, 100f),
            calories = item.calories.coerceIn(0, 5000),
            protein = item.protein.coerceIn(0, 500),
            carbs = item.carbs.coerceIn(0, 1000),
            fat = item.fat.coerceIn(0, 500),
            sugar = item.sugar.coerceIn(0, 500),
            fiber = item.fiber.coerceIn(0, 200),
            sodium = item.sodium.coerceIn(0, 10000)
        )
    }
}
