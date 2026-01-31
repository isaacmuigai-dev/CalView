package com.example.calview.core.data.coach

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates personalized coaching messages based on user's daily performance.
 */
@Singleton
class CoachMessageGenerator @Inject constructor() {
    
    data class CoachTip(
        val message: String,
        val emoji: String,
        val category: TipCategory
    )
    
    enum class TipCategory {
        MOTIVATION,
        NUTRITION,
        HYDRATION,
        ACTIVITY,
        ACHIEVEMENT
    }
    
    /**
     * Generate a morning tip based on yesterday's performance.
     */
    fun generateMorningTip(
        yesterdayCaloriesConsumed: Int,
        yesterdayCaloriesGoal: Int,
        yesterdayProteinConsumed: Int,
        yesterdayProteinGoal: Int,
        yesterdayWaterGlasses: Int,
        waterGoal: Int,
        streakDays: Int
    ): CoachTip {
        // Check what went well yesterday
        val caloriesOnTrack = yesterdayCaloriesConsumed in (yesterdayCaloriesGoal - 200)..(yesterdayCaloriesGoal + 200)
        val proteinHit = yesterdayProteinConsumed >= yesterdayProteinGoal * 0.9
        val hydratedWell = yesterdayWaterGlasses >= waterGoal
        
        return when {
            streakDays >= 7 && caloriesOnTrack && proteinHit -> CoachTip(
                message = "You're on fire! $streakDays-day streak and you crushed your goals yesterday. Keep the momentum going!",
                emoji = "🔥",
                category = TipCategory.ACHIEVEMENT
            )
            proteinHit && !caloriesOnTrack -> CoachTip(
                message = "Great protein intake yesterday! Today, focus on hitting your calorie target too.",
                emoji = "💪",
                category = TipCategory.NUTRITION
            )
            caloriesOnTrack && !proteinHit -> CoachTip(
                message = "You nailed your calories yesterday! Add more protein-rich foods today like Greek yogurt or chicken.",
                emoji = "🎯",
                category = TipCategory.NUTRITION
            )
            !hydratedWell -> CoachTip(
                message = "Time to hydrate! Start your day with a glass of water to boost energy and metabolism.",
                emoji = "💧",
                category = TipCategory.HYDRATION
            )
            streakDays == 0 -> CoachTip(
                message = "New day, fresh start! Log your first meal to begin building your streak.",
                emoji = "🌅",
                category = TipCategory.MOTIVATION
            )
            else -> CoachTip(
                message = listOf(
                    "Good morning! Set an intention today: What's one healthy choice you'll make?",
                    "Rise and shine! Ready to crush your goals today?",
                    "New day, new opportunities. What are we tracking first?"
                ).random(),
                emoji = "☀️",
                category = TipCategory.MOTIVATION
            )
        }
    }
    
    /**
     * Generate an evening tip based on today's progress.
     */
    fun generateEveningTip(
        caloriesConsumed: Int,
        caloriesGoal: Int,
        proteinConsumed: Int,
        proteinGoal: Int,
        carbsConsumed: Int,
        carbsGoal: Int,
        fatsConsumed: Int,
        fatsGoal: Int,
        waterGlasses: Int,
        waterGoal: Int
    ): CoachTip {
        val caloriesRemaining = caloriesGoal - caloriesConsumed
        val proteinRemaining = proteinGoal - proteinConsumed
        
        return when {
            caloriesRemaining in 100..400 && proteinRemaining > 20 -> CoachTip(
                message = "You have ${caloriesRemaining} cal left. Consider a protein-rich snack like cottage cheese or a protein shake.",
                emoji = "🥛",
                category = TipCategory.NUTRITION
            )
            caloriesRemaining > 500 -> CoachTip(
                message = "You're ${caloriesRemaining} cal under target. Remember, eating too little can slow your metabolism.",
                emoji = "⚠️",
                category = TipCategory.NUTRITION
            )
            caloriesConsumed > caloriesGoal + 300 -> CoachTip(
                message = "Slightly over today's goal. No worries—tomorrow is a new opportunity! Stay consistent.",
                emoji = "🌟",
                category = TipCategory.MOTIVATION
            )
            waterGlasses < waterGoal -> CoachTip(
                message = "Don't forget to hydrate! You're ${waterGoal - waterGlasses} glasses short of your goal.",
                emoji = "💧",
                category = TipCategory.HYDRATION
            )
            proteinConsumed >= proteinGoal && caloriesConsumed <= caloriesGoal + 100 -> CoachTip(
                message = "Amazing day! You hit your protein goal and stayed within calories. Great job!",
                emoji = "🏆",
                category = TipCategory.ACHIEVEMENT
            )
            else -> CoachTip(
                message = listOf(
                    "Another day of progress! Every meal logged is a step toward your goals.",
                    "Great work today! Consistency is the secret to change.",
                    "Day complete! Your future self will thank you for today's logs."
                ).random(),
                emoji = "✨",
                category = TipCategory.MOTIVATION
            )
        }
    }
    
    /**
     * Generate a real-time tip based on current macro gaps.
     */
    fun generateMacroTip(
        proteinRemaining: Int,
        carbsRemaining: Int,
        fatsRemaining: Int,
        caloriesRemaining: Int
    ): CoachTip {
        // Find the biggest macro gap
        val maxGap = maxOf(
            proteinRemaining.coerceAtLeast(0),
            carbsRemaining.coerceAtLeast(0),
            fatsRemaining.coerceAtLeast(0)
        )
        
        return when {
            caloriesRemaining < 0 -> CoachTip(
                message = "You've exceeded your calorie goal. Consider lighter options for your remaining meals.",
                emoji = "📊",
                category = TipCategory.NUTRITION
            )
            maxGap == proteinRemaining && proteinRemaining > 30 -> CoachTip(
                message = "Low on protein today. Try: eggs, chicken, fish, Greek yogurt, or a protein shake.",
                emoji = "🍳",
                category = TipCategory.NUTRITION
            )
            maxGap == carbsRemaining && carbsRemaining > 50 -> CoachTip(
                message = "Room for healthy carbs! Consider: oatmeal, brown rice, sweet potato, or fruits.",
                emoji = "🍠",
                category = TipCategory.NUTRITION
            )
            maxGap == fatsRemaining && fatsRemaining > 20 -> CoachTip(
                message = "Need more healthy fats. Try: avocado, nuts, olive oil, or fatty fish.",
                emoji = "🥑",
                category = TipCategory.NUTRITION
            )
            caloriesRemaining in 100..300 -> CoachTip(
                message = "Almost there! A light snack can help you hit your targets perfectly.",
                emoji = "🎯",
                category = TipCategory.NUTRITION
            )
            else -> CoachTip(
                message = listOf(
                    "You're doing great! Keep making balanced choices.",
                    "Solid progress! Your macros are looking balanced.",
                    "Nice work! You're staying consistent with your logging."
                ).random(),
                emoji = "👍",
                category = TipCategory.MOTIVATION
            )
        }
    }
    fun generateHealthScoreTip(
        currentScore: Int,
        previousScore: Int,
        lastActivityTimestamp: Long
    ): CoachTip {
        val scoreDiff = currentScore - previousScore
        val hoursSinceLastActivity = (System.currentTimeMillis() - lastActivityTimestamp) / (1000 * 60 * 60)
        
        return when {
            hoursSinceLastActivity > 24 -> CoachTip(
                message = listOf(
                    "Welcome back! Let's get your health score up by logging today's meals.",
                    "Missed you! A quick meal log will refresh your health score.",
                    "Ready to get back on track? Log your first meal to update your score."
                ).random(),
                emoji = "👋",
                category = TipCategory.MOTIVATION
            )
            scoreDiff > 0 -> CoachTip(
                message = listOf(
                    "Your health score went up by $scoreDiff points! Whatever you're doing, it's working.",
                    "Progress! Your score climbed $scoreDiff points. Keep up the great habits!",
                    "On the rise! Your health score is up by $scoreDiff. You're doing amazing."
                ).random(),
                emoji = "📈",
                category = TipCategory.ACHIEVEMENT
            )
            scoreDiff < 0 -> CoachTip(
                message = listOf(
                    "Your score dropped slightly. Check your macros and water intake to get back on track.",
                    "A small dip in your score. Focus on your protein goal today to see it rise!",
                    "Let's turn this around! A balanced meal today will boost your health score."
                ).random(),
                emoji = "📉",
                category = TipCategory.MOTIVATION
            )
            currentScore >= 9 -> CoachTip(
                message = listOf(
                    "Excellent health score! You're optimizing your nutrition perfectly.",
                    "Elite level! Your health score is top-tier. Keep inspiring yourself!",
                    "Master of nutrition! Your score reflects your dedication to health."
                ).random(),
                emoji = "⭐",
                category = TipCategory.ACHIEVEMENT
            )
            currentScore >= 7 -> CoachTip(
                message = listOf(
                    "Great score! Small adjustments to protein or water could get you to a 10.",
                    "Strong performance! You're very close to a perfect health score.",
                    "Solid habits! Keep pushing for those daily macro targets to hit a 10."
                ).random(),
                emoji = "🚀",
                category = TipCategory.NUTRITION
            )
            else -> CoachTip(
                message = listOf(
                    "Consistency is key. Log every meal to see your score improve.",
                    "Every log counts! Your health score grows with your consistency.",
                    "Success is built daily. Keep logging to track your journey accurately."
                ).random(),
                emoji = "📝",
                category = TipCategory.MOTIVATION
            )
        }
    }
}
