package com.example.calview.core.ml

/**
 * Local calorie database for Food-101 categories
 * Values are approximate calories per 100g
 */
object CalorieDatabase {
    
    // Food-101 categories with calorie estimates per 100g
    private val calorieMap = mapOf(
        // Desserts & Sweets
        "apple_pie" to 237,
        "baklava" to 334,
        "bread_pudding" to 153,
        "cannoli" to 369,
        "carrot_cake" to 315,
        "cheesecake" to 321,
        "chocolate_cake" to 371,
        "chocolate_mousse" to 226,
        "churros" to 383,
        "creme_brulee" to 254,
        "cup_cakes" to 305,
        "donuts" to 417,
        "frozen_yogurt" to 159,
        "ice_cream" to 207,
        "macarons" to 400,
        "panna_cotta" to 236,
        "red_velvet_cake" to 310,
        "strawberry_shortcake" to 249,
        "tiramisu" to 283,
        "waffles" to 291,
        
        // Meat & Protein
        "baby_back_ribs" to 292,
        "beef_carpaccio" to 121,
        "beef_tartare" to 136,
        "chicken_curry" to 142,
        "chicken_quesadilla" to 215,
        "chicken_wings" to 247,
        "crab_cakes" to 155,
        "filet_mignon" to 267,
        "foie_gras" to 462,
        "fried_calamari" to 175,
        "grilled_salmon" to 208,
        "lobster_bisque" to 94,
        "lobster_roll" to 189,
        "mussels" to 172,
        "oysters" to 81,
        "peking_duck" to 337,
        "pork_chop" to 231,
        "prime_rib" to 340,
        "pulled_pork_sandwich" to 220,
        "scallops" to 111,
        "shrimp_and_grits" to 167,
        "steak" to 271,
        
        // Asian Foods
        "bibimbap" to 109,
        "dumplings" to 220,
        "edamame" to 121,
        "gyoza" to 190,
        "miso_soup" to 40,
        "pad_thai" to 170,
        "pho" to 43,
        "ramen" to 190,
        "sashimi" to 127,
        "spring_rolls" to 198,
        "sushi" to 145,
        "takoyaki" to 176,
        
        // Italian Foods
        "bruschetta" to 120,
        "caprese_salad" to 169,
        "escargots" to 90,
        "gnocchi" to 182,
        "lasagna" to 135,
        "macaroni_and_cheese" to 174,
        "paella" to 150,
        "pizza" to 266,
        "ravioli" to 189,
        "risotto" to 143,
        "spaghetti_bolognese" to 157,
        "spaghetti_carbonara" to 191,
        
        // Mexican Foods
        "breakfast_burrito" to 195,
        "ceviche" to 82,
        "guacamole" to 160,
        "huevos_rancheros" to 153,
        "nachos" to 306,
        "tacos" to 226,
        
        // Sandwiches & Quick Foods
        "club_sandwich" to 196,
        "croque_madame" to 254,
        "fish_and_chips" to 233,
        "french_fries" to 312,
        "french_toast" to 229,
        "fried_rice" to 163,
        "garlic_bread" to 350,
        "grilled_cheese" to 345,
        "hamburger" to 295,
        "hot_dog" to 290,
        "onion_rings" to 411,
        "pancakes" to 227,
        "poutine" to 150,
        
        // Salads & Light Foods
        "beet_salad" to 43,
        "caesar_salad" to 127,
        "greek_salad" to 99,
        "seaweed_salad" to 70,
        
        // Soups
        "clam_chowder" to 87,
        "french_onion_soup" to 47,
        "hot_and_sour_soup" to 39,
        
        // Appetizers & Sides
        "beignets" to 334,
        "deviled_eggs" to 196,
        "falafel" to 333,
        "hummus" to 177,
        "samosa" to 262,
        "tuna_tartare" to 140,
        
        // Eggs & Breakfast
        "eggs_benedict" to 196,
        "omelette" to 154
    )
    
    /**
     * Get calories per 100g for a food label
     * @param foodLabel The food label from the classifier (e.g., "chicken_wings" or "Chicken Wings")
     * @return Calories per 100g, or 150 as default if not found
     */
    fun getCaloriesPer100g(foodLabel: String): Int {
        val normalizedLabel = foodLabel.lowercase().replace(" ", "_")
        return calorieMap[normalizedLabel] ?: 150
    }
    
    /**
     * Estimate calories for a portion
     * @param foodLabel The food label
     * @param estimatedGrams Estimated weight in grams
     * @return Estimated total calories
     */
    fun estimateCalories(foodLabel: String, estimatedGrams: Int = 100): Int {
        val per100g = getCaloriesPer100g(foodLabel)
        return (per100g * estimatedGrams) / 100
    }
    
    /**
     * Get a formatted display name from a label
     */
    fun getDisplayName(label: String): String {
        return label.replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }
    
    /**
     * Get all available food categories
     */
    fun getAllCategories(): List<String> = calorieMap.keys.toList()
}
