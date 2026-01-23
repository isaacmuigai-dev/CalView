package com.example.calview.core.data.suggestions

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Logic engine for generating smart meal suggestions based on user gaps.
 */
@Singleton
class MealSuggestionEngine @Inject constructor(
    private val database: SuggestedFoodsDatabase
) {
    
    data class SuggestionResult(
        val reason: String,
        val foods: List<SuggestedFood>
    )
    
    /**
     * Analyze gaps and return targeted suggestions.
     */
    fun suggestFoods(
        caloriesRemaining: Int,
        proteinRemaining: Int,
        carbsRemaining: Int,
        fatsRemaining: Int,
        currentHour: Int
    ): SuggestionResult {
        
        // 1. Identify critical gaps
        val needsProtein = proteinRemaining > 30
        val needsCarbs = carbsRemaining > 40
        val needsFats = fatsRemaining > 20
        val lowCalories = caloriesRemaining < 300
        
        val suggestions = mutableListOf<SuggestedFood>()
        var rationale = ""
        
        // 2. Select strategy based on gaps and time
        when {
            // "Protein Priority" - significant protein gap
            needsProtein && caloriesRemaining > 200 -> {
                rationale = "You're behind on protein. Here are some lean, high-protein options to help you hit your goal."
                suggestions.addAll(database.getHighProteinFoods().shuffled().take(4))
            }
            
            // "Energy Boost" - Carbs needed, distinct from breakfast
            needsCarbs && !needsProtein && currentHour in 10..15 -> {
                rationale = "Need an energy boost? These healthy carb sources will fuel your afternoon."
                suggestions.addAll(database.getFoodsByCategory(FoodCategory.CARB).shuffled().take(2))
                suggestions.addAll(database.getFoodsByCategory(FoodCategory.FRUIT).shuffled().take(2))
            }
            
            // "Healthy Fats" - Fats low
            needsFats && caloriesRemaining > 200 -> {
                rationale = "Don't forget healthy fats! They are essential for hormone health and satiety."
                suggestions.addAll(database.getFoodsByCategory(FoodCategory.FAT).shuffled().take(4))
            }
            
            // "Low Calorie Finish" - Low calories left but might be hungry
            lowCalories && caloriesRemaining > 50 -> {
                rationale = "Running low on calories? These nutrient-dense, low-calorie options fit perfectly."
                suggestions.addAll(database.getLowCalorieFoods().shuffled().take(4))
            }
            
            // "Breakfast Time"
            currentHour in 5..10 -> {
                rationale = "Good morning! Start your day strong with these balanced breakfast ideas."
                suggestions.addAll(database.getFoodsByCategory(FoodCategory.BREAKFAST).shuffled().take(2))
                suggestions.addAll(database.getFoodsByCategory(FoodCategory.FRUIT).shuffled().take(1))
                suggestions.add(database.allFoods.first { it.name.contains("Oats") || it.name.contains("Yogurt") })
            }
            
            // "Lunch Time"
            currentHour in 11..14 -> {
                rationale = "Lunchtime! Here are some balanced meals to keep you powered up."
                suggestions.addAll(database.getFoodsByCategory(FoodCategory.LUNCH).shuffled().take(3))
                suggestions.addAll(database.getFoodsByCategory(FoodCategory.VEGETABLE).shuffled().take(1))
            }
            
            // "Dinner Time"
            currentHour in 17..20 -> {
                rationale = "Dinner ideas: Focus on protein and veggies for a satisfying end to your day."
                suggestions.addAll(database.getFoodsByCategory(FoodCategory.DINNER).shuffled().take(3))
                suggestions.addAll(database.getFoodsByCategory(FoodCategory.VEGETABLE).shuffled().take(1))
            }
            
            // Default / Balanced
            else -> {
                rationale = "Here are some balanced, healthy suggestions for your next meal."
                suggestions.addAll(database.allFoods.shuffled().take(5))
            }
        }
        
        // Ensure distinct and return
        return SuggestionResult(
            reason = rationale,
            foods = suggestions.distinctBy { it.name }
        )
    }
}
