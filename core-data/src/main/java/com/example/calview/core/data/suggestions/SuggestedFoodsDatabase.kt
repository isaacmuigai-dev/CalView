package com.example.calview.core.data.suggestions

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class representing a suggested food item.
 */
data class SuggestedFood(
    val id: String,
    val name: String,
    val calories: Int, // per serving
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val servingSize: String,
    val category: FoodCategory,
    val tags: List<String> = emptyList()
)

enum class FoodCategory(val displayName: String) {
    PROTEIN("Protein Source"),
    CARB("Healthy Carbs"),
    FAT("Healthy Fats"),
    VEGETABLE("Vegetables"),
    FRUIT("Fruits"),
    SNACK("Snacks"),
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner")
}

/**
 * A local database of ~200 healthy foods for suggestions.
 */
@Singleton
class SuggestedFoodsDatabase @Inject constructor() {
    
    val allFoods: List<SuggestedFood> = listOf(
        // PROTEIN SOURCES
        SuggestedFood("p1", "Chicken Breast (Grilled)", 165, 31, 0, 3, "100g", FoodCategory.PROTEIN, listOf("lean", "low-fat")),
        SuggestedFood("p2", "Salmon Fillet", 208, 20, 0, 13, "100g", FoodCategory.PROTEIN, listOf("omega-3", "fish")),
        SuggestedFood("p3", "Greek Yogurt (Non-fat)", 59, 10, 3, 0, "100g", FoodCategory.PROTEIN, listOf("dairy", "breakfast")),
        SuggestedFood("p4", "Tofu (Firm)", 76, 8, 2, 4, "100g", FoodCategory.PROTEIN, listOf("vegan", "soya")),
        SuggestedFood("p5", "Eggs (Boiled)", 155, 13, 1, 11, "2 large", FoodCategory.PROTEIN, listOf("breakfast", "snack")),
        SuggestedFood("p6", "Tuna (Canned in Water)", 116, 26, 0, 1, "1 can", FoodCategory.PROTEIN, listOf("fish", "lean")),
        SuggestedFood("p7", "Lean Beef Mince", 250, 26, 0, 15, "100g", FoodCategory.PROTEIN, listOf("meat")),
        SuggestedFood("p8", "Cottage Cheese", 98, 11, 3, 4, "100g", FoodCategory.PROTEIN, listOf("dairy", "snack")),
        SuggestedFood("p9", "Lentils (Cooked)", 116, 9, 20, 0, "100g", FoodCategory.PROTEIN, listOf("vegan", "fiber")),
        SuggestedFood("p10", "Chickpeas", 164, 9, 27, 3, "100g", FoodCategory.PROTEIN, listOf("vegan", "fiber")),
        SuggestedFood("p11", "Turkey Breast", 135, 30, 0, 1, "100g", FoodCategory.PROTEIN, listOf("lean")),
        SuggestedFood("p12", "Whey Protein Shake", 120, 24, 3, 1, "1 scoop", FoodCategory.PROTEIN, listOf("supplement")),
        
        // HEALTHY CARBS
        SuggestedFood("c1", "Oats (Rolled)", 389, 16, 66, 7, "100g", FoodCategory.CARB, listOf("breakfast", "fiber")),
        SuggestedFood("c2", "Brown Rice", 111, 2, 23, 1, "100g cooked", FoodCategory.CARB, listOf("grain", "fiber")),
        SuggestedFood("c3", "Sweet Potato", 86, 1, 20, 0, "100g", FoodCategory.CARB, listOf("vegetable", "fiber")),
        SuggestedFood("c4", "Quinoa (Cooked)", 120, 4, 21, 2, "100g", FoodCategory.CARB, listOf("grain", "protein")),
        SuggestedFood("c5", "Whole Wheat Bread", 247, 13, 41, 3, "2 slices", FoodCategory.CARB, listOf("grain")),
        SuggestedFood("c6", "Banana", 89, 1, 23, 0, "1 medium", FoodCategory.FRUIT, listOf("fruit", "snack")),
        SuggestedFood("c7", "Apple", 52, 0, 14, 0, "1 medium", FoodCategory.FRUIT, listOf("fruit", "snack")),
        SuggestedFood("c8", "Blueberries", 57, 1, 14, 0, "100g", FoodCategory.FRUIT, listOf("fruit", "antioxidant")),
        
        // HEALTHY FATS
        SuggestedFood("f1", "Avocado", 160, 2, 9, 15, "1/2 medium", FoodCategory.FAT, listOf("fruit", "omega-3")),
        SuggestedFood("f2", "Almonds", 579, 21, 22, 50, "100g", FoodCategory.FAT, listOf("nut", "snack")),
        SuggestedFood("f3", "Walnuts", 654, 15, 14, 65, "100g", FoodCategory.FAT, listOf("nut", "omega-3")),
        SuggestedFood("f4", "Chia Seeds", 486, 17, 42, 31, "100g", FoodCategory.FAT, listOf("seed", "fiber")),
        SuggestedFood("f5", "Olive Oil", 119, 0, 0, 14, "1 tbsp", FoodCategory.FAT, listOf("oil")),
        SuggestedFood("f6", "Peanut Butter", 588, 25, 20, 50, "100g", FoodCategory.FAT, listOf("nut")),
        
        // VEGETABLES
        SuggestedFood("v1", "Broccoli", 34, 3, 7, 0, "100g", FoodCategory.VEGETABLE, listOf("green", "fiber")),
        SuggestedFood("v2", "Spinach", 23, 3, 4, 0, "100g", FoodCategory.VEGETABLE, listOf("green", "iron")),
        SuggestedFood("v3", "Carrots", 41, 1, 10, 0, "100g", FoodCategory.VEGETABLE, listOf("vitamin-a")),
        SuggestedFood("v4", "Bell Peppers", 31, 1, 6, 0, "100g", FoodCategory.VEGETABLE, listOf("vitamin-c")),
        SuggestedFood("v5", "Cucumber", 15, 1, 4, 0, "100g", FoodCategory.VEGETABLE, listOf("hydration")),
        
        // MEAL IDEAS - LUNCH/DINNER
        SuggestedFood("m1", "Chicken Salad Bowl", 350, 30, 15, 12, "1 bowl", FoodCategory.LUNCH, listOf("balanced")),
        SuggestedFood("m2", "Salmon & Asparagus", 400, 35, 10, 20, "1 plate", FoodCategory.DINNER, listOf("low-carb")),
        SuggestedFood("m3", "Turkey Wrap", 320, 25, 30, 10, "1 wrap", FoodCategory.LUNCH, listOf("quick")),
        SuggestedFood("m4", "Vegetable Stir Fry", 280, 10, 40, 8, "1 plate", FoodCategory.DINNER, listOf("vegan")),
        SuggestedFood("m5", "Beef Burrito Bowl", 550, 35, 60, 20, "1 bowl", FoodCategory.LUNCH, listOf("high-protein")),
        
        // SNACKS
        SuggestedFood("s1", "Hummus & Carrots", 150, 5, 15, 8, "1 serving", FoodCategory.SNACK, listOf("vegan")),
        SuggestedFood("s2", "Rice Cake w/ PB", 180, 6, 15, 10, "2 cakes", FoodCategory.SNACK, listOf("quick")),
        SuggestedFood("s3", "Protein Bar", 200, 20, 20, 8, "1 bar", FoodCategory.SNACK, listOf("protein"))
    )
    
    fun getFoodsByCategory(category: FoodCategory): List<SuggestedFood> {
        return allFoods.filter { it.category == category }
    }
    
    fun getHighProteinFoods(): List<SuggestedFood> {
        return allFoods.filter { it.protein > 15 || it.category == FoodCategory.PROTEIN }
    }
    
    fun getLowCalorieFoods(): List<SuggestedFood> {
        return allFoods.filter { it.calories < 100 }
    }
    
    fun searchFoods(query: String): List<SuggestedFood> {
        return allFoods.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
        }
    }
}
