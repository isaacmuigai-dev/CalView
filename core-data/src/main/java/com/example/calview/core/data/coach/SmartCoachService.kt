package com.example.calview.core.data.coach

import com.example.calview.core.data.local.AnalysisStatus
import com.example.calview.core.data.local.BadgeEntity
import com.example.calview.core.data.local.SocialChallengeEntity
import com.example.calview.core.data.repository.DailyLogRepository
import com.example.calview.core.data.repository.FastingRepository
import com.example.calview.core.data.repository.GamificationRepository
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.SocialChallengeRepository
import com.example.calview.core.data.repository.StreakFreezeRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.repository.WeightHistoryRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Smart AI Coach Service that analyzes comprehensive app data
 * to generate personalized, contextual coaching messages.
 * 
 * Limits: Maximum 3 messages per day at strategic intervals
 * Message length: 4-5 lines for meaningful advice
 */
@Singleton
class SmartCoachService @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mealRepository: MealRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val weightHistoryRepository: WeightHistoryRepository,
    private val gamificationRepository: GamificationRepository,
    private val socialChallengeRepository: SocialChallengeRepository,
    private val streakFreezeRepository: StreakFreezeRepository,
    private val fastingRepository: FastingRepository
) {
    
    companion object {
        // Message intervals: Morning (6-10AM), Afternoon (12-4PM), Evening (6-10PM)
        private const val MORNING_START = 6
        private const val MORNING_END = 10
        private const val AFTERNOON_START = 12
        private const val AFTERNOON_END = 16
        private const val EVENING_START = 18
        private const val EVENING_END = 22
        
        // Minimum hours between messages
        private const val MIN_HOURS_BETWEEN_MESSAGES = 4
        private const val MAX_MESSAGES_PER_DAY = 3
    }
    
    data class SmartCoachTip(
        val message: String,
        val emoji: String,
        val category: CoachMessageGenerator.TipCategory,
        val priority: Int = 0 // Higher = more important
    )
    
    /**
     * Comprehensive data snapshot for analysis
     */
    private data class UserDataSnapshot(
        // Nutrition today
        val caloriesConsumed: Int,
        val caloriesGoal: Int,
        val proteinConsumed: Int,
        val proteinGoal: Int,
        val carbsConsumed: Int,
        val carbsGoal: Int,
        val fatsConsumed: Int,
        val fatsGoal: Int,
        val waterConsumed: Int,
        val waterGoal: Int,
        
        // Activity
        val steps: Long,
        val stepsGoal: Int,
        val caloriesBurned: Int,
        
        // Progress
        val currentStreak: Int,
        val bestStreak: Int,
        val currentWeight: Float,
        val goalWeight: Float,
        val startWeight: Float,
        val userGoal: String, // "Lose", "Gain", "Maintain"
        
        // Gamification
        val userLevel: Int,
        val userXp: Int,
        val xpToNextLevel: Int,
        val recentBadges: List<BadgeEntity>,
        
        // Social Challenges
        val activeChallenges: List<SocialChallengeEntity>,
        
        // Historical patterns
        val weeklyCalorieAverage: Int,
        val proteinHitDaysThisWeek: Int,
        val daysLoggedThisWeek: Int,
        val weightChangeThisWeek: Float,
        
        // Time context
        val currentHour: Int,
        val mealsLoggedToday: Int,
        val lastMealTime: Long?,
        
        // Fasting data
        val isCurrentlyFasting: Boolean = false,
        val fastingProgress: Float = 0f,
        val fastingMinutesElapsed: Int = 0,
        val fastingMinutesRemaining: Int = 0,
        val currentFastType: String? = null,
        val completedFastsThisWeek: Int = 0,
        val fastingStreak: Int = 0
    )
    
    /**
     * Generate a smart coaching tip based on comprehensive data analysis.
     * Returns null if we've reached the daily message limit or it's not the right time.
     */
    suspend fun generateSmartTip(
        caloriesConsumed: Int,
        caloriesGoal: Int,
        proteinConsumed: Int,
        proteinGoal: Int,
        carbsConsumed: Int,
        carbsGoal: Int,
        fatsConsumed: Int,
        fatsGoal: Int,
        waterConsumed: Int,
        steps: Long,
        stepsGoal: Int,
        caloriesBurned: Int,
        currentStreak: Int,
        healthScore: Int
    ): SmartCoachTip? {
        // Check if we should generate a new message
        if (!shouldGenerateMessage()) {
            return null
        }
        
        // Gather comprehensive data
        val snapshot = gatherDataSnapshot(
            caloriesConsumed, caloriesGoal,
            proteinConsumed, proteinGoal,
            carbsConsumed, carbsGoal,
            fatsConsumed, fatsGoal,
            waterConsumed, steps, stepsGoal,
            caloriesBurned, currentStreak
        )
        
        // Generate prioritized tips based on the data
        val tips = mutableListOf<SmartCoachTip>()
        
        // Add tips from different analyzers
        tips.addAll(analyzeAchievements(snapshot))
        tips.addAll(analyzeNutritionProgress(snapshot))
        tips.addAll(analyzeActivityProgress(snapshot))
        tips.addAll(analyzeWeightProgress(snapshot))
        tips.addAll(analyzeSocialChallenges(snapshot))
        tips.addAll(analyzeStreakAndConsistency(snapshot))
        tips.addAll(analyzeTimeBasedSuggestions(snapshot))
        tips.addAll(analyzeFastingProgress(snapshot))
        
        // Select the highest priority tip
        val selectedTip = tips.maxByOrNull { it.priority }
        
        // Update message tracking
        if (selectedTip != null) {
            updateMessageTracking()
        }
        
        return selectedTip
    }
    
    private suspend fun shouldGenerateMessage(): Boolean {
        val now = System.currentTimeMillis()
        val lastMessageTime = userPreferencesRepository.coachLastMessageTime.first()
        val messageCountToday = userPreferencesRepository.coachMessageCountToday.first()
        val lastMessageDate = userPreferencesRepository.coachLastMessageDate.first()
        
        val today = LocalDate.now().toString()
        val currentHour = LocalTime.now().hour
        
        // Reset count if it's a new day
        val effectiveCount = if (lastMessageDate != today) 0 else messageCountToday
        
        // Check daily limit
        // if (effectiveCount >= MAX_MESSAGES_PER_DAY) {
        //     return false
        // }
        
        // Check minimum time between messages
        // val hoursSinceLastMessage = (now - lastMessageTime) / (1000 * 60 * 60)
        // if (hoursSinceLastMessage < MIN_HOURS_BETWEEN_MESSAGES && lastMessageDate == today) {
        //     return false
        // }
        
        // Check if it's an appropriate time window
        // val isValidTimeWindow = (currentHour in MORNING_START..MORNING_END) ||
        //                        (currentHour in AFTERNOON_START..AFTERNOON_END) ||
        //                        (currentHour in EVENING_START..EVENING_END)
        
        // return isValidTimeWindow || effectiveCount == 0 // Always allow first message of the day
        return true // Always allow new messages for "different message on each app open" requirement
    }
    
    private suspend fun updateMessageTracking() {
        val today = LocalDate.now().toString()
        val lastDate = userPreferencesRepository.coachLastMessageDate.first()
        
        val newCount = if (lastDate != today) 1 else {
            userPreferencesRepository.coachMessageCountToday.first() + 1
        }
        
        userPreferencesRepository.setCoachMessageTracking(
            timestamp = System.currentTimeMillis(),
            count = newCount,
            date = today
        )
    }
    
    private suspend fun gatherDataSnapshot(
        caloriesConsumed: Int,
        caloriesGoal: Int,
        proteinConsumed: Int,
        proteinGoal: Int,
        carbsConsumed: Int,
        carbsGoal: Int,
        fatsConsumed: Int,
        fatsGoal: Int,
        waterConsumed: Int,
        steps: Long,
        stepsGoal: Int,
        caloriesBurned: Int,
        currentStreak: Int
    ): UserDataSnapshot {
        val currentWeight = userPreferencesRepository.weight.first()
        val goalWeight = userPreferencesRepository.goalWeight.first()
        val startWeight = userPreferencesRepository.startWeight.first()
        val userGoal = userPreferencesRepository.userGoal.first()
        val userLevel = userPreferencesRepository.userLevel.first()
        val userXp = userPreferencesRepository.userXp.first()
        
        // Get meals for today
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val todayMeals = mealRepository.getMealsForDate(today.format(dateFormatter)).first()
            .filter { it.analysisStatus == AnalysisStatus.COMPLETED }
        
        // Calculate weekly stats
        val weekAgo = today.minusDays(7)
        val allMeals = mealRepository.getAllMeals().first()
        val weeklyMeals = allMeals.filter { meal ->
            val mealDate = java.time.Instant.ofEpochMilli(meal.timestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            mealDate.isAfter(weekAgo) && meal.analysisStatus == AnalysisStatus.COMPLETED
        }
        
        val weeklyCalorieAverage = if (weeklyMeals.isNotEmpty()) {
            val dailyTotals = weeklyMeals.groupBy { meal ->
                java.time.Instant.ofEpochMilli(meal.timestamp)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
            }.map { (_, meals) -> meals.sumOf { it.calories } }
            if (dailyTotals.isNotEmpty()) dailyTotals.average().toInt() else 0
        } else 0
        
        val proteinHitDays = weeklyMeals.groupBy { meal ->
            java.time.Instant.ofEpochMilli(meal.timestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate()
        }.count { (_, meals) ->
            meals.sumOf { it.protein } >= proteinGoal * 0.9
        }
        
        val daysLoggedThisWeek = weeklyMeals.map { meal ->
            java.time.Instant.ofEpochMilli(meal.timestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate()
        }.distinct().count()
        
        // Weight change this week
        val weightHistory = weightHistoryRepository.getAllWeightHistory().first()
        val recentWeights = weightHistory.filter { entry ->
            val entryDate = java.time.Instant.ofEpochMilli(entry.timestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            entryDate.isAfter(weekAgo)
        }.sortedBy { it.timestamp }
        
        val weightChangeThisWeek = if (recentWeights.size >= 2) {
            recentWeights.last().weight - recentWeights.first().weight
        } else 0f
        
        // Get recent badges (unlocked in last 7 days)
        val recentBadges = gamificationRepository.unlockedBadges.first().filter { badge ->
            val unlockDate = java.time.Instant.ofEpochMilli(badge.dateUnlocked)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            unlockDate.isAfter(weekAgo)
        }
        
        // Get active social challenges
        val activeChallenges: List<SocialChallengeEntity> = try {
            socialChallengeRepository.getAllActiveUserChallengesSync()
        } catch (e: Exception) {
            emptyList()
        }
        
        // Best streak calculation
        val allMealDates = allMeals.filter { it.analysisStatus == AnalysisStatus.COMPLETED }
            .map { java.time.Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() }
            .distinct()
        val bestStreak = streakFreezeRepository.calculateBestStreak(allMealDates, currentStreak).first()
        
        val lastMealTime = todayMeals.maxByOrNull { it.timestamp }?.timestamp
        
        // Get fasting data
        val activeFast = fastingRepository.getActiveFast()
        val isCurrentlyFasting = activeFast != null
        val fastingProgress: Float
        val fastingMinutesElapsed: Int
        val fastingMinutesRemaining: Int
        val currentFastType: String?
        
        if (activeFast != null) {
            val now = System.currentTimeMillis()
            val elapsedMillis = now - activeFast.startTime
            fastingMinutesElapsed = (elapsedMillis / 60000).toInt()
            fastingMinutesRemaining = (activeFast.targetDurationMinutes - fastingMinutesElapsed).coerceAtLeast(0)
            fastingProgress = (fastingMinutesElapsed.toFloat() / activeFast.targetDurationMinutes).coerceIn(0f, 1f)
            currentFastType = activeFast.fastingType
        } else {
            fastingMinutesElapsed = 0
            fastingMinutesRemaining = 0
            fastingProgress = 0f
            currentFastType = null
        }
        
        // Get fasting stats
        val completedFastsThisWeek = fastingRepository.getCompletedSessions(20).first()
            .filter { session ->
                session.isCompleted && session.endTime != null &&
                java.time.Instant.ofEpochMilli(session.endTime)
                    .atZone(ZoneId.systemDefault()).toLocalDate().isAfter(weekAgo)
            }.size
        val fastingStreak = fastingRepository.currentStreak.first()
        
        return UserDataSnapshot(
            caloriesConsumed = caloriesConsumed,
            caloriesGoal = caloriesGoal,
            proteinConsumed = proteinConsumed,
            proteinGoal = proteinGoal,
            carbsConsumed = carbsConsumed,
            carbsGoal = carbsGoal,
            fatsConsumed = fatsConsumed,
            fatsGoal = fatsGoal,
            waterConsumed = waterConsumed,
            waterGoal = 8, // 8 glasses standard
            steps = steps,
            stepsGoal = stepsGoal,
            caloriesBurned = caloriesBurned,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            currentWeight = currentWeight,
            goalWeight = goalWeight,
            startWeight = startWeight,
            userGoal = userGoal,
            userLevel = userLevel,
            userXp = userXp,
            xpToNextLevel = userLevel * 1000,
            recentBadges = recentBadges,
            activeChallenges = activeChallenges,
            weeklyCalorieAverage = weeklyCalorieAverage,
            proteinHitDaysThisWeek = proteinHitDays,
            daysLoggedThisWeek = daysLoggedThisWeek,
            weightChangeThisWeek = weightChangeThisWeek,
            currentHour = LocalTime.now().hour,
            mealsLoggedToday = todayMeals.size,
            lastMealTime = lastMealTime,
            isCurrentlyFasting = isCurrentlyFasting,
            fastingProgress = fastingProgress,
            fastingMinutesElapsed = fastingMinutesElapsed,
            fastingMinutesRemaining = fastingMinutesRemaining,
            currentFastType = currentFastType,
            completedFastsThisWeek = completedFastsThisWeek,
            fastingStreak = fastingStreak
        )
    }
    
    private fun analyzeAchievements(snapshot: UserDataSnapshot): List<SmartCoachTip> {
        val tips = mutableListOf<SmartCoachTip>()
        
        // Congratulate on new badges
        if (snapshot.recentBadges.isNotEmpty()) {
            val latestBadge = snapshot.recentBadges.maxByOrNull { it.dateUnlocked }
            if (latestBadge != null) {
                tips.add(SmartCoachTip(
                    message = "Congratulations on earning the '${latestBadge.name}' badge! " +
                            "Your dedication to ${latestBadge.description.lowercase()} is truly impressive. " +
                            "Keep up this momentum and you'll unlock even more achievements. " +
                            "Each badge represents real progress toward your health goals!",
                    emoji = "🏆",
                    category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                    priority = 95
                ))
            }
        }
        
        // Level up congratulations
        val xpProgress = (snapshot.userXp.toFloat() / snapshot.xpToNextLevel * 100).toInt()
        if (xpProgress >= 90 && xpProgress < 100) {
            tips.add(SmartCoachTip(
                message = "You're so close to Level ${snapshot.userLevel + 1}! " +
                        "Just ${snapshot.xpToNextLevel - snapshot.userXp} XP more to go. " +
                        "Log one more meal or hit a macro goal to level up today. " +
                        "Every healthy choice counts toward your next milestone!",
                emoji = "⬆️",
                category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                priority = 85
            ))
        }
        
        // Personal best streak
        if (snapshot.currentStreak > 0 && snapshot.currentStreak == snapshot.bestStreak) {
            val messages = listOf(
                "You're on a ${snapshot.currentStreak}-day streak — your personal best! This level of consistency is what transforms habits into lifestyle. Keep it up!",
                "Amazing! A ${snapshot.currentStreak}-day personal record! Your body is adapting to this healthy routine in incredible ways. Don't stop now!",
                "${snapshot.currentStreak} days of pure dedication! You're rewriting your health story one log at a time. Keep the chain going!",
                "New personal best: ${snapshot.currentStreak} days! Consistent tracking is the secret sauce to reaching your goals. You've got this!",
                "Look at you go! ${snapshot.currentStreak} days of being your best self. Your consistency is inspiring. Let's make it ${snapshot.currentStreak + 1} tomorrow!"
            )
            tips.add(SmartCoachTip(
                message = messages.random(),
                emoji = "🔥",
                category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                priority = 90
            ))
        }
        
        return tips
    }
    
    private fun analyzeNutritionProgress(snapshot: UserDataSnapshot): List<SmartCoachTip> {
        val tips = mutableListOf<SmartCoachTip>()
        
        val caloriePercent = if (snapshot.caloriesGoal > 0) 
            (snapshot.caloriesConsumed.toFloat() / snapshot.caloriesGoal * 100).toInt() else 0
        val proteinPercent = if (snapshot.proteinGoal > 0)
            (snapshot.proteinConsumed.toFloat() / snapshot.proteinGoal * 100).toInt() else 0
        
        // Perfect macro day celebration
        val carbsPercent = if (snapshot.carbsGoal > 0)
            (snapshot.carbsConsumed.toFloat() / snapshot.carbsGoal * 100).toInt() else 0
        val fatsPercent = if (snapshot.fatsGoal > 0)
            (snapshot.fatsConsumed.toFloat() / snapshot.fatsGoal * 100).toInt() else 0
            
        if (caloriePercent in 90..110 && proteinPercent in 90..110 && 
            carbsPercent in 85..115 && fatsPercent in 85..115) {
            tips.add(SmartCoachTip(
                message = "Outstanding nutrition balance today! You've hit all your macros within " +
                        "the optimal range. This kind of precision fuels your body perfectly. " +
                        "Your muscles are thanking you for the protein, and your energy levels " +
                        "should be stable. Keep up this excellent macro tracking!",
                emoji = "⭐",
                category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                priority = 88
            ))
        }
        
        // Protein achievement
        if (proteinPercent >= 100 && snapshot.proteinHitDaysThisWeek >= 5) {
            tips.add(SmartCoachTip(
                message = "Protein champion! You've hit your protein goal ${snapshot.proteinHitDaysThisWeek} days " +
                        "this week. Consistent protein intake supports muscle maintenance, " +
                        "keeps you feeling full longer, and boosts your metabolism. " +
                        "Your body composition is improving with every high-protein day!",
                emoji = "💪",
                category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                priority = 82
            ))
        }
        
        // Protein gap warning with actionable advice
        if (proteinPercent < 50 && snapshot.currentHour >= 14) {
            val proteinNeeded = snapshot.proteinGoal - snapshot.proteinConsumed
            tips.add(SmartCoachTip(
                message = "You need ${proteinNeeded}g more protein today to hit your goal. " +
                        "Try adding Greek yogurt, chicken breast, or a protein shake. " +
                        "Spreading protein throughout the day optimizes muscle protein synthesis. " +
                        "Even a protein-rich snack before bed can help you reach your target!",
                emoji = "🥩",
                category = CoachMessageGenerator.TipCategory.NUTRITION,
                priority = 75
            ))
        }
        
        // Calorie deficit warning (too low)
        if (caloriePercent < 60 && snapshot.currentHour >= 18) {
            tips.add(SmartCoachTip(
                message = "You're significantly under your calorie goal with ${snapshot.caloriesConsumed} kcal consumed. " +
                        "Eating too little can slow your metabolism and affect energy levels. " +
                        "Consider a balanced dinner with lean protein, complex carbs, and veggies. " +
                        "Sustainable progress comes from consistent, adequate nutrition!",
                emoji = "⚠️",
                category = CoachMessageGenerator.TipCategory.NUTRITION,
                priority = 78
            ))
        }
        
        // Calorie surplus warning
        if (caloriePercent > 120) {
            val overBy = snapshot.caloriesConsumed - snapshot.caloriesGoal
            tips.add(SmartCoachTip(
                message = "You're ${overBy} calories over your goal today. Don't stress - " +
                        "one day won't derail your progress. Focus on lighter meals tomorrow " +
                        "and maybe add some extra steps. Consistency over weeks matters more " +
                        "than perfection on any single day. You've got this!",
                emoji = "📊",
                category = CoachMessageGenerator.TipCategory.MOTIVATION,
                priority = 65
            ))
        }
        
        // Weekly consistency recognition
        if (snapshot.daysLoggedThisWeek >= 6) {
            tips.add(SmartCoachTip(
                message = "Amazing consistency! You've logged meals ${snapshot.daysLoggedThisWeek} out of 7 days " +
                        "this week. Research shows that consistent tracking is the #1 predictor " +
                        "of weight management success. You're building habits that last. " +
                        "Your average daily intake of ${snapshot.weeklyCalorieAverage} kcal shows great awareness!",
                emoji = "📈",
                category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                priority = 80
            ))
        }
        
        return tips
    }
    
    private fun analyzeActivityProgress(snapshot: UserDataSnapshot): List<SmartCoachTip> {
        val tips = mutableListOf<SmartCoachTip>()
        
        val stepsPercent = if (snapshot.stepsGoal > 0)
            (snapshot.steps.toFloat() / snapshot.stepsGoal * 100).toInt() else 0
        
        // Steps goal achieved
        if (stepsPercent >= 100) {
            tips.add(SmartCoachTip(
                message = "You crushed your step goal with ${snapshot.steps} steps today! " +
                        "Walking is one of the most underrated forms of exercise - " +
                        "it burns calories, improves cardiovascular health, and boosts mood. " +
                        "You've burned an extra ${snapshot.caloriesBurned} calories through activity!",
                emoji = "🚶",
                category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                priority = 76
            ))
        }
        
        // Afternoon motivation for steps
        if (stepsPercent < 50 && snapshot.currentHour in 14..18) {
            val stepsNeeded = snapshot.stepsGoal - snapshot.steps.toInt()
            tips.add(SmartCoachTip(
                message = "You're halfway through the day with ${snapshot.steps} steps. " +
                        "A 20-minute walk after lunch or taking the stairs more often " +
                        "can easily add 2,000-3,000 steps. Movement also helps with digestion " +
                        "and keeps afternoon energy levels up. ${stepsNeeded} more steps to go!",
                emoji = "👟",
                category = CoachMessageGenerator.TipCategory.ACTIVITY,
                priority = 60
            ))
        }
        
        // Evening step push
        if (stepsPercent in 70..95 && snapshot.currentHour >= 19) {
            val stepsNeeded = snapshot.stepsGoal - snapshot.steps.toInt()
            tips.add(SmartCoachTip(
                message = "You're at ${stepsPercent}% of your step goal - so close! " +
                        "A short evening walk of just $stepsNeeded more steps will get you there. " +
                        "Walking after dinner also aids digestion and can improve sleep quality. " +
                        "End the day strong and complete that step ring!",
                emoji = "🌙",
                category = CoachMessageGenerator.TipCategory.ACTIVITY,
                priority = 72
            ))
        }
        
        return tips
    }
    
    private fun analyzeWeightProgress(snapshot: UserDataSnapshot): List<SmartCoachTip> {
        val tips = mutableListOf<SmartCoachTip>()
        
        if (snapshot.goalWeight <= 0 || snapshot.userGoal == "Maintain") {
            return tips
        }
        
        val startToGoal = abs(snapshot.startWeight - snapshot.goalWeight)
        val currentToGoal = abs(snapshot.currentWeight - snapshot.goalWeight)
        val progressPercent = if (startToGoal > 0) 
            ((startToGoal - currentToGoal) / startToGoal * 100).toInt().coerceIn(0, 100) else 0
        
        // Weight loss/gain progress celebration
        if (progressPercent >= 50 && progressPercent < 75) {
            tips.add(SmartCoachTip(
                message = "You're over halfway to your weight goal! You've already achieved " +
                        "${String.format("%.1f", startToGoal - currentToGoal)} kg of your ${String.format("%.1f", startToGoal)} kg target. " +
                        "This proves your approach is working. Stay patient - the same habits " +
                        "that got you here will carry you to the finish line!",
                emoji = "🎯",
                category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                priority = 84
            ))
        }
        
        // Weekly weight change feedback
        if (snapshot.userGoal == "Lose" && snapshot.weightChangeThisWeek < -0.3f) {
            tips.add(SmartCoachTip(
                message = "Great progress! You've lost ${String.format("%.1f", abs(snapshot.weightChangeThisWeek))} kg this week. " +
                        "Losing 0.5-1 kg weekly is the healthy, sustainable rate that preserves muscle. " +
                        "You're in the perfect zone. Keep your protein high to maintain lean mass " +
                        "while your body burns stored fat for energy!",
                emoji = "📉",
                category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                priority = 83
            ))
        } else if (snapshot.userGoal == "Gain" && snapshot.weightChangeThisWeek > 0.2f) {
            tips.add(SmartCoachTip(
                message = "You're gaining weight at a healthy rate - ${String.format("%.1f", snapshot.weightChangeThisWeek)} kg this week. " +
                        "Combined with resistance training, this supports muscle growth. " +
                        "Focus on progressive overload in your workouts and getting enough sleep. " +
                        "Your body is responding well to the caloric surplus!",
                emoji = "📈",
                category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                priority = 83
            ))
        }
        
        return tips
    }
    
    private fun analyzeSocialChallenges(snapshot: UserDataSnapshot): List<SmartCoachTip> {
        val tips = mutableListOf<SmartCoachTip>()
        
        if (snapshot.activeChallenges.isEmpty()) {
            return tips
        }
        
        val activeChallenge = snapshot.activeChallenges.firstOrNull { it.isActive }
        if (activeChallenge != null) {
            val daysRemaining = ChronoUnit.DAYS.between(
                LocalDate.now(),
                java.time.Instant.ofEpochMilli(activeChallenge.endDate)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
            )
            
            if (daysRemaining in 1..3) {
                tips.add(SmartCoachTip(
                    message = "Your '${activeChallenge.title}' challenge ends in $daysRemaining days! " +
                            "This is the final push - every ${activeChallenge.type.lowercase()} goal you hit counts. " +
                            "Competing with friends makes health journeys more fun and effective. " +
                            "Show them what you're made of in this final stretch!",
                    emoji = "🏁",
                    category = CoachMessageGenerator.TipCategory.MOTIVATION,
                    priority = 77
                ))
            }
        }
        
        return tips
    }
    
    private fun analyzeStreakAndConsistency(snapshot: UserDataSnapshot): List<SmartCoachTip> {
        val tips = mutableListOf<SmartCoachTip>()
        
        // Milestone streaks
        val milestones = listOf(7, 14, 21, 30, 50, 100)
        for (milestone in milestones) {
            if (snapshot.currentStreak == milestone) {
                tips.add(SmartCoachTip(
                    message = "Incredible - a $milestone-day logging streak! Science says it takes " +
                            "${if (milestone >= 21) "at least" else "about"} 21 days to form a habit. " +
                            "You've proven your commitment to tracking your nutrition. " +
                            "This consistency is the foundation of lasting health transformation!",
                    emoji = "🔥",
                    category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                    priority = 92
                ))
                break
            }
        }
        
        // Streak at risk (evening with no meals logged)
        if (snapshot.currentStreak > 3 && snapshot.mealsLoggedToday == 0 && snapshot.currentHour >= 18) {
            tips.add(SmartCoachTip(
                message = "Your ${snapshot.currentStreak}-day streak is at risk! You haven't logged " +
                        "any meals today yet. Even a quick photo of your dinner will keep it alive. " +
                        "Remember: logging consistently is more important than logging perfectly. " +
                        "Don't let one missed day break your amazing momentum!",
                emoji = "⏰",
                category = CoachMessageGenerator.TipCategory.MOTIVATION,
                priority = 88
            ))
        }
        
        // First day encouragement
        if (snapshot.currentStreak == 0 && snapshot.mealsLoggedToday > 0) {
            tips.add(SmartCoachTip(
                message = "Great job logging ${snapshot.mealsLoggedToday} meal${if (snapshot.mealsLoggedToday > 1) "s" else ""} today! " +
                        "Every streak starts with day one. Research shows people who track their food " +
                        "are twice as likely to reach their goals. You've taken the first step. " +
                        "Log again tomorrow to start building your streak!",
                emoji = "🌱",
                category = CoachMessageGenerator.TipCategory.MOTIVATION,
                priority = 70
            ))
        }
        
        return tips
    }
    
    private fun analyzeTimeBasedSuggestions(snapshot: UserDataSnapshot): List<SmartCoachTip> {
        val tips = mutableListOf<SmartCoachTip>()
        
        // Morning motivation
        if (snapshot.currentHour in MORNING_START..MORNING_END && snapshot.mealsLoggedToday == 0) {
            tips.add(SmartCoachTip(
                message = "Good morning! A protein-rich breakfast within an hour of waking " +
                        "jumpstarts your metabolism and stabilizes blood sugar for the day ahead. " +
                        "Try eggs, Greek yogurt, or a protein smoothie. Then snap a photo " +
                        "to log it and start your day with progress!",
                emoji = "☀️",
                category = CoachMessageGenerator.TipCategory.NUTRITION,
                priority = 55
            ))
        }
        
        // Post-lunch check (early afternoon)
        if (snapshot.currentHour in 13..15 && snapshot.mealsLoggedToday >= 2) {
            val caloriesRemaining = snapshot.caloriesGoal - snapshot.caloriesConsumed
            if (caloriesRemaining > 500) {
                tips.add(SmartCoachTip(
                    message = "Afternoon check: You have $caloriesRemaining calories left for today. " +
                            "A healthy snack now can prevent overeating at dinner. Consider nuts, " +
                            "fruit with nut butter, or veggie sticks with hummus. Planning ahead " +
                            "helps you make better choices when hunger strikes later!",
                    emoji = "🥗",
                    category = CoachMessageGenerator.TipCategory.NUTRITION,
                    priority = 58
                ))
            }
        }
        
        // Hydration reminder
        val waterGlasses = snapshot.waterConsumed / 8
        if (snapshot.currentHour >= 14 && waterGlasses < 4) {
            tips.add(SmartCoachTip(
                message = "Hydration check! You've had $waterGlasses glasses of water so far. " +
                        "Aim for at least ${8 - waterGlasses} more by bedtime. Proper hydration " +
                        "supports metabolism, reduces hunger, and improves energy levels. " +
                        "Keep a water bottle nearby as a visual reminder to drink up!",
                emoji = "💧",
                category = CoachMessageGenerator.TipCategory.HYDRATION,
                priority = 62
            ))
        }
        
        return tips
    }
    
    private fun analyzeFastingProgress(snapshot: UserDataSnapshot): List<SmartCoachTip> {
        val tips = mutableListOf<SmartCoachTip>()
        
        // Active fast progress milestones
        if (snapshot.isCurrentlyFasting && snapshot.currentFastType != null) {
            val progressPercent = (snapshot.fastingProgress * 100).toInt()
            val hoursElapsed = snapshot.fastingMinutesElapsed / 60
            val hoursRemaining = snapshot.fastingMinutesRemaining / 60
            
            when {
                progressPercent >= 90 && progressPercent < 100 -> {
                    tips.add(SmartCoachTip(
                        message = listOf(
                            "Almost there! Just $hoursRemaining hour${if (hoursRemaining != 1) "s" else ""} left on your ${snapshot.currentFastType} fast. " +
                                    "Your body is now in peak fat-burning mode, using stored energy efficiently. " +
                                    "The finish line is in sight - stay hydrated and push through!",
                            "You're in the home stretch of your ${snapshot.currentFastType} fast! Only ${snapshot.fastingMinutesRemaining} minutes to go. " +
                                    "Your autophagy processes are at their peak now, cleaning out old cells. " +
                                    "You've got this - the hardest part is behind you!",
                            "Final push! Your ${snapshot.currentFastType} fast is ${progressPercent}% complete. " +
                                    "Your metabolic flexibility is improving with every fast you complete. " +
                                    "Stay strong - you're building incredible discipline!"
                        ).random(),
                        emoji = "🏁",
                        category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                        priority = 92
                    ))
                }
                progressPercent >= 75 -> {
                    tips.add(SmartCoachTip(
                        message = listOf(
                            "Three-quarters done with your ${snapshot.currentFastType} fast! You've been fasting for $hoursElapsed hours. " +
                                    "At this stage, your body has switched to burning fat for fuel. " +
                                    "Growth hormone levels are elevated, supporting muscle preservation. Keep going!",
                            "Amazing progress - ${progressPercent}% through your fast! Your body is now efficiently " +
                                    "tapping into fat stores for energy. Mental clarity often peaks at this stage. " +
                                    "Only $hoursRemaining hours until you complete this fast!"
                        ).random(),
                        emoji = "💪",
                        category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                        priority = 85
                    ))
                }
                progressPercent >= 50 -> {
                    tips.add(SmartCoachTip(
                        message = listOf(
                            "Halfway through your ${snapshot.currentFastType} fast! $hoursElapsed hours down, $hoursRemaining to go. " +
                                    "Your insulin levels have dropped significantly, allowing fat burning to accelerate. " +
                                    "Stay busy, drink water, and remember why you started!",
                            "You've reached the midpoint of your fast - ${progressPercent}% complete! " +
                                    "Your body is transitioning into deeper ketosis now. " +
                                    "The second half often feels easier as hunger hormones stabilize. You're doing great!"
                        ).random(),
                        emoji = "⏱️",
                        category = CoachMessageGenerator.TipCategory.MOTIVATION,
                        priority = 78
                    ))
                }
                progressPercent >= 25 -> {
                    tips.add(SmartCoachTip(
                        message = listOf(
                            "Your ${snapshot.currentFastType} fast is off to a solid start - $hoursElapsed hours in! " +
                                    "Your body is beginning to deplete glycogen stores and prepare for fat burning. " +
                                    "Stay hydrated with water, black coffee, or unsweetened tea. You've got this!",
                            "Quarter of the way through your fast! Your body is adapting to use stored energy. " +
                                    "If you feel hungry, remember: it usually passes in waves. " +
                                    "$hoursRemaining hours remaining - each minute makes you stronger!"
                        ).random(),
                        emoji = "🚀",
                        category = CoachMessageGenerator.TipCategory.MOTIVATION,
                        priority = 70
                    ))
                }
            }
        }
        
        // Fasting streak achievements
        if (snapshot.fastingStreak > 0) {
            val streakMilestones = listOf(3, 5, 7, 14, 21, 30)
            for (milestone in streakMilestones) {
                if (snapshot.fastingStreak == milestone) {
                    tips.add(SmartCoachTip(
                        message = listOf(
                            "Incredible - a $milestone-day fasting streak! Your metabolic flexibility is improving " +
                                    "as your body becomes more efficient at switching between fuel sources. " +
                                    "Consistent fasting has been shown to support longevity and cellular health. " +
                                    "Keep up this amazing discipline!",
                            "You've maintained a $milestone-day fasting streak! This consistency is training " +
                                    "your body to be more metabolically flexible. Research shows regular fasting " +
                                    "can improve insulin sensitivity and support healthy aging. Impressive work!"
                        ).random(),
                        emoji = "🔥",
                        category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                        priority = 89
                    ))
                    break
                }
            }
        }
        
        // Completed fasts this week celebration
        if (snapshot.completedFastsThisWeek >= 3 && !snapshot.isCurrentlyFasting) {
            tips.add(SmartCoachTip(
                message = listOf(
                    "You've completed ${snapshot.completedFastsThisWeek} fasts this week! Your body is becoming " +
                            "a fat-burning machine with this consistent practice. Each fast trains your metabolism " +
                            "to efficiently switch between fuel sources. Keep up the excellent work!",
                    "Impressive - ${snapshot.completedFastsThisWeek} successful fasts this week! " +
                            "Regular fasting is one of the most powerful tools for metabolic health. " +
                            "You're building habits that support long-term wellness and energy!"
                ).random(),
                emoji = "🏆",
                category = CoachMessageGenerator.TipCategory.ACHIEVEMENT,
                priority = 82
            ))
        }
        
        // Encouragement to start a fast (if none active and good time of day)
        if (!snapshot.isCurrentlyFasting && snapshot.fastingStreak == 0 && 
            snapshot.currentHour in 18..21 && snapshot.completedFastsThisWeek < 2) {
            tips.add(SmartCoachTip(
                message = listOf(
                    "Evening is a great time to start an overnight fast! Beginning your fast after dinner " +
                            "means you'll sleep through the most challenging hours. Even a simple 12-14 hour fast " +
                            "gives your digestive system a rest and can improve sleep quality. Ready to try?",
                    "Consider starting a fast tonight! Overnight fasting is the easiest way to experience " +
                            "the benefits of time-restricted eating. Your body can focus on repair and recovery " +
                            "instead of digestion while you sleep. Start simple with a 12-hour window!"
                ).random(),
                emoji = "🌙",
                category = CoachMessageGenerator.TipCategory.MOTIVATION,
                priority = 55
            ))
        }
        
        return tips
    }
    
    /**
     * Get the current coach tip, or generate a new one if conditions are met.
     * This is the main entry point called by the DashboardViewModel.
     */
    suspend fun getCurrentOrGenerateTip(
        caloriesConsumed: Int,
        caloriesGoal: Int,
        proteinConsumed: Int,
        proteinGoal: Int,
        carbsConsumed: Int,
        carbsGoal: Int,
        fatsConsumed: Int,
        fatsGoal: Int,
        waterConsumed: Int,
        steps: Long,
        stepsGoal: Int,
        caloriesBurned: Int,
        currentStreak: Int,
        healthScore: Int
    ): CoachMessageGenerator.CoachTip {
        // Try to generate a smart tip
        val smartTip = generateSmartTip(
            caloriesConsumed, caloriesGoal,
            proteinConsumed, proteinGoal,
            carbsConsumed, carbsGoal,
            fatsConsumed, fatsGoal,
            waterConsumed, steps, stepsGoal,
            caloriesBurned, currentStreak, healthScore
        )
        
        // Convert to CoachTip format if we have a smart tip
        if (smartTip != null) {
            return CoachMessageGenerator.CoachTip(
                message = smartTip.message,
                emoji = smartTip.emoji,
                category = smartTip.category
            )
        }
        
        // Fallback: return a context-aware default tip
        return getFallbackTip(
            caloriesConsumed, caloriesGoal,
            proteinConsumed, proteinGoal,
            currentStreak, healthScore
        )
    }
    
    private fun getFallbackTip(
        caloriesConsumed: Int,
        caloriesGoal: Int,
        proteinConsumed: Int,
        proteinGoal: Int,
        currentStreak: Int,
        healthScore: Int
    ): CoachMessageGenerator.CoachTip {
        val caloriesRemaining = caloriesGoal - caloriesConsumed
        val proteinRemaining = proteinGoal - proteinConsumed
        
        return when {
            currentStreak >= 7 -> CoachMessageGenerator.CoachTip(
                message = "Your ${currentStreak}-day streak shows real commitment to your health journey. " +
                        "Consistency beats perfection every time. Keep logging and watching your progress!",
                emoji = "🔥",
                category = CoachMessageGenerator.TipCategory.MOTIVATION
            )
            healthScore >= 8 -> CoachMessageGenerator.CoachTip(
                message = "Your health score of $healthScore/10 is excellent! You're balancing nutrition, " +
                        "hydration and activity well. Small consistent efforts lead to big transformations.",
                emoji = "⭐",
                category = CoachMessageGenerator.TipCategory.ACHIEVEMENT
            )
            caloriesRemaining > 500 && proteinRemaining > 30 -> CoachMessageGenerator.CoachTip(
                message = "You have $caloriesRemaining calories and ${proteinRemaining}g protein left today. " +
                        "A protein-rich meal will help you hit both targets. Try lean meat, fish, or legumes.",
                emoji = "🎯",
                category = CoachMessageGenerator.TipCategory.NUTRITION
            )
            else -> CoachMessageGenerator.CoachTip(
                message = "Every meal you log is data that helps you understand your body better. " +
                        "Track consistently, learn your patterns, and adjust as needed. You've got this!",
                emoji = "📝",
                category = CoachMessageGenerator.TipCategory.MOTIVATION
            )
        }
    }
}
