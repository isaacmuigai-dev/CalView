package com.example.calview.ai.model

import kotlinx.serialization.Serializable

@Serializable
data class FoodAnalysisResponse(
    val detected_items: List<FoodItem> = emptyList(),
    val total: NutritionTotal,
    val confidence_score: Double,
    val health_insight: String,
    val error: String? = null
)

@Serializable
data class FoodItem(
    val name: String,
    val estimated_weight_g: Int,
    val calories: Int,
    val macros: Macros
)

@Serializable
data class Macros(
    val p: Int,
    val c: Int,
    val f: Int
)

@Serializable
data class NutritionTotal(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int
)
