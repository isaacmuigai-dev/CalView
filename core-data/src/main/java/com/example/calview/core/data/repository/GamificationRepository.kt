package com.example.calview.core.data.repository

import com.example.calview.core.data.local.GamificationDao
import com.example.calview.core.data.local.ChallengeEntity
import com.example.calview.core.data.local.ChallengeType
import com.example.calview.core.data.local.BadgeEntity
import com.example.calview.core.data.local.BadgeTier
import com.example.calview.core.data.local.MealDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Calendar
import java.util.UUID
import com.example.calview.core.data.notification.NotificationHandler

class GamificationRepository @Inject constructor(
    private val gamificationDao: GamificationDao,
    private val mealDao: MealDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notificationHandler: NotificationHandler
) {
    val activeChallenges: Flow<List<ChallengeEntity>> = gamificationDao.getActiveChallenges()
    val completedChallenges: Flow<List<ChallengeEntity>> = gamificationDao.getCompletedChallenges()
    val unlockedBadges: Flow<List<BadgeEntity>> = gamificationDao.getAllBadges()

    /**
     * initializes challenges if none exist.
     * Should be called on app start or dashboard load.
     */
    suspend fun initializeChallenges() {
        val currentChallenges = activeChallenges.first()
        if (currentChallenges.isEmpty()) {
            // Create default weekly challenges
            val calendar = Calendar.getInstance()
            // Set to beginning of current week (Monday 00:00:00)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            val weekStart = calendar.timeInMillis
            val oneWeekMs = 7 * 24 * 60 * 60 * 1000L
            
            val challenges = listOf(
                ChallengeEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Consistency King",
                    description = "Log at least 3 meals for 5 days this week.",
                    type = ChallengeType.LOG_MEALS,
                    targetValue = 15, // 3 meals * 5 days = 15 meals (simplified logic)
                    currentProgress = 0,
                    startDate = weekStart,
                    endDate = weekStart + oneWeekMs,
                    badgeRewardId = "consistency_badge"
                ),
                ChallengeEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Protein Power",
                    description = "Hit 100g of protein 3 times this week.",
                    type = ChallengeType.HIT_PROTEIN,
                    targetValue = 3,
                    currentProgress = 0,
                    startDate = weekStart,
                    endDate = weekStart + oneWeekMs,
                    badgeRewardId = "protein_badge"
                ),
                ChallengeEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Early Bird",
                    description = "Log a meal before 9 AM for 5 days.",
                    type = ChallengeType.EARLY_BIRD,
                    targetValue = 5,
                    currentProgress = 0,
                    startDate = weekStart,
                    endDate = weekStart + oneWeekMs,
                    badgeRewardId = "early_bird_badge"
                )
            )
            gamificationDao.insertChallenges(challenges)
        }
    }

    /**
     * Updates progress for challenges based on logs.
     * This is a simplified check. Real app would have more complex query logic.
     */
    suspend fun checkChallengeProgress() {
        val challenges = activeChallenges.first()
        
        challenges.forEach { challenge ->
            if (challenge.type == ChallengeType.LOG_MEALS) {
                // Simplified: Just counting total meals in the period
                // Ideally we'd group by day, but for MVP this is fine or we improve query.
                val meals = mealDao.getAllMeals().first()
                val validMeals = meals.count { it.timestamp >= challenge.startDate && it.timestamp <= challenge.endDate }
                
                if (validMeals != challenge.currentProgress) {
                    val updatedChallenge = challenge.copy(
                        currentProgress = validMeals,
                        isCompleted = validMeals >= challenge.targetValue
                    )
                    gamificationDao.updateChallenge(updatedChallenge)
                    
                    if (updatedChallenge.isCompleted && !challenge.isCompleted) {
                         awardBadge(challenge.badgeRewardId)
                         grantXp(500) // Large XP for challenge completion
                    }
                }
            } else if (challenge.type == ChallengeType.HIT_PROTEIN) {
                // Check distinct days with > 100g protein
                val meals = mealDao.getAllMeals().first()
                val currentWeekMeals = meals.filter { it.timestamp >= challenge.startDate && it.timestamp <= challenge.endDate }
                
                val daysWithHighProtein = currentWeekMeals
                    .groupBy { 
                        java.time.Instant.ofEpochMilli(it.timestamp)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    .count { (_, dayMeals) ->
                        dayMeals.sumOf { it.protein.toDouble() } >= 100.0
                    }
                
                 if (daysWithHighProtein != challenge.currentProgress) {
                    val updatedChallenge = challenge.copy(
                        currentProgress = daysWithHighProtein,
                        isCompleted = daysWithHighProtein >= challenge.targetValue
                    )
                    gamificationDao.updateChallenge(updatedChallenge)
                    if (updatedChallenge.isCompleted && !challenge.isCompleted) {
                         awardBadge(challenge.badgeRewardId)
                         grantXp(500)
                    }
                 }
            } else if (challenge.type == ChallengeType.EARLY_BIRD) {
                // Check meals before 9 AM
                 val meals = mealDao.getAllMeals().first()
                 val currentWeekMeals = meals.filter { it.timestamp >= challenge.startDate && it.timestamp <= challenge.endDate }
                 
                 val earlyBirdDays = currentWeekMeals
                    .groupBy { 
                        java.time.Instant.ofEpochMilli(it.timestamp)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    .count { (_, dayMeals) ->
                        dayMeals.any { 
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = it.timestamp
                            calendar.get(Calendar.HOUR_OF_DAY) < 9
                        }
                    }
                   
                 if (earlyBirdDays != challenge.currentProgress) {
                    val updatedChallenge = challenge.copy(
                        currentProgress = earlyBirdDays,
                        isCompleted = earlyBirdDays >= challenge.targetValue
                    )
                    gamificationDao.updateChallenge(updatedChallenge)
                    if (updatedChallenge.isCompleted && !challenge.isCompleted) {
                         awardBadge(challenge.badgeRewardId)
                         grantXp(500)
                    }
                 }
            }
        }
    }

    
    private suspend fun awardBadge(badgeId: String?) {
        if (badgeId == null) return
        
        // Define badges map or fetch from somewhere
        val badge = when(badgeId) {
            "consistency_badge" -> BadgeEntity(
                id = "consistency_badge",
                name = "Consistency King",
                description = "Logged meals consistently for a week",
                iconResName = "ic_badge_consistency",
                dateUnlocked = System.currentTimeMillis(),
                tier = BadgeTier.GOLD
            )
            "protein_badge" -> BadgeEntity(
                id = "protein_badge",
                name = "Protein Master",
                description = "Hit protein goals multiple times",
                iconResName = "ic_badge_protein",
                dateUnlocked = System.currentTimeMillis(),
                tier = BadgeTier.SILVER
            )
            "early_bird_badge" -> BadgeEntity(
                id = "early_bird_badge",
                name = "Early Bird",
                description = "Consistent morning logger",
                iconResName = "ic_badge_morning",
                dateUnlocked = System.currentTimeMillis(),
                tier = BadgeTier.BRONZE
            )
            "water_warrior" -> BadgeEntity(
                id = "water_warrior",
                name = "Water Warrior",
                description = "Hit your water goal for 3 days straight",
                iconResName = "ic_badge_water",
                dateUnlocked = System.currentTimeMillis(),
                tier = BadgeTier.SILVER
            )
            "macro_master" -> BadgeEntity(
                id = "macro_master",
                name = "Macro Master",
                description = "Perfectly hit all your macro goals today",
                iconResName = "ic_badge_macros",
                dateUnlocked = System.currentTimeMillis(),
                tier = BadgeTier.GOLD
            )
            else -> null
        }
        
        badge?.let { 
            gamificationDao.insertBadge(it)
            notificationHandler.showNotification(
                id = NotificationHandler.ID_BADGE_UNLOCKED,
                channelId = NotificationHandler.CHANNEL_ENGAGEMENT,
                title = "✨ New Badge Unlocked!",
                message = "Congratulations! You've earned the '${it.name}' badge: ${it.description}",
                navigateTo = "gamification"
            )
            grantXp(200) // Bonus for badges
        }
    }

    /**
     * Helper to grant XP and handle leveling.
     */
    suspend fun grantXp(amount: Int) {
        val currentXp = userPreferencesRepository.userXp.first()
        val currentLevel = userPreferencesRepository.userLevel.first()
        
        val newXpTotal = currentXp + amount
        
        // Simple level logic: Level * 1000 = XP required for next level
        val xpRequiredForNext = currentLevel * 1000
        
        if (newXpTotal >= xpRequiredForNext) {
            val newLevel = currentLevel + 1
            userPreferencesRepository.setUserLevel(newLevel)
            userPreferencesRepository.setUserXp(newXpTotal - xpRequiredForNext)
            
            notificationHandler.showNotification(
                id = NotificationHandler.ID_BADGE_UNLOCKED, // Reuse for feedback
                channelId = NotificationHandler.CHANNEL_ENGAGEMENT,
                title = "⏫ Level Up!",
                message = "You've reached Level $newLevel! Your progress is inspiring.",
                navigateTo = "achievements"
            )
        } else {
            userPreferencesRepository.setUserXp(newXpTotal)
        }
    }

    // Master list of all available badges
    private val ALL_BADGES = listOf(
        BadgeEntity(
            id = "consistency_badge",
            name = "Consistency King",
            description = "Log meals consistently for 5 days.",
            iconResName = "ic_badge_consistency",
            tier = BadgeTier.GOLD
        ),
        BadgeEntity(
            id = "protein_badge",
            name = "Protein Master",
            description = "Hit your protein goal 3 times in a week.",
            iconResName = "ic_badge_protein",
            tier = BadgeTier.SILVER
        ),
        BadgeEntity(
            id = "early_bird_badge",
            name = "Early Bird",
            description = "Log breakfast before 9 AM for 5 days.",
            iconResName = "ic_badge_morning",
            tier = BadgeTier.BRONZE
        ),
        BadgeEntity(
            id = "water_warrior",
            name = "Water Warrior",
            description = "Hit your water goal for 3 days straight.",
            iconResName = "ic_badge_water",
            tier = BadgeTier.SILVER
        ),
        BadgeEntity(
            id = "macro_master",
            name = "Macro Master",
            description = "Perfectly hit all your macro goals today.",
            iconResName = "ic_badge_macros",
            tier = BadgeTier.GOLD
        ),
        BadgeEntity(
            id = "night_owl",
            name = "Night Owl",
            description = "Log a late night snack after 10 PM.",
            iconResName = "ic_badge_moon",
            tier = BadgeTier.BRONZE
        ),
        BadgeEntity(
            id = "weekend_warrior",
            name = "Weekend Warrior",
            description = "Stay on track both Saturday and Sunday.",
            iconResName = "ic_badge_weekend",
            tier = BadgeTier.SILVER
        ),
        BadgeEntity(
            id = "streak_flame",
            name = "On Fire!",
            description = "Reach a 7-day streak.",
            iconResName = "ic_badge_fire",
            tier = BadgeTier.PLATINUM
        )
    )

    // Locked badges hints (Mystery Badges)
    private val BADGE_HINTS = mapOf(
        "consistency_badge" to "Show up for 5 days in a row...",
        "protein_badge" to "Focus on your gains 3 times this week...",
        "early_bird_badge" to "Catch the worm before 9 AM...",
        "water_warrior" to "Stay hydrated for 3 days...",
        "macro_master" to "Balance everything perfectly today...",
        "night_owl" to "Getting hungry late at night?",
        "weekend_warrior" to "Don't let the weekend break you...",
        "streak_flame" to "Keep the fire burning for a week..."
    )

    /**
     * Returns all badges, marking them as unlocked based on DB state.
     * Locked badges have dateUnlocked = 0 and mystery descriptions.
     */
    val allBadges: Flow<List<BadgeEntity>> = gamificationDao.getAllBadges().map { unlockedList ->
        val unlockedIds = unlockedList.map { it.id }.toSet()
        
        ALL_BADGES.map { badgeDef ->
            if (unlockedIds.contains(badgeDef.id)) {
                // Return the unlocked version from DB (contains correct date)
                unlockedList.first { it.id == badgeDef.id }
            } else {
                // Return locked version with mystery hint
                badgeDef.copy(
                    dateUnlocked = 0,
                    description = BADGE_HINTS[badgeDef.id] ?: "Mystery Badge...",
                    name = "???"
                )
            }
        }.sortedWith(
            compareBy<BadgeEntity> { it.dateUnlocked == 0L }
                .thenByDescending { it.tier }
        ) // Unlocked first, then by prestige (Platinum -> Bronze)
    }

    suspend fun clearAllData() {
        gamificationDao.clearAllChallenges()
        gamificationDao.clearAllBadges()
    }
}
