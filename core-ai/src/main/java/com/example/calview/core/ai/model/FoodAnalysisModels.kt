package com.example.calview.core.ai.model

import kotlinx.serialization.Serializable

@Serializable
data class FoodAnalysisResponse(
    val detected_items: List<FoodItem>,
    val total: NutritionalData,
    val confidence_score: Double,
    val health_insight: String
)

@Serializable
data class FoodItem(
    val name: String,
    val estimated_weight_g: Int,
    val calories: Int,
    val macros: Macros
)

@Serializable
data class NutritionalData(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int
)

@Serializable
data class Macros(
    val p: Int,
    val c: Int,
    val f: Int
)

@Serializable
data class ErrorResponse(
    val error: String
)
