package com.example.calview.feature.scanner

/**
 * Parses nutritional information from OCR text of food labels.
 */
object NutritionLabelParser {
    
    /**
     * Parse nutrition facts from OCR extracted text.
     * Returns parsed values or defaults to 0 if not found.
     */
    fun parseNutritionLabel(text: String): ParsedNutrition {
        val cleanText = text.lowercase().replace("\n", " ")
        
        val calories = extractCalories(cleanText)
        val protein = extractNutrient(cleanText, listOf("protein", "proteins"))
        val carbs = extractNutrient(cleanText, listOf("carbohydrate", "carbohydrates", "carbs", "total carb"))
        val fats = extractNutrient(cleanText, listOf("total fat", "fat", "fats"))
        val sugars = extractNutrient(cleanText, listOf("sugar", "sugars", "total sugar"))
        val fiber = extractNutrient(cleanText, listOf("fiber", "fibre", "dietary fiber"))
        val sodium = extractNutrient(cleanText, listOf("sodium", "salt"))
        
        return ParsedNutrition(
            calories = calories,
            protein = protein,
            carbs = carbs,
            fats = fats,
            sugars = sugars,
            fiber = fiber,
            sodium = sodium
        )
    }
    
    private fun extractCalories(text: String): Int {
        // Common patterns: "calories 250", "250 calories", "energy 250kcal", "250 kcal"
        val patterns = listOf(
            Regex("""calories[:\s]*(\d+)"""),
            Regex("""(\d+)\s*calories"""),
            Regex("""energy[:\s]*(\d+)\s*kcal"""),
            Regex("""(\d+)\s*kcal"""),
            Regex("""cal[:\s]*(\d+)"""),
            Regex("""(\d+)\s*cal\b""")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val value = match.groupValues[1].toIntOrNull()
                if (value != null && value in 1..5000) {
                    return value
                }
            }
        }
        return 0
    }
    
    private fun extractNutrient(text: String, keywords: List<String>): Float {
        for (keyword in keywords) {
            // Pattern: "protein 25g", "protein: 25 g", "protein 25"
            val patterns = listOf(
                Regex("""$keyword[:\s]*(\d+\.?\d*)\s*g"""),
                Regex("""$keyword[:\s]*(\d+\.?\d*)"""),
                Regex("""(\d+\.?\d*)\s*g\s*$keyword""")
            )
            
            for (pattern in patterns) {
                val match = pattern.find(text)
                if (match != null) {
                    val value = match.groupValues[1].toFloatOrNull()
                    if (value != null && value in 0f..1000f) {
                        return value
                    }
                }
            }
        }
        return 0f
    }
}

/**
 * Parsed nutrition values from a food label.
 */
data class ParsedNutrition(
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fats: Float,
    val sugars: Float = 0f,
    val fiber: Float = 0f,
    val sodium: Float = 0f
) {
    val isValid: Boolean
        get() = calories > 0 || protein > 0 || carbs > 0 || fats > 0
}
