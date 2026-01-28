package com.example.calview.core.data.exercise

import com.example.calview.core.data.local.ExerciseType

/**
 * Template for exercises in the database.
 * @param name Display name of the exercise
 * @param type Category of exercise
 * @param metValue Metabolic Equivalent of Task - used for calorie calculation
 * @param description Optional description or tips
 */
data class ExerciseTemplate(
    val name: String,
    val type: ExerciseType,
    val metValue: Double,
    val description: String? = null
)

/**
 * Curated database of common exercises with their MET values.
 * MET (Metabolic Equivalent of Task) values are based on the Compendium of Physical Activities.
 * 
 * Calorie calculation formula:
 * Calories = MET × weight(kg) × duration(hours)
 */
object ExerciseDatabase {
    
    /**
     * Calculate calories burned using the MET formula.
     * @param metValue The MET value of the exercise
     * @param weightKg User's weight in kilograms
     * @param durationMinutes Duration of exercise in minutes
     * @return Estimated calories burned
     */
    fun calculateCalories(metValue: Double, weightKg: Double, durationMinutes: Int): Int {
        return (metValue * weightKg * (durationMinutes / 60.0)).toInt()
    }
    
    /**
     * Adjust MET value based on intensity (0.0 to 1.0).
     * Low intensity (0.0) = 70% of MET
     * Moderate (0.5) = 100% of MET
     * High intensity (1.0) = 130% of MET
     */
    fun adjustMetForIntensity(baseMet: Double, intensity: Float): Double {
        val multiplier = 0.7 + (intensity * 0.6) // Range: 0.7 to 1.3
        return baseMet * multiplier
    }
    
    /**
     * Get exercises filtered by type.
     */
    fun getExercisesByType(type: ExerciseType): List<ExerciseTemplate> {
        return exercises.filter { it.type == type }
    }
    
    /**
     * Search exercises by name (case-insensitive).
     */
    fun searchExercises(query: String): List<ExerciseTemplate> {
        if (query.isBlank()) return exercises
        val lowerQuery = query.lowercase()
        return exercises.filter { it.name.lowercase().contains(lowerQuery) }
    }
    
    // ==================== CARDIO EXERCISES ====================
    
    private val cardioExercises = listOf(
        // Running
        ExerciseTemplate("Running (5 mph / 8 km/h)", ExerciseType.CARDIO, 8.3, "Light jogging pace"),
        ExerciseTemplate("Running (6 mph / 9.7 km/h)", ExerciseType.CARDIO, 9.8, "Moderate pace"),
        ExerciseTemplate("Running (7 mph / 11.3 km/h)", ExerciseType.CARDIO, 11.0, "Fast pace"),
        ExerciseTemplate("Running (8 mph / 12.9 km/h)", ExerciseType.CARDIO, 11.8, "Very fast pace"),
        ExerciseTemplate("Running (10 mph / 16 km/h)", ExerciseType.CARDIO, 14.5, "Sprint pace"),
        ExerciseTemplate("Running, stairs", ExerciseType.CARDIO, 15.0, "Running up stairs"),
        ExerciseTemplate("Trail Running", ExerciseType.CARDIO, 10.0, "Running on trails/uneven terrain"),
        
        // Walking
        ExerciseTemplate("Walking (2.5 mph / 4 km/h)", ExerciseType.CARDIO, 3.0, "Slow, leisurely pace"),
        ExerciseTemplate("Walking (3.5 mph / 5.6 km/h)", ExerciseType.CARDIO, 4.3, "Moderate pace"),
        ExerciseTemplate("Walking (4 mph / 6.4 km/h)", ExerciseType.CARDIO, 5.0, "Brisk pace"),
        ExerciseTemplate("Walking, uphill", ExerciseType.CARDIO, 6.0, "Walking uphill at moderate pace"),
        ExerciseTemplate("Power Walking", ExerciseType.CARDIO, 5.5, "Fast-paced walking"),
        ExerciseTemplate("Nordic Walking", ExerciseType.CARDIO, 6.8, "Walking with poles"),
        
        // Cycling
        ExerciseTemplate("Cycling (10-12 mph)", ExerciseType.CARDIO, 6.8, "Leisurely pace"),
        ExerciseTemplate("Cycling (12-14 mph)", ExerciseType.CARDIO, 8.0, "Moderate effort"),
        ExerciseTemplate("Cycling (14-16 mph)", ExerciseType.CARDIO, 10.0, "Vigorous effort"),
        ExerciseTemplate("Cycling (16-19 mph)", ExerciseType.CARDIO, 12.0, "Racing pace"),
        ExerciseTemplate("Cycling (>20 mph)", ExerciseType.CARDIO, 15.8, "Professional pace"),
        ExerciseTemplate("Stationary Bike (light)", ExerciseType.CARDIO, 5.5, "Light resistance"),
        ExerciseTemplate("Stationary Bike (moderate)", ExerciseType.CARDIO, 7.0, "Moderate resistance"),
        ExerciseTemplate("Stationary Bike (vigorous)", ExerciseType.CARDIO, 10.5, "High resistance"),
        ExerciseTemplate("Spinning Class", ExerciseType.CARDIO, 8.5, "Group cycling class"),
        ExerciseTemplate("Mountain Biking", ExerciseType.CARDIO, 8.5, "Off-road cycling"),
        
        // Swimming
        ExerciseTemplate("Swimming (freestyle, light)", ExerciseType.CARDIO, 5.8, "Slow, relaxed swimming"),
        ExerciseTemplate("Swimming (freestyle, moderate)", ExerciseType.CARDIO, 7.0, "Moderate pace"),
        ExerciseTemplate("Swimming (freestyle, fast)", ExerciseType.CARDIO, 9.8, "Vigorous lap swimming"),
        ExerciseTemplate("Swimming (backstroke)", ExerciseType.CARDIO, 7.0, "General backstroke"),
        ExerciseTemplate("Swimming (breaststroke)", ExerciseType.CARDIO, 7.0, "General breaststroke"),
        ExerciseTemplate("Swimming (butterfly)", ExerciseType.CARDIO, 11.0, "Butterfly stroke"),
        ExerciseTemplate("Water Aerobics", ExerciseType.CARDIO, 5.5, "Aerobics in water"),
        ExerciseTemplate("Treading Water", ExerciseType.CARDIO, 4.0, "Moderate effort"),
        
        // Cardio Machines
        ExerciseTemplate("Elliptical Trainer", ExerciseType.CARDIO, 5.0, "General use"),
        ExerciseTemplate("Elliptical (moderate)", ExerciseType.CARDIO, 6.5, "Moderate resistance"),
        ExerciseTemplate("Elliptical (vigorous)", ExerciseType.CARDIO, 8.0, "High resistance"),
        ExerciseTemplate("Stair Climber", ExerciseType.CARDIO, 9.0, "Stair machine"),
        ExerciseTemplate("Rowing Machine (light)", ExerciseType.CARDIO, 4.8, "Light effort"),
        ExerciseTemplate("Rowing Machine (moderate)", ExerciseType.CARDIO, 7.0, "Moderate effort"),
        ExerciseTemplate("Rowing Machine (vigorous)", ExerciseType.CARDIO, 8.5, "Vigorous effort"),
        ExerciseTemplate("Treadmill Walking", ExerciseType.CARDIO, 4.5, "3.5 mph, no incline"),
        ExerciseTemplate("Treadmill Running", ExerciseType.CARDIO, 9.0, "6 mph"),
        ExerciseTemplate("Ski Machine", ExerciseType.CARDIO, 6.8, "General use"),
        
        // Aerobics & Dance
        ExerciseTemplate("Aerobics (low impact)", ExerciseType.CARDIO, 5.0, "Low impact class"),
        ExerciseTemplate("Aerobics (high impact)", ExerciseType.CARDIO, 7.3, "High impact class"),
        ExerciseTemplate("Step Aerobics", ExerciseType.CARDIO, 8.5, "6-8 inch step"),
        ExerciseTemplate("Zumba", ExerciseType.CARDIO, 6.5, "Dance fitness class"),
        ExerciseTemplate("Dancing (general)", ExerciseType.CARDIO, 4.8, "General dancing"),
        ExerciseTemplate("Dancing (vigorous)", ExerciseType.CARDIO, 7.8, "Fast-paced dancing"),
        ExerciseTemplate("Hip Hop Dance", ExerciseType.CARDIO, 7.0, "Hip hop dance class"),
        ExerciseTemplate("Ballet", ExerciseType.CARDIO, 5.0, "Ballet practice"),
        
        // HIIT & Intervals
        ExerciseTemplate("HIIT Workout", ExerciseType.CARDIO, 9.0, "High-intensity interval training"),
        ExerciseTemplate("Tabata", ExerciseType.CARDIO, 10.0, "20s work, 10s rest intervals"),
        ExerciseTemplate("Circuit Training", ExerciseType.CARDIO, 8.0, "Mixed cardio/strength circuits"),
        ExerciseTemplate("Bootcamp Class", ExerciseType.CARDIO, 8.0, "Group fitness bootcamp"),
        
        // Jump Rope & Jumping
        ExerciseTemplate("Jump Rope (slow)", ExerciseType.CARDIO, 8.8, "Less than 100 skips/min"),
        ExerciseTemplate("Jump Rope (moderate)", ExerciseType.CARDIO, 11.0, "100-120 skips/min"),
        ExerciseTemplate("Jump Rope (fast)", ExerciseType.CARDIO, 12.3, "More than 120 skips/min"),
        ExerciseTemplate("Jumping Jacks", ExerciseType.CARDIO, 8.0, "Continuous jumping jacks"),
        ExerciseTemplate("Box Jumps", ExerciseType.CARDIO, 8.0, "Plyometric box jumps"),
        ExerciseTemplate("Burpees", ExerciseType.CARDIO, 8.0, "Full burpees"),
        
        // Other Cardio
        ExerciseTemplate("Kickboxing", ExerciseType.CARDIO, 10.0, "Cardio kickboxing class"),
        ExerciseTemplate("Boxing (bag work)", ExerciseType.CARDIO, 5.5, "Punching bag"),
        ExerciseTemplate("Boxing (sparring)", ExerciseType.CARDIO, 9.0, "Sparring session"),
        ExerciseTemplate("Hiking", ExerciseType.CARDIO, 6.0, "Cross-country hiking"),
        ExerciseTemplate("Hiking (steep)", ExerciseType.CARDIO, 7.5, "Steep incline"),
        ExerciseTemplate("Cross-Country Skiing", ExerciseType.CARDIO, 9.0, "Moderate pace"),
        ExerciseTemplate("Roller Skating", ExerciseType.CARDIO, 7.0, "General skating"),
        ExerciseTemplate("Ice Skating", ExerciseType.CARDIO, 7.0, "General skating")
    )
    
    // ==================== STRENGTH EXERCISES ====================
    
    private val strengthExercises = listOf(
        // Weight Training - General
        ExerciseTemplate("Weight Training (light)", ExerciseType.STRENGTH, 3.5, "Light weights, high reps"),
        ExerciseTemplate("Weight Training (moderate)", ExerciseType.STRENGTH, 5.0, "Moderate weights"),
        ExerciseTemplate("Weight Training (vigorous)", ExerciseType.STRENGTH, 6.0, "Heavy weights, low reps"),
        ExerciseTemplate("Powerlifting", ExerciseType.STRENGTH, 6.0, "Heavy compound lifts"),
        ExerciseTemplate("Olympic Weightlifting", ExerciseType.STRENGTH, 6.0, "Snatch, clean & jerk"),
        
        // Upper Body
        ExerciseTemplate("Push-ups", ExerciseType.STRENGTH, 3.8, "Bodyweight push-ups"),
        ExerciseTemplate("Pull-ups", ExerciseType.STRENGTH, 3.8, "Bodyweight pull-ups"),
        ExerciseTemplate("Bench Press", ExerciseType.STRENGTH, 5.0, "Barbell or dumbbell"),
        ExerciseTemplate("Overhead Press", ExerciseType.STRENGTH, 5.0, "Shoulder press"),
        ExerciseTemplate("Bicep Curls", ExerciseType.STRENGTH, 3.5, "Dumbbell or barbell curls"),
        ExerciseTemplate("Tricep Dips", ExerciseType.STRENGTH, 3.8, "Bodyweight or assisted"),
        ExerciseTemplate("Lat Pulldown", ExerciseType.STRENGTH, 4.5, "Cable machine"),
        ExerciseTemplate("Rows", ExerciseType.STRENGTH, 5.0, "Barbell or dumbbell rows"),
        ExerciseTemplate("Shoulder Raises", ExerciseType.STRENGTH, 3.5, "Lateral/front raises"),
        ExerciseTemplate("Chest Flyes", ExerciseType.STRENGTH, 3.5, "Dumbbell or cable"),
        
        // Lower Body
        ExerciseTemplate("Squats (bodyweight)", ExerciseType.STRENGTH, 5.0, "Air squats"),
        ExerciseTemplate("Squats (barbell)", ExerciseType.STRENGTH, 6.0, "Back or front squats"),
        ExerciseTemplate("Lunges", ExerciseType.STRENGTH, 4.0, "Walking or stationary"),
        ExerciseTemplate("Leg Press", ExerciseType.STRENGTH, 5.0, "Machine leg press"),
        ExerciseTemplate("Deadlifts", ExerciseType.STRENGTH, 6.0, "Conventional or sumo"),
        ExerciseTemplate("Romanian Deadlifts", ExerciseType.STRENGTH, 5.5, "RDLs for hamstrings"),
        ExerciseTemplate("Leg Curls", ExerciseType.STRENGTH, 3.5, "Machine leg curls"),
        ExerciseTemplate("Leg Extensions", ExerciseType.STRENGTH, 3.5, "Machine leg extensions"),
        ExerciseTemplate("Calf Raises", ExerciseType.STRENGTH, 3.5, "Standing or seated"),
        ExerciseTemplate("Hip Thrusts", ExerciseType.STRENGTH, 5.0, "Barbell hip thrusts"),
        ExerciseTemplate("Step-ups", ExerciseType.STRENGTH, 4.0, "Weighted step-ups"),
        
        // Core
        ExerciseTemplate("Planks", ExerciseType.STRENGTH, 3.0, "Isometric core hold"),
        ExerciseTemplate("Crunches", ExerciseType.STRENGTH, 3.8, "Abdominal crunches"),
        ExerciseTemplate("Sit-ups", ExerciseType.STRENGTH, 3.8, "Full sit-ups"),
        ExerciseTemplate("Russian Twists", ExerciseType.STRENGTH, 3.5, "Rotational core exercise"),
        ExerciseTemplate("Leg Raises", ExerciseType.STRENGTH, 3.5, "Hanging or lying"),
        ExerciseTemplate("Mountain Climbers", ExerciseType.STRENGTH, 8.0, "Dynamic core exercise"),
        ExerciseTemplate("Ab Wheel Rollouts", ExerciseType.STRENGTH, 4.0, "Ab roller exercise"),
        ExerciseTemplate("Dead Bug", ExerciseType.STRENGTH, 3.0, "Core stability"),
        ExerciseTemplate("Bird Dog", ExerciseType.STRENGTH, 3.0, "Core stability"),
        
        // Functional & Bodyweight
        ExerciseTemplate("Kettlebell Swings", ExerciseType.STRENGTH, 6.0, "Explosive hip hinge"),
        ExerciseTemplate("Medicine Ball Throws", ExerciseType.STRENGTH, 5.0, "Various throws"),
        ExerciseTemplate("Battle Ropes", ExerciseType.STRENGTH, 8.0, "Rope training"),
        ExerciseTemplate("TRX Training", ExerciseType.STRENGTH, 5.0, "Suspension training"),
        ExerciseTemplate("Resistance Bands", ExerciseType.STRENGTH, 4.0, "Band exercises"),
        ExerciseTemplate("Farmer's Walk", ExerciseType.STRENGTH, 6.0, "Loaded carry"),
        ExerciseTemplate("Wall Sits", ExerciseType.STRENGTH, 3.0, "Isometric leg hold"),
        ExerciseTemplate("Dips (parallel bars)", ExerciseType.STRENGTH, 4.0, "Upper body dips"),
        
        // CrossFit Style
        ExerciseTemplate("CrossFit WOD", ExerciseType.STRENGTH, 8.0, "Workout of the day"),
        ExerciseTemplate("Clean and Jerk", ExerciseType.STRENGTH, 6.0, "Olympic lift"),
        ExerciseTemplate("Snatch", ExerciseType.STRENGTH, 6.0, "Olympic lift"),
        ExerciseTemplate("Thrusters", ExerciseType.STRENGTH, 7.0, "Front squat to press"),
        ExerciseTemplate("Wall Balls", ExerciseType.STRENGTH, 6.0, "Squat and throw")
    )
    
    // ==================== FLEXIBILITY EXERCISES ====================
    
    private val flexibilityExercises = listOf(
        // Yoga
        ExerciseTemplate("Yoga (Hatha)", ExerciseType.FLEXIBILITY, 2.5, "Gentle, beginner-friendly"),
        ExerciseTemplate("Yoga (Vinyasa)", ExerciseType.FLEXIBILITY, 4.0, "Flow-based yoga"),
        ExerciseTemplate("Yoga (Power)", ExerciseType.FLEXIBILITY, 5.0, "Athletic, strength-focused"),
        ExerciseTemplate("Yoga (Bikram/Hot)", ExerciseType.FLEXIBILITY, 5.0, "Hot yoga, 26 poses"),
        ExerciseTemplate("Yoga (Ashtanga)", ExerciseType.FLEXIBILITY, 4.5, "Dynamic, set sequence"),
        ExerciseTemplate("Yoga (Yin)", ExerciseType.FLEXIBILITY, 2.5, "Long holds, deep stretching"),
        ExerciseTemplate("Yoga (Restorative)", ExerciseType.FLEXIBILITY, 2.0, "Relaxation focused"),
        
        // Pilates
        ExerciseTemplate("Pilates (mat)", ExerciseType.FLEXIBILITY, 3.0, "Mat-based exercises"),
        ExerciseTemplate("Pilates (reformer)", ExerciseType.FLEXIBILITY, 3.5, "Machine-based Pilates"),
        ExerciseTemplate("Pilates (advanced)", ExerciseType.FLEXIBILITY, 4.0, "Advanced movements"),
        
        // Stretching
        ExerciseTemplate("Stretching (light)", ExerciseType.FLEXIBILITY, 2.3, "Gentle stretching"),
        ExerciseTemplate("Stretching (dynamic)", ExerciseType.FLEXIBILITY, 3.0, "Active stretching"),
        ExerciseTemplate("Foam Rolling", ExerciseType.FLEXIBILITY, 2.5, "Self-myofascial release"),
        ExerciseTemplate("Mobility Work", ExerciseType.FLEXIBILITY, 3.0, "Joint mobility exercises"),
        
        // Mind-Body
        ExerciseTemplate("Tai Chi", ExerciseType.FLEXIBILITY, 3.0, "Slow, flowing movements"),
        ExerciseTemplate("Qigong", ExerciseType.FLEXIBILITY, 2.5, "Breathing and movement"),
        ExerciseTemplate("Meditation (seated)", ExerciseType.FLEXIBILITY, 1.5, "Mindfulness practice"),
        ExerciseTemplate("Barre Class", ExerciseType.FLEXIBILITY, 4.0, "Ballet-inspired fitness")
    )
    
    // ==================== SPORT EXERCISES ====================
    
    private val sportExercises = listOf(
        // Ball Sports
        ExerciseTemplate("Basketball (game)", ExerciseType.SPORT, 8.0, "Full-court game"),
        ExerciseTemplate("Basketball (shooting around)", ExerciseType.SPORT, 4.5, "Casual shooting"),
        ExerciseTemplate("Soccer (game)", ExerciseType.SPORT, 10.0, "Full match play"),
        ExerciseTemplate("Soccer (practice)", ExerciseType.SPORT, 7.0, "Drills and practice"),
        ExerciseTemplate("Tennis (singles)", ExerciseType.SPORT, 8.0, "Singles match"),
        ExerciseTemplate("Tennis (doubles)", ExerciseType.SPORT, 6.0, "Doubles match"),
        ExerciseTemplate("Badminton", ExerciseType.SPORT, 5.5, "General play"),
        ExerciseTemplate("Volleyball (beach)", ExerciseType.SPORT, 8.0, "Sand volleyball"),
        ExerciseTemplate("Volleyball (indoor)", ExerciseType.SPORT, 4.0, "Indoor volleyball"),
        ExerciseTemplate("Table Tennis", ExerciseType.SPORT, 4.0, "Ping pong"),
        ExerciseTemplate("Squash", ExerciseType.SPORT, 12.0, "Competitive squash"),
        ExerciseTemplate("Racquetball", ExerciseType.SPORT, 7.0, "General play"),
        ExerciseTemplate("Handball", ExerciseType.SPORT, 12.0, "Team handball"),
        
        // Field Sports
        ExerciseTemplate("American Football", ExerciseType.SPORT, 8.0, "Game play"),
        ExerciseTemplate("Rugby", ExerciseType.SPORT, 8.3, "Game play"),
        ExerciseTemplate("Cricket", ExerciseType.SPORT, 5.0, "General play"),
        ExerciseTemplate("Field Hockey", ExerciseType.SPORT, 7.8, "Game play"),
        ExerciseTemplate("Lacrosse", ExerciseType.SPORT, 8.0, "Game play"),
        ExerciseTemplate("Ultimate Frisbee", ExerciseType.SPORT, 8.0, "Game play"),
        
        // Combat Sports
        ExerciseTemplate("Martial Arts (general)", ExerciseType.SPORT, 5.3, "General practice"),
        ExerciseTemplate("Martial Arts (moderate)", ExerciseType.SPORT, 10.3, "Intense training"),
        ExerciseTemplate("Karate", ExerciseType.SPORT, 5.3, "Karate practice"),
        ExerciseTemplate("Taekwondo", ExerciseType.SPORT, 5.3, "Taekwondo practice"),
        ExerciseTemplate("Judo", ExerciseType.SPORT, 10.3, "Judo training"),
        ExerciseTemplate("Brazilian Jiu-Jitsu", ExerciseType.SPORT, 8.0, "BJJ training"),
        ExerciseTemplate("Wrestling", ExerciseType.SPORT, 6.0, "Wrestling practice"),
        ExerciseTemplate("Fencing", ExerciseType.SPORT, 6.0, "Competitive fencing"),
        ExerciseTemplate("MMA Training", ExerciseType.SPORT, 10.0, "Mixed martial arts"),
        
        // Water Sports
        ExerciseTemplate("Surfing", ExerciseType.SPORT, 3.0, "General surfing"),
        ExerciseTemplate("Paddleboarding", ExerciseType.SPORT, 6.0, "Stand-up paddleboard"),
        ExerciseTemplate("Kayaking", ExerciseType.SPORT, 5.0, "General kayaking"),
        ExerciseTemplate("Kayaking (vigorous)", ExerciseType.SPORT, 7.0, "Racing/whitewater"),
        ExerciseTemplate("Canoeing", ExerciseType.SPORT, 4.0, "Recreational canoeing"),
        ExerciseTemplate("Rowing (on water)", ExerciseType.SPORT, 7.0, "Crew/sculling"),
        ExerciseTemplate("Water Polo", ExerciseType.SPORT, 10.0, "Game play"),
        ExerciseTemplate("Diving", ExerciseType.SPORT, 3.0, "Platform/springboard"),
        
        // Winter Sports
        ExerciseTemplate("Downhill Skiing", ExerciseType.SPORT, 6.8, "Moderate effort"),
        ExerciseTemplate("Snowboarding", ExerciseType.SPORT, 5.3, "General snowboarding"),
        ExerciseTemplate("Ice Hockey", ExerciseType.SPORT, 8.0, "Game play"),
        ExerciseTemplate("Sledding/Tobogganing", ExerciseType.SPORT, 7.0, "Walking up hill included"),
        ExerciseTemplate("Snowshoeing", ExerciseType.SPORT, 8.0, "Moderate pace"),
        
        // Golf & Bowling
        ExerciseTemplate("Golf (walking)", ExerciseType.SPORT, 4.8, "Carrying clubs"),
        ExerciseTemplate("Golf (with cart)", ExerciseType.SPORT, 3.5, "Using golf cart"),
        ExerciseTemplate("Bowling", ExerciseType.SPORT, 3.0, "General bowling"),
        
        // Climbing
        ExerciseTemplate("Rock Climbing", ExerciseType.SPORT, 8.0, "Outdoor climbing"),
        ExerciseTemplate("Indoor Climbing", ExerciseType.SPORT, 5.8, "Climbing gym"),
        ExerciseTemplate("Bouldering", ExerciseType.SPORT, 5.0, "Short climbs, no rope"),
        
        // Other Sports
        ExerciseTemplate("Horse Riding (walking)", ExerciseType.SPORT, 3.8, "Walking pace"),
        ExerciseTemplate("Horse Riding (trotting)", ExerciseType.SPORT, 5.8, "Trotting"),
        ExerciseTemplate("Archery", ExerciseType.SPORT, 3.5, "General archery"),
        ExerciseTemplate("Billiards/Pool", ExerciseType.SPORT, 2.5, "Standing, walking"),
        ExerciseTemplate("Darts", ExerciseType.SPORT, 2.5, "Standing, throwing")
    )
    
    // ==================== OTHER ACTIVITIES ====================
    
    private val otherExercises = listOf(
        ExerciseTemplate("Gardening", ExerciseType.OTHER, 3.8, "General gardening"),
        ExerciseTemplate("Housework (light)", ExerciseType.OTHER, 2.5, "Cleaning, dusting"),
        ExerciseTemplate("Housework (vigorous)", ExerciseType.OTHER, 4.0, "Scrubbing, moving furniture"),
        ExerciseTemplate("Mowing Lawn (push mower)", ExerciseType.OTHER, 5.5, "Push mowing"),
        ExerciseTemplate("Shoveling Snow", ExerciseType.OTHER, 6.0, "Moderate effort"),
        ExerciseTemplate("Playing with Kids", ExerciseType.OTHER, 4.0, "Active play"),
        ExerciseTemplate("Walking the Dog", ExerciseType.OTHER, 3.0, "Moderate pace"),
        ExerciseTemplate("Stair Climbing", ExerciseType.OTHER, 8.8, "Taking stairs"),
        ExerciseTemplate("Standing (general)", ExerciseType.OTHER, 1.8, "Standing work"),
        ExerciseTemplate("Sitting (desk work)", ExerciseType.OTHER, 1.3, "Sedentary work")
    )
    
    /**
     * All exercises combined in a single list.
     */
    val exercises: List<ExerciseTemplate> = 
        cardioExercises + strengthExercises + flexibilityExercises + sportExercises + otherExercises
    
    /**
     * Get a map of exercises grouped by type.
     */
    val exercisesByCategory: Map<ExerciseType, List<ExerciseTemplate>> by lazy {
        exercises.groupBy { it.type }
    }
}
